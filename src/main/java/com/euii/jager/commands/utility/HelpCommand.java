package com.euii.jager.commands.utility;

import com.euii.jager.Jager;
import com.euii.jager.commands.Category;
import com.euii.jager.commands.CommandContainer;
import com.euii.jager.commands.CommandHandler;
import com.euii.jager.contracts.commands.AbstractCommand;
import com.euii.jager.factories.MessageFactory;
import com.euii.jager.utilities.EmoteReference;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class HelpCommand extends AbstractCommand {

    public HelpCommand(Jager jager) {
        super(jager);
    }

    @Override
    public String getName() {
        return "Help";
    }

    @Override
    public String getDescription() {
        return "Provides assistance with using Jager commands.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command` - Shows detailed information on how to use the chosen command.");
    }

    @Override
    public String getExampleUsage() {
        return null;
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("help");
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        if (!args[0].contains("!")) {
            StringBuilder sb = new StringBuilder(args[0]);
            sb.insert(0, "!");
            args[0] = sb.toString();
        }
        return showCommand(message, CommandHandler.getCommand(args[0]), args[0]);
    }

    private boolean showCommand(Message message, CommandContainer commandContainer, String commandString) {
        if (commandContainer == null) {
            MessageFactory.makeWarning(message, "This command doesn't exist! " + EmoteReference.SLIGHT_FROWN).queue();
            return false;
        }

        final String commandPrefix = commandContainer.getCommand().generateCommandPrefix(message);

        EmbedBuilder embed = MessageFactory.createEmbeddedBuilder()
                .setTitle(commandContainer.getCommand().getName())
                .setColor(MessageFactory.MessagePriority.SUCCESS.getColour())
                .addField("Usage", commandContainer.getCommand().generateUsageInstructions(message), false)
                .setFooter("Category: " + commandContainer.getCategory().getName(), null);

        if (commandContainer.getCommand().getTriggers().size() > 1) {
            embed.addField("Command aliases: ", commandContainer.getCommand().getTriggers().stream()
                    .skip(1)
                    .map(trigger -> commandPrefix + trigger)
                    .collect(Collectors.joining("`, `", "`", "`")), false);
        }

        String description = commandContainer.getCommand().getDescription();

        message.getChannel().sendMessage(embed.setDescription(description).build()).queue();
        return true;
    }

    private boolean isCommand(String command) {
        for (Category category : Category.values()) {
            if (command.startsWith("!") && CommandHandler.getCommand(command) != null)
                return true;
        }

        return false;
    }
}
