package com.euii.jager.middleware;

import com.euii.jager.Jager;
import com.euii.jager.contracts.middleware.AbstractMiddleware;
import com.euii.jager.factories.MessageFactory;
import com.euii.jager.utilities.EmoteReference;
import net.dv8tion.jda.api.entities.Message;

import java.util.ArrayList;

public class IsBotDeveloper extends AbstractMiddleware {

    public IsBotDeveloper(Jager jager) {
        super(jager);
    }

    @Override
    public boolean handle(Message message, MiddlewareStack stack, String... args) {
        ArrayList<String> botDevelopers = jager.getConfig().getDevelopers();

        if (!botDevelopers.contains(message.getAuthor().getId())) {
            MessageFactory.makeError(message, EmoteReference.X + "You need to be whitelisted as a bot developer " +
                    "to execute this command. You do not have permission to execute this command.").queue();
            return false;
        }

        return stack.next();
    }
}
