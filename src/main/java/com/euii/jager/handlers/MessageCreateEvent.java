package com.euii.jager.handlers;

import com.euii.jager.Jager;
import com.euii.jager.commands.CommandContainer;
import com.euii.jager.commands.CommandHandler;
import com.euii.jager.contracts.handlers.EventHandler;
import com.euii.jager.factories.MessageFactory;
import com.euii.jager.middleware.MiddlewareStack;
import com.euii.jager.utilities.EmoteReference;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class MessageCreateEvent extends EventHandler {

    public MessageCreateEvent(Jager jager) {
        super(jager);
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }

        CommandContainer container = CommandHandler.getCommand(event.getMessage());
        if (container != null) {
            if (!container.getCommand().isDirectMessageAllowed() && !event.getChannelType().isGuild()) {
                MessageFactory.makeWarning(event.getMessage(), EmoteReference.WARNING + "You cannot use this " +
                        "command in direct messages.").queue();
                return;
            }

            (new MiddlewareStack(jager, event.getMessage(), container)).next();
        }
    }
}
