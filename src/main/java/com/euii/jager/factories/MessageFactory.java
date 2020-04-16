package com.euii.jager.factories;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.requests.RestAction;

import java.awt.*;
import java.util.Arrays;

import static com.euii.jager.factories.MessageFactory.MessagePriority.*;

public class MessageFactory {

    public static RestAction<Message> makeError(Message entity, String message, Object... args) {
        return makeEmbeddedMessage(entity, ERROR, message, args);
    }

    public static RestAction<Message> makeWarning(Message entity, String message, Object... args) {
        return makeEmbeddedMessage(entity, WARNING, message, args);
    }

    public static RestAction<Message> makeSuccess(Message entity, String message, Object... args) {
        return makeEmbeddedMessage(entity, SUCCESS, message, args);
    }

    public static RestAction<Message> makeInfo(Message entity, String message, Object... args) {
        return makeEmbeddedMessage(entity, INFO, message, args);
    }

    public static EmbedBuilder createEmbeddedBuilder() {
        return new EmbedBuilder();
    }

    public static RestAction<Message> makeEmbeddedMessage(Message entity, MessagePriority priority, String message,
                                                          Object... args) {
        return makeEmbeddedMessage(entity.getChannel(), priority.getColour(), prepareMessage(entity, message, args));
    }

    public static RestAction<Message> makeEmbeddedMessage(MessageChannel channel, Color color, String message) {
        return channel.sendMessage(createEmbeddedBuilder().setColor(color).setDescription(message).build());
    }

    public static RestAction<Message> makeEmbeddedMessage(MessageChannel channel, MessagePriority priority,
                                                          Field... fields) {
        EmbedBuilder embed = createEmbeddedBuilder().setColor(priority.getColour());
        Arrays.stream(fields).forEachOrdered(embed::addField);
        return channel.sendMessage(embed.build());
    }

    private static String prepareMessage(Message entity, String message, Object... args) {
        return String.format(message, args);
    }


    public enum MessagePriority {
        ERROR("#EF5350"),
        WARNING("#FAA61A"),
        SUCCESS("#43B581"),
        INFO("#3A71C1");

        private final String colour;

        MessagePriority(String colour) {
            this.colour = colour;
        }

        public Color getColour() {
            return Color.decode(this.colour);
        }
    }
}
