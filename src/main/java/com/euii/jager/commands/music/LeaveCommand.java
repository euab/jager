package com.euii.jager.commands.music;

import com.euii.jager.Jager;
import com.euii.jager.audio.AudioHandler;
import com.euii.jager.audio.GuildAudioController;
import com.euii.jager.contracts.commands.AbstractCommand;
import com.euii.jager.factories.MessageFactory;
import com.euii.jager.tasks.AudioInactivityTask;
import com.euii.jager.utilities.EmoteReference;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.managers.AudioManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LeaveCommand extends AbstractCommand {

    public LeaveCommand(Jager jager) {
        super(jager, false);
    }

    @Override
    public String getName() {
        return "Leave";
    }

    @Override
    public String getDescription() {
        return "Leave the voice channel that the bot is currently in.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command` - Leaves the current voice channel");
    }

    @Override
    public String getExampleUsage() {
        return null;
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("leave", "stop");
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        AudioManager manager = message.getGuild().getAudioManager();
        GuildAudioController controller = AudioHandler.getGuildAudioPlayer(message.getGuild());

        if (!manager.isConnected())
            return sendErrorMessage(message, "I am not connected to a voice channel. You can add me to one by " +
                    "requesting music using `!play <song>`...");

        String guildId = message.getGuild().getId();
        int size = controller.getScheduler().getQueue().size();

        controller.getPlayer().stopTrack();
        controller.getScheduler().getQueue().clear();

        AudioInactivityTask.PAUSED_PLAYERS.remove(guildId);
        AudioInactivityTask.MISSING_LISTENERS.remove(guildId);
        AudioInactivityTask.ORPHANED_PLAYERS.remove(guildId);

        controller.getPlayer().destroy();
        manager.closeAudioConnection();

        MessageFactory.makeInfo(message, String.format("Disconnected from the voice channel. I have removed `%s song%s` from " +
                "the queue. You can add me back by requesting more music using `!play <song>` " +
                EmoteReference.WAVING_HAND,
                size,
                size == 1 ? "" : "s")
        ).queue();

        return true;
    }
}
