package com.euii.jager.audio;

import com.euii.jager.api.Prometheus;
import com.euii.jager.contracts.async.AbstractFuture;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Message;

import java.util.function.Consumer;

public class TrackRequest extends AbstractFuture {

    private final GuildAudioController controller;
    private final Message message;
    private final String trackUri;

    public TrackRequest(GuildAudioController controller, Message message, String trackUri) {
        this.controller = controller;
        this.message = message;
        this.trackUri = trackUri;

        controller.setLastMessage(message);
    }

    @Override
    public void handle(final Consumer success, final Consumer<Throwable> failure) {
        handle(success, failure, null);
    }

    public void handle(final Consumer success, final Consumer<Throwable> failure,
                       final Consumer<Playlist> playlistConsumer) {
        Prometheus.audioRequests.inc();

        AudioHandler.AUDIO_PLAYER_MANAGER.loadItemOrdered(controller, trackUri, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                Prometheus.tracksLoaded.inc();

                success.accept(new TrackResponse(controller, track, trackUri));
                AudioHandler.play(message, controller, track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                if (trackUri.startsWith("ytsearch:") || trackUri.startsWith("scsearch:")) {
                    if (playlistConsumer == null) {
                        trackLoaded(playlist.getTracks().get(0));
                        return;
                    }

                    playlistConsumer.accept(AudioHandler.createPlaylist(message, playlist));
                    return;
                }

                success.accept(new TrackResponse(controller, playlist, trackUri));

                for (AudioTrack track : playlist.getTracks()) {
                    Prometheus.tracksLoaded.inc();
                    AudioHandler.play(message, controller, track);
                }
            }

            @Override
            public void noMatches() {
                Prometheus.trackLoadedFailures.inc();
                failure.accept(new FriendlyException("I found nothing matching your query.",
                        FriendlyException.Severity.COMMON, null));
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                Prometheus.trackLoadedFailures.inc();
                failure.accept(new FriendlyException("I couldn't add that to the queue.",
                        FriendlyException.Severity.COMMON, exception));
            }
        });
    }
}
