package com.euii.jager.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import net.dv8tion.jda.api.entities.Message;

public class GuildAudioController {

    private final AudioPlayer player;
    private final TrackScheduler scheduler;

    private boolean repeatingQueue = false;
    private Message lastMessage = null;

    public GuildAudioController(AudioPlayerManager manager) {
        player = manager.createPlayer();
        scheduler = new TrackScheduler(this, player);
        player.addListener(scheduler);
    }

    public Message getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(Message lastMessage) {
        this.lastMessage = lastMessage;
    }

    public AudioPlayer getPlayer() {
        return player;
    }

    public TrackScheduler getScheduler() {
        return scheduler;
    }

    public boolean isRepeatingQueue() {
        return repeatingQueue;
    }

    public void setRepeatingQueue(boolean repeatingQueue) {
        this.repeatingQueue = repeatingQueue;
    }

    public PlayerSendHandler getSendHandler() {
        return new PlayerSendHandler(player);
    }
}
