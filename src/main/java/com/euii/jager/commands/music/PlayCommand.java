package com.euii.jager.commands.music;

import com.euii.jager.Jager;
import com.euii.jager.audio.AudioHandler;
import com.euii.jager.audio.ConnectionStatus;
import com.euii.jager.audio.TrackResponse;
import com.euii.jager.contracts.commands.AbstractCommand;
import com.euii.jager.factories.MessageFactory;
import com.euii.jager.utilities.Time;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class PlayCommand extends AbstractCommand {

    public PlayCommand(Jager jager) {
        super(jager, false);
    }

    @Override
    public String getName() {
        return "Play";
    }

    @Override
    public String getDescription() {
        return "Plays the music that you request.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command <song>` - Plays the provided song.");
    }

    @Override
    public String getExampleUsage() {
        return null;
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("play", "add", "enqueue");
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        if (args.length == 0)
            return sendErrorMessage(message, "You need to give me something to play.");

        ConnectionStatus connectionStatus = AudioHandler.connectToVoiceChannel(message);
        if (!connectionStatus.isSuccess()) {
            MessageFactory.makeWarning(message, ":warning: **" + connectionStatus.getErrorMessage() + "**")
                    .queue();
            return false;
        }

        AudioHandler.loadThenExecute(message, buildUri(args)).handle((Consumer<TrackResponse>)
                (TrackResponse response) -> {
            if (response.getController().getPlayer().isPaused()) {
                response.getController().getPlayer().setPaused(false);
            }

            if (response.getController().getPlayer().getPlayingTrack() != null) {
                if (response.isPlaylist())
                    sendResponse(message, response, true);
                else
                    sendResponse(message, response, false);

            }
        }, throwable -> MessageFactory.makeError(message, throwable.getMessage()).queue());

        return true;
    }

    private void sendResponse(Message message, TrackResponse response, boolean isResponsePlaylist) {
        if (isResponsePlaylist) {
            AudioPlaylist playlist = (AudioPlaylist) response.getAudioItem();

            MessageFactory.makeSuccess(message, String.format(
                    "`PLAYLIST ENQUEUED` - %s (%s) containing %s songs.\nBy %s\nThere are `%s` " +
                            "songs ahead in of it in the queue.",
                    playlist.getName(),
                    response.getTrackUri(),
                    playlist.getTracks().size(),
                    message.getAuthor().getName(),
                    AudioHandler.getQueueSize(response.getController())
            )).queue();

        } else {
            AudioTrack track = (AudioTrack) response.getAudioItem();

            int queueSize = AudioHandler.getQueueSize(response.getController());

            MessageEmbed embed = MessageFactory.createEmbeddedBuilder()
                    .setColor(MessageFactory.MessagePriority.SUCCESS.getColour())
                    .setTitle("Enqueued")
                    .setDescription(String.format("[%s](%s)",
                            track.getInfo().title,
                            track.getInfo().uri))
                    .addField("Duration", String.format("`%s`", Time.makeTimestamp(track.getDuration())), true)
                    .addField("Queue size", String.format(
                            "`%s` song%s", queueSize, queueSize == 1 ? "": "s"), true)
                    .setFooter(String.format("Requested by: %s", message.getAuthor().getAsTag()),
                            message.getAuthor().getAvatarUrl())
                    .build();

            message.getChannel().sendMessage(embed).queue();
        }
    }

    private String buildUri(String[] args) {
        String string = String.join(" ", args);

        try {
            new URL(string);
            return string;
        } catch (MalformedURLException e) {
            return "ytsearch:" + string;
        }
    }
}
