package com.euii.jager.commands.music;

import com.euii.jager.Jager;
import com.euii.jager.audio.AudioHandler;
import com.euii.jager.audio.GuildAudioController;
import com.euii.jager.contracts.commands.AbstractCommand;
import com.euii.jager.factories.MessageFactory;
import net.dv8tion.jda.api.entities.Message;

import java.util.Collections;
import java.util.List;

public class PauseCommand extends AbstractCommand {

    public PauseCommand(Jager jager) {
        super(jager, false);
    }

    @Override
    public String getName() {
        return "Pause";
    }

    @Override
    public String getDescription() {
        return "Pauses song playback.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command` - Pause playback.");
    }

    @Override
    public String getExampleUsage() {
        return null;
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("pause");
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        GuildAudioController controller = AudioHandler.getGuildAudioPlayer(message.getGuild());

        if (controller.getPlayer().getPlayingTrack() == null)
            return sendErrorMessage(message, "Nothing is playing in the voice channel.");

        if (controller.getPlayer().isPaused()) {
            MessageFactory.makeWarning(message, ":warning: Already paused. Use `!resume` to resume playing.")
                    .queue();
            return true;
        }

        controller.getPlayer().setPaused(true);
        message.getChannel().sendMessage(":pause_button:").queue();

        return true;
    }
}
