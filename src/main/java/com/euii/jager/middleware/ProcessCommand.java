package com.euii.jager.middleware;

import com.euii.jager.Jager;
import com.euii.jager.contracts.middleware.AbstractMiddleware;
import net.dv8tion.jda.api.entities.Message;

import java.util.Arrays;
import java.util.regex.Pattern;

public class ProcessCommand extends AbstractMiddleware {

    private final static String COMMAND_OUTPUT = "Executing command \"%command%\" in \"%category%\" category for ->"
            + "\nUser:\t %author%"
            + "\nGuild:\t %guild%"
            + "\nChannel: %channel%"
            + "\nMessage: %message%";

    private final Pattern argumentsRegex;

    public ProcessCommand(Jager jager) {
        super(jager);
        this.argumentsRegex = Pattern.compile("[\\s\"]+|\"([^\"]*)\"", Pattern.MULTILINE);
    }

    @Override
    public boolean handle(Message message, MiddlewareStack stack, String... args) {
        String[] arguments = argumentsRegex.split(message.getContentRaw());

        jager.getLogger().info(COMMAND_OUTPUT
                .replace("%command%", stack.getCommand().getName())
                .replace("%category%", stack.getCommandContainer().getCategory().getName())
                .replace("%author%", formatUsername(message))
                .replace("%guild%", formatGuild(message))
                .replace("%channel%", formatChannel(message))
                .replace("%message%", message.getContentRaw())
        );

        return stack.getCommand().onCommand(message, Arrays.copyOfRange(arguments, 1, arguments.length));
    }

    private String formatUsername(Message message) {
        return String.format("%s#%s [%s]",
                message.getAuthor().getName(),
                message.getAuthor().getDiscriminator(),
                message.getAuthor().getId()
        );
    }

    private String formatGuild(Message message) {
        if (!message.getChannelType().isGuild())
            return "PRIVATE";

        return String.format("%s [%s]",
                message.getGuild().getName(),
                message.getGuild().getId()
        );
    }

    private CharSequence formatChannel(Message message) {
        if (!message.getChannelType().isGuild())
            return "PRIVATE";

        return String.format("%s [%s]",
                message.getChannel().getName(),
                message.getChannel().getId()
        );
    }
}
