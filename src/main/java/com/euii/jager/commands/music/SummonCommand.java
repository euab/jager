package com.euii.jager.commands.music;

import com.euii.jager.Jager;
import com.euii.jager.audio.AudioHandler;
import com.euii.jager.audio.ConnectionStatus;
import com.euii.jager.contracts.commands.AbstractCommand;
import com.euii.jager.factories.MessageFactory;
import com.euii.jager.utilities.EmoteReference;
import net.dv8tion.jda.api.entities.Message;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SummonCommand extends AbstractCommand {

    public SummonCommand(Jager jager) {
        super(jager, false);
    }

    @Override
    public String getName() {
        return "Summon";
    }

    @Override
    public String getDescription() {
        return "Adds the bot to the voice channel you are currently in.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command` - Adds the bot to your voice channel.");
    }

    @Override
    public String getExampleUsage() {
        return null;
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("summon", "join");
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        ConnectionStatus connectionStatus = AudioHandler.connectToVoiceChannel(message, true);
        if (!connectionStatus.isSuccess()) {
            MessageFactory.makeWarning(message, EmoteReference.WARNING + "**" + connectionStatus.getErrorMessage() + "**")
                    .queue();
            return false;
        }

        MessageFactory.makeSuccess(message, String.format("Connecting to `%s` " + EmoteReference.WAVING_HAND,
                message.getMember().getVoiceState().getChannel().getName())).queue();

        return true;
    }
}
