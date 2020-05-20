package com.euii.jager.audio;

import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import net.dv8tion.jda.api.entities.Message;

public class Playlist {

    private final AudioPlaylist songs;
    private final long createdAt;

    private Message message;

    Playlist(AudioPlaylist songs) {
        this.songs = songs;
        this.createdAt = System.currentTimeMillis();
    }

    public AudioPlaylist getSongs() {
        return songs;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }
}
