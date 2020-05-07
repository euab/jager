package com.euii.jager.commands.utility;

import com.euii.jager.Jager;
import com.euii.jager.contracts.commands.AbstractCommand;
import com.euii.jager.factories.MessageFactory;
import com.euii.jager.utilities.EmoteReference;
import net.dv8tion.jda.api.entities.Message;

import java.util.Collections;
import java.util.List;

public class PingCommand extends AbstractCommand {

    public PingCommand(Jager jager) {
        super(jager);
    }

    @Override
    public String getName() {
        return "Ping";
    }

    @Override
    public String getDescription() {
        return "Can be used to check if the bot is running and get latency.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command:` - returns the latency of the bot");
    }

    @Override
    public String getExampleUsage() {
        return null;
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("ping");
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        MessageFactory.makeSuccess(message, EmoteReference.HOURGLASS + "%s ms", message.getJDA().getGatewayPing()).queue();
        return true;
    }
}
