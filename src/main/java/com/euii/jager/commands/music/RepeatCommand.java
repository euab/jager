package com.euii.jager.commands.music;

import com.euii.jager.Jager;
import com.euii.jager.audio.AudioHandler;
import com.euii.jager.audio.GuildAudioController;
import com.euii.jager.contracts.commands.AbstractCommand;
import com.euii.jager.factories.MessageFactory;
import net.dv8tion.jda.api.entities.Message;

import java.util.Arrays;
import java.util.List;

public class RepeatCommand extends AbstractCommand {

    public RepeatCommand(Jager jager) {
        super(jager, false);
    }

    @Override
    public String getName() {
        return "Repeat";
    }

    @Override
    public String getDescription() {
        return "Repeat your queue of music until it is disabled.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return null;
    }

    @Override
    public String getExampleUsage() {
        return null;
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("repeat", "loop");
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        GuildAudioController controller = AudioHandler.getGuildAudioPlayer(message.getGuild());

        if (controller.getPlayer().getPlayingTrack() == null)
            return sendErrorMessage(message, "There is nothing to repeat. Add music to your queue using `!play <song>.");

        controller.setRepeatingQueue(!controller.isRepeatingQueue());

        MessageFactory.makeSuccess(message, String.format(
                "Repeat mode for your queue has been turned `%s` :repeat:",
                controller.isRepeatingQueue() ? "ON" : "OFF"
        )).queue();

        return true;
    }
}
