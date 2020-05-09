package com.euii.jager.audio;

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
    public void handle(Consumer success, Consumer<Throwable> failure) {
        AudioHandler.AUDIO_PLAYER_MANAGER.loadItemOrdered(controller, trackUri, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                success.accept(new TrackResponse(controller, track, trackUri));
                AudioHandler.play(message, controller, track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                if (trackUri.startsWith("ytsearch:")) {
                    trackLoaded(playlist.getTracks().get(0));
                    return;
                }

                success.accept(new TrackResponse(controller, playlist, trackUri));

                for (AudioTrack track : playlist.getTracks())
                    AudioHandler.play(message, controller, track);
            }

            @Override
            public void noMatches() {
                failure.accept(new FriendlyException("I found nothing matching your query.",
                        FriendlyException.Severity.COMMON, null));
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                failure.accept(new FriendlyException("I couldn't add that to the queue.",
                        FriendlyException.Severity.COMMON, exception));
            }
        });
    }
}
