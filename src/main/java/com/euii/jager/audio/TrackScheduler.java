package com.euii.jager.audio;

import com.euii.jager.factories.MessageFactory;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.api.entities.User;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class TrackScheduler extends AudioEventAdapter {

    public final ExecutorService service = Executors.newCachedThreadPool();

    private final GuildAudioController controller;
    private final AudioPlayer player;
    private final BlockingQueue<TrackContainer> queue;

    private TrackContainer trackContainer;

    public TrackScheduler(GuildAudioController controller, AudioPlayer player) {
        this.controller = controller;
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
    }

    public void queue(AudioTrack track, User user) {
        TrackContainer container = new TrackContainer(track, user);

        if (!player.startTrack(track, true)) {
            queue.offer(container);
            return;
        }

        if (controller.getLastMessage() != null) {
            trackContainer = container;
            sendNowPlaying(container);
        }
    }

    public void nextTrack() {
        TrackContainer container = queue.poll();

        if (container == null) {
            if (controller.getLastMessage() == null)
                return;
            return;
        }

        trackContainer = container;
        player.playTrack(container.getAudioTrack());
        if (controller.getLastMessage() != null)
            sendNowPlaying(container);
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason reason) {
        if (reason.mayStartNext) {
            if (controller.isRepeatingQueue())
                queue.offer(new TrackContainer(track.makeClone(), getTrackContainer().getUser()));

            nextTrack();
            return;
        }
    }

    private void sendNowPlaying(TrackContainer container) {
        MessageFactory.makeSuccess(
                controller.getLastMessage(),
                String.format("Now playing [%s](%s)\n`%s` - Requested by %s",
                        container.getAudioTrack().getInfo().title,
                        container.getAudioTrack().getInfo().uri,
                        container.getFormattedDuration(),
                        container.getUser().getAsMention())
        ).queue();
    }

    public BlockingQueue<TrackContainer> getQueue() {
        return queue;
    }

    public TrackContainer getTrackContainer() {
        return trackContainer;
    }
}
