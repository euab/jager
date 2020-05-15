package com.euii.jager.tasks;

import com.euii.jager.Jager;
import com.euii.jager.audio.AudioHandler;
import com.euii.jager.audio.GuildAudioController;
import com.euii.jager.contracts.tasks.AbstractTask;
import com.euii.jager.factories.MessageFactory;
import com.euii.jager.utilities.EmoteReference;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class AudioInactivityTask extends AbstractTask {

    public static Map<Long, Integer> MISSING_LISTENERS = new HashMap<>();
    public static Map<Long, Integer> ORPHANED_PLAYERS = new HashMap<>();
    public static Map<Long, Integer> PAUSED_PLAYERS = new HashMap<>();

    public AudioInactivityTask(Jager jager) {
        super(jager, 30, 30, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        jager.getLogger().info("[BG task] AudioInactivityTask running now. UUID: {}", getUuid());

        Iterator<AudioManager> iterator = jager.getJda().getAudioManagers().iterator();

        try {
            while(iterator.hasNext()) {
                AudioManager manager = iterator.next();

                if (!manager.isConnected())
                    continue;

                long guildId = manager.getGuild().getIdLong();

                if (!AudioHandler.CONTROLLER_MAP.containsKey(guildId)) {
                    handleOrphanedPlayer(manager, null, guildId);
                    continue;
                }

                GuildAudioController controller = AudioHandler.CONTROLLER_MAP.get(guildId);

                if (controller.getScheduler().getQueue().isEmpty() && controller.getPlayer().getPlayingTrack() == null) {
                    handleOrphanedPlayer(manager, controller, guildId);
                }

                if (ORPHANED_PLAYERS.containsKey(guildId))
                    ORPHANED_PLAYERS.remove(guildId);

                if (controller.getPlayer().isPaused()) {
                    handlePausedAudio(manager, controller, guildId);
                    continue;
                }

                VoiceChannel voiceChannel = manager.getConnectedChannel();

                boolean currActive = false;
                for (Member member : voiceChannel.getMembers()) {
                    if (member.getUser().isBot())
                        continue;

                    if (member.getVoiceState().isDeafened())
                        continue;

                    currActive = true;
                    break;
                }

                if (currActive && !manager.getGuild().getSelfMember().getVoiceState().isMuted()) {
                    MISSING_LISTENERS.remove(guildId);
                    continue;
                }

                int times = MISSING_LISTENERS.getOrDefault(guildId, 0) + 1;

                if (times <= Math.max(1, 5) * 2) {
                    MISSING_LISTENERS.put(guildId, times);
                }

                destroyPlayer(manager, controller, guildId);
            }
        } catch (Exception e) {
            jager.getLogger().error("[BG Task] An error occurred during execution of AudioInactivityTask: " +
                    e.getMessage(), e);
        }
    }

    private void handleOrphanedPlayer(AudioManager manager, GuildAudioController controller, long guildId) {
        int times = ORPHANED_PLAYERS.getOrDefault(guildId, 0) + 1;

        if (times <= Math.max(1, 2) * 2) {
            ORPHANED_PLAYERS.put(guildId, times);
            return;
        }

        destroyPlayer(manager, controller, guildId);
    }

    private void handlePausedAudio(AudioManager manager, GuildAudioController controller, long guildId) {
        int times = PAUSED_PLAYERS.getOrDefault(guildId, 0) + 1;

        if (times <= Math.max(1, 10) * 2) {
            PAUSED_PLAYERS.put(guildId, times);
            return;
        }

        destroyPlayer(manager, controller, guildId);
    }

    private void destroyPlayer(AudioManager manager, GuildAudioController controller, long guildId) {
        if (controller != null) {
            controller.getScheduler().getQueue().clear();

            if (controller.getLastMessage() != null && controller.getLastMessage().getTextChannel().canTalk()) {
                MessageFactory.makeWarning(controller.getLastMessage(), "I have left the channel because there " +
                        "was no activity anyway. " + EmoteReference.WAVING_HAND).queue();
            }
        }

        AudioHandler.CONTROLLER_MAP.remove(guildId);
        MISSING_LISTENERS.remove(guildId);
        PAUSED_PLAYERS.remove(guildId);
        ORPHANED_PLAYERS.remove(guildId);

        manager.closeAudioConnection();
    }
}
