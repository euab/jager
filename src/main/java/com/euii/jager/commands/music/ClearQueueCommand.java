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

public class ClearQueueCommand extends AbstractCommand {

    public ClearQueueCommand(Jager jager) {
        super(jager, false);
    }

    @Override
    public String getName() {
        return "Clear queue";
    }

    @Override
    public String getDescription() {
        return "Clears the current queue in your server to start afresh.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command` - Clears the server queue.");
    }

    @Override
    public String getExampleUsage() {
        return null;
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("clearqueue", "cq", "flushqueue");
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        GuildAudioController controller = AudioHandler.getGuildAudioPlayer(message.getGuild());

        if (controller.getPlayer().getPlayingTrack() == null)
            sendErrorMessage(message, "There is nothing to clear, as either the bot is not in the voice channel or " +
                    "there is no active player. To request music, use `!play <song>`.");

        if (controller.getScheduler().getQueue().isEmpty()) {
            MessageFactory.makeWarning(message, ":warning: There are no songs pending to play in the queue " +
                    "right now. You can add songs using `!play <song>`.");

            return false;
        }

        // Confirmation needs to take place before the queue is cleared so that the size of the queue can be recorded.

        MessageFactory.makeSuccess(message, String.format(
                "**The queue has been cleared.** I removed `%s` songs from the queue.\nYou can request more songs " +
                        "using `!play <song>` and you can remove me from the voice channel using `!leave`.",
                controller.getScheduler().getQueue().size())).queue();
        controller.getScheduler().getQueue().clear();

        return true;
    }


}
