package com.euii.jager.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public class AudioHandler {

    public static final AudioPlayerManager AUDIO_PLAYER_MANAGER;
    public static final Map<Long, GuildAudioController> CONTROLLER_MAP;

    static {
        CONTROLLER_MAP = new HashMap<>();
        AUDIO_PLAYER_MANAGER = new DefaultAudioPlayerManager();

        AudioSourceManagers.registerRemoteSources(AUDIO_PLAYER_MANAGER);
        AudioSourceManagers.registerLocalSource(AUDIO_PLAYER_MANAGER);
    }

    public static TrackRequest loadThenExecute(Message message,String trackUri) {
        return new TrackRequest(getGuildAudioPlayer(message.getGuild()), message, trackUri);
    }

    public static void skipTrack(Message message) {
        GuildAudioController controller = getGuildAudioPlayer(message.getGuild());
        controller.getScheduler().nextTrack();
    }

    public static ConnectionStatus play(Message message, GuildAudioController controller, AudioTrack track) {
        ConnectionStatus connectionStatus = connectToVoiceChannel(message);

        if (connectionStatus.isSuccess())
            controller.getScheduler().queue(track, message.getAuthor());

        return connectionStatus;
    }

    public static ConnectionStatus connectToVoiceChannel(Message message) {
        return connectToVoiceChannel(message, false);
    }

    public static synchronized GuildAudioController getGuildAudioPlayer(Guild guild) {
        long guildId = Long.parseLong(guild.getId());
        GuildAudioController audioController = CONTROLLER_MAP.get(guildId);

        if (audioController == null) {
            audioController = new GuildAudioController(AUDIO_PLAYER_MANAGER);
            audioController.getPlayer().setVolume(50);

            CONTROLLER_MAP.put(guildId, audioController);
        }

        guild.getAudioManager().setSendingHandler(audioController.getSendHandler());

        return audioController;
    }

    public static ConnectionStatus connectToVoiceChannel(Message message, boolean switchIfConnected) {
        AudioManager manager = message.getGuild().getAudioManager();

        if (!manager.isAttemptingToConnect()) {
            VoiceChannel voiceChannel = message.getMember().getVoiceState().getChannel();
            if (voiceChannel == null)
                return ConnectionStatus.NOT_CONNECTED;

            if (manager.isConnected()) {
                if (voiceChannel.getIdLong() == manager.getConnectedChannel().getIdLong())
                    return ConnectionStatus.CONNECTED;

                if (switchIfConnected) {
                    return connectToVoiceChannel(message, voiceChannel, manager);
                }

                return ConnectionStatus.CONNECTED;
            }

            return connectToVoiceChannel(message, voiceChannel, manager);
        }

        return ConnectionStatus.CONNECTED;
    }

    private static ConnectionStatus connectToVoiceChannel(Message message, VoiceChannel voiceChannel,
                                                          AudioManager manager) {
        EnumSet<Permission> permissions = message.getGuild().getMember(message.getJDA().getSelfUser())
                .getPermissions(voiceChannel);
        if (!permissions.contains(Permission.VOICE_CONNECT))
            return ConnectionStatus.MISSING_PERMISSIONS;

        if (voiceChannel.getUserLimit() > 0 && !permissions.contains(Permission.VOICE_MOVE_OTHERS)
                && voiceChannel.getUserLimit() <= voiceChannel.getMembers().size())
            return ConnectionStatus.USER_LIMIT;

        try {
            manager.openAudioConnection(voiceChannel);
        } catch (Exception e) {
            return ConnectionStatus.USER_LIMIT;
        }

        return ConnectionStatus.CONNECTED;
    }

    public static int getQueueSize(GuildAudioController controller) {
        return controller.getPlayer().getPlayingTrack() == null ?
                controller.getScheduler().getQueue().size() :
                controller.getScheduler().getQueue().size() + 1;
    }
}
