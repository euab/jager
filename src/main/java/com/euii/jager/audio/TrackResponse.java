package com.euii.jager.audio;

import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;

public class TrackResponse {

    private final GuildAudioController controller;
    private final AudioItem audioItem;
    private final String trackUri;

    public TrackResponse(GuildAudioController controller, AudioItem audioItem, String trackUri) {
        this.controller = controller;
        this.audioItem = audioItem;
        this.trackUri = trackUri;
    }

    public GuildAudioController getController() {
        return controller;
    }

    public AudioItem getAudioItem() {
        return audioItem;
    }

    public String getTrackUri() {
        return trackUri;
    }

    public boolean isPlaylist() {
        return getAudioItem() instanceof AudioPlaylist;
    }
}
