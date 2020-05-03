package com.euii.jager.commands.music;

import com.euii.jager.Jager;
import com.euii.jager.audio.AudioHandler;
import com.euii.jager.audio.GuildAudioController;
import com.euii.jager.audio.TrackContainer;
import com.euii.jager.audio.TrackScheduler;
import com.euii.jager.contracts.commands.AbstractCommand;
import com.euii.jager.factories.MessageFactory;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class NowPlayingCommand extends AbstractCommand {

    public NowPlayingCommand(Jager jager) {
        super(jager, false);
    }

    @Override
    public String getName() {
        return "Now playing";
    }

    @Override
    public String getDescription() {
        return "Get information about the track currently playing.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command` - Shows information about the song currently playing.");
    }

    @Override
    public String getExampleUsage() {
        return null;
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("np", "nowplaying", "song", "queue");
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        GuildAudioController controller = AudioHandler.getGuildAudioPlayer(message.getGuild());

        if (controller.getPlayer().getPlayingTrack() == null) {
            return sendErrorMessage(message, "There are no songs currently playing. Request music using `!play`.");
        }

        MessageEmbed embed = MessageFactory.createEmbeddedBuilder()
                .setColor(MessageFactory.MessagePriority.SUCCESS.getColour())
                .setTitle(controller.getPlayer().isPaused() ? "PAUSED" : "PLAYING")
                .setDescription(buildCurrTrackInfo(controller.getPlayer(), controller.getScheduler()))
                .addField("Enqueued: ", buildQueueList(controller.getScheduler()), false)
                .setFooter(String.format("Requested by: %s", message.getAuthor().getAsTag()),
                        message.getAuthor().getAvatarUrl())
                .build();

        message.getChannel().sendMessage(embed).queue();

        return true;
    }

    private String buildCurrTrackInfo(AudioPlayer player, TrackScheduler scheduler) {
        return String.format(
                "[%s](%s)\nPlaying now with `%s` remaining.",
                player.getPlayingTrack().getInfo().title,
                player.getPlayingTrack().getInfo().uri,
                scheduler.getTrackContainer().getFormattedRemaining()
        );
    }

    private String buildQueueList(TrackScheduler scheduler) {
        if (scheduler.getQueue().isEmpty())
            return "There is nothing ahead in the queue.";

        int number = 1;
        String songs = "";

        Iterator<TrackContainer> containerIterator = scheduler.getQueue().iterator();
        while (containerIterator.hasNext() && number <= 6) {
            TrackContainer next = containerIterator.next();
            songs += String.format(
                    "`%s` [%s](%s)\n", number++, next.getAudioTrack().getInfo().title,
                    next.getAudioTrack().getInfo().uri
            );
        }

        if (scheduler.getQueue().size() > 6)
            songs += String.format("__**%s** more song%s enqueued.__", scheduler.getQueue().size() - 6,
                    scheduler.getQueue().size() == 7 ? "" : 's');

        return songs;
    }
}
