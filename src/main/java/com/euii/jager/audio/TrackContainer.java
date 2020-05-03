package com.euii.jager.audio;

import com.euii.jager.utilities.Time;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.User;

import java.util.ArrayList;
import java.util.List;

public class TrackContainer {

    private final AudioTrack audioTrack;
    private final User user;
    private final List<Long> skips;
    private int playedTime;

    public TrackContainer(AudioTrack audioTrack, User user) {
        this.audioTrack = audioTrack;
        this.user = user;

        skips = new ArrayList<>();
        playedTime = 0;
    }

    public AudioTrack getAudioTrack() {
        return audioTrack;
    }

    public User getUser() {
        return user;
    }

    public List<Long> getSkips() {
        return skips;
    }

    public int getPlayedTime() {
        return playedTime;
    }

    public String getFormattedPlayTime() {
        return Time.makeTimestamp(getAudioTrack().getPosition());
    }

    public String getFormattedRemaining() {
        return Time.makeTimestamp(getAudioTrack().getDuration() - getAudioTrack().getPosition());
    }

    public String getFormattedDuration() {
        return Time.makeTimestamp(getAudioTrack().getDuration());
    }
}
