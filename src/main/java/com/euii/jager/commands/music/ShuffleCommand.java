package com.euii.jager.commands.music;

import com.euii.jager.Jager;
import com.euii.jager.audio.AudioHandler;
import com.euii.jager.audio.GuildAudioController;
import com.euii.jager.audio.TrackContainer;
import com.euii.jager.contracts.commands.AbstractCommand;
import com.euii.jager.factories.MessageFactory;
import com.euii.jager.utilities.EmoteReference;
import net.dv8tion.jda.api.entities.Message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShuffleCommand extends AbstractCommand {

    public ShuffleCommand(Jager jager) {
        super(jager, false);
    }

    @Override
    public String getName() {
        return "Shuffle";
    }

    @Override
    public String getDescription() {
        return "Shuffles the music queue into a random order, for when a queue needs a random chance.";
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
        return Collections.singletonList("shuffle");
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        GuildAudioController controller = AudioHandler.getGuildAudioPlayer(message.getGuild());

        if (controller.getScheduler().getQueue().isEmpty())
            return sendErrorMessage(message, String.format("There is nothing in the queue for me to shuffle. Add " +
                    "songs to the queue using `%sadd` before using this command",
                    generateCommandPrefix(message))
            );

        List<TrackContainer> queue = new ArrayList<>();
        controller.getScheduler().getQueue().drainTo(queue);

        Collections.shuffle(queue);

        controller.getScheduler().getQueue().addAll(queue);

        MessageFactory.makeSuccess(message, String.format(EmoteReference.SHUFFLE + "Shuffled `%s` song%s in your queue.",
                queue.size(),
                queue.size() == 1 ? "" : "s")
        ).queue();

        return true;
    }
}
