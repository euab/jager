package com.euii.jager.contracts.commands;

import com.euii.jager.Jager;
import com.euii.jager.commands.Category;
import com.euii.jager.commands.CommandContainer;
import com.euii.jager.commands.CommandHandler;
import com.euii.jager.factories.MessageFactory;
import net.dv8tion.jda.api.entities.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class AbstractCommand {

    protected final Jager jager;
    protected final boolean directMessageAllowed;

    public AbstractCommand(Jager jager) {
        this(jager, true);
    }

    public AbstractCommand(Jager jager, boolean directMessageAllowed) {
        this.jager = jager;
        this.directMessageAllowed = directMessageAllowed;
    }

    public abstract String getName();

    public abstract String getDescription();

    public abstract List<String> getUsageInstructions();

    public abstract String getExampleUsage();

    public abstract List<String> getTriggers();

    public List<String> getMiddleware() {
        return new ArrayList<>();
    }

    public boolean isDirectMessageAllowed() {
        return directMessageAllowed;
    }

    public abstract boolean onCommand(Message message, String[] args);

    protected boolean sendErrorMessage(Message message, String error) {
        Category category = Category.fromCommand(this);

        message.getChannel().sendMessage(MessageFactory.createEmbeddedBuilder()
                .setTitle(getName())
                .setDescription(error)
                .setColor(MessageFactory.MessagePriority.ERROR.getColour())
                .addField("Usage", generateUsageInstructions(message), false)
                .addField("Example usage", generateExampleUsage(message), false)
                .setFooter("Command category: " + category.getName(), null).build()).queue();
        return false;
    }

    public String formatCommandGeneratorString(Message message, String string) {
        CommandContainer container = CommandHandler.getCommand(this);
        String command = container.getDefaultPrefix() + container.getCommand().getTriggers().get(0);

        return string.replaceAll(":command", command);
    }

    public String generateUsageInstructions(Message message) {
        return formatCommandGeneratorString(message,
                getUsageInstructions() == null ? "`:command`" :
                    getUsageInstructions().stream()
                        .collect(Collectors.joining("\n"))
        );
    }

    public String generateExampleUsage(Message message) {
        return formatCommandGeneratorString(message,
                getExampleUsage() == null ? "`command:`" : getExampleUsage()
        );
    }

    public String generateCommandTrigger(Message message) {
        return generateCommandPrefix(message) + getTriggers().get(0);
    }

    public String generateCommandPrefix(Message message) {
        return CommandHandler.getCommand(this).getDefaultPrefix();
    }

    public boolean isSame(AbstractCommand command) {
        return Objects.equals(command.getName(), getName())
                && Objects.equals(command.getDescription(), command.getDescription())
                && Objects.equals(command.getUsageInstructions(), getUsageInstructions())
                && Objects.equals(command.getExampleUsage(), getExampleUsage())
                && Objects.equals(command.getTriggers(), getTriggers());
    }
}
