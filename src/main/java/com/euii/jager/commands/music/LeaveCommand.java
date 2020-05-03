package com.euii.jager.commands.music;

import com.euii.jager.Jager;
import com.euii.jager.audio.AudioHandler;
import com.euii.jager.audio.GuildAudioController;
import com.euii.jager.contracts.commands.AbstractCommand;
import com.euii.jager.factories.MessageFactory;
import net.dv8tion.jda.api.entities.Message;

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
        // TODO: Implement checks if bot is in active voice channel.

        GuildAudioController controller = AudioHandler.getGuildAudioPlayer(message.getGuild());

        controller.getPlayer().stopTrack();
        message.getGuild().getAudioManager().closeAudioConnection();

        MessageFactory.makeSuccess(message, "Left the voice channel. You can add me back using `!summon` or " +
                "`!play <song>` :wave:.").queue();

        return true;
    }
}
