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
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class HelpCommand extends AbstractCommand {

    private final String categories;

    public HelpCommand(Jager jager) {
        super(jager);

        categories = Arrays.stream(Category.values())
                .map(Category::getName)
                .sorted()
                .collect(Collectors.joining("\n" + EmoteReference.DOT + " ",
                        EmoteReference.DOT + " ",
                        "\n\n"));
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
        if (args.length == 0) {
            return showCategories(message);
        }

        if (!isCommand(args[0]))
            return showCommandsByCategory(message, Category.fromLazyName(args[0]), args[0]);

        if (!args[0].contains("!")) {
            StringBuilder sb = new StringBuilder(args[0]);
            sb.insert(0, "!");
            args[0] = sb.toString();
        }

        return showCommand(message, CommandHandler.getCommand(args[0]), args[0]);
    }

    private boolean showCategories(Message message) {
        Category category = Category.random();

        String helpNote = String.format("Type `:help category` to get a list of all the commands " +
                "from this category.\nExample: `:help %s`",
                category.getName().toLowerCase())
                .replaceAll(":help", generateCommandTrigger(message));

        MessageEmbed embed = MessageFactory.createEmbeddedBuilder()
                .setColor(MessageFactory.MessagePriority.INFO.getColour())
                .setTitle(EmoteReference.SCROLL + "Categories")
                .setDescription(categories + helpNote)
                .build();

        message.getChannel().sendMessage(embed).queue();
        return true;
    }

    private boolean showCommandsByCategory(Message message, Category category, String categoryString) {
        if (category == null) {
            MessageFactory.makeError(message, "I don't know of a category called `%s`...", category);
            return false;
        }

        message.getChannel().sendMessage(String.format(
                EmoteReference.SCROLL + "**%s** ```css\n%s```\n",
                "List of commands",
                CommandHandler.getCommands().stream()
                        .filter(commandContainer -> commandContainer.getCategory().equals(category))
                        .map(container -> {
                            String trigger = container.getCommand().generateCommandTrigger(message);

                            for (int i = trigger.length(); i < 15; i++)
                                trigger += " ";

                            List<String> triggers = container.getCommand().getTriggers();
                            if (triggers.size() == 1)
                                return trigger + "[]";

                            String prefix = container.getCommand().generateCommandPrefix(message);
                            String[] aliases = new String[triggers.size() - 1];

                            for (int i = 1; i < triggers.size(); i++)
                                aliases[i - 1] = prefix + triggers.get(i);

                            return String.format("%s[%s]", trigger, String.join(", ", aliases));
                        })
                        .collect(Collectors.joining("\n"))
        )).queue(sentMessage -> MessageFactory.makeInfo(sentMessage,
                "Type `:help <command>` to see more detailed information about that command.\nExample: `:help :command`."
                                .replaceAll(":help", generateCommandTrigger(message))
                                .replace(":command", CommandHandler.getCommands().stream()
                                        .filter(commandContainer -> commandContainer.getCategory().equals(category))
                                        .collect(Collectors.collectingAndThen(Collectors.toList(), collected -> {
                                            Collections.shuffle(collected);
                                            return collected.stream();
                                        }))
                                        .findFirst().get().getCommand().generateCommandTrigger(message)
                                )
        ).queue());

        return true;
    }

    private boolean showCommand(Message message, CommandContainer commandContainer, String commandString) {
        if (commandContainer == null) {
            MessageFactory.makeWarning(message, "This command doesn't exist!" + EmoteReference.SLIGHT_FROWN).queue();
            return false;
        }

        final String commandPrefix = commandContainer.getCommand().generateCommandPrefix(message);

        EmbedBuilder embed = MessageFactory.createEmbeddedBuilder()
                .setTitle(commandContainer.getCommand().getName())
                .setColor(MessageFactory.MessagePriority.INFO.getColour())
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
            if (CommandHandler.getCommand(command) != null)
                return true;
        }

        return false;
    }
}
