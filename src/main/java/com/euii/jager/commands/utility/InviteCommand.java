package com.euii.jager.commands.utility;

import com.euii.jager.Jager;
import com.euii.jager.contracts.commands.AbstractCommand;
import com.euii.jager.factories.MessageFactory;
import net.dv8tion.jda.api.entities.Message;

import java.util.Collections;
import java.util.List;

public class InviteCommand extends AbstractCommand {

    public InviteCommand(Jager jager) {
        super(jager);
    }

    @Override
    public String getName() {
        return "Invite Command";
    }

    @Override
    public String getDescription() {
        return "Returns a link to invite the bot to other servers.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`command` - Gives you an invite to invite the bot to other servers.");
    }

    @Override
    public String getExampleUsage() {
        return null;
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("invite");
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        String response = "[Click here](oauth) to invite me to another server.";
        String formattedResponse = response.replace("oauth", System.getenv("JAGEN_OAUTH"));

        MessageFactory.makeInfo(message, formattedResponse).queue();

        return true;
    }
}
