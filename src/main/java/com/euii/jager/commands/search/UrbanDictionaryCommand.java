package com.euii.jager.commands.search;

import com.euii.jager.Jager;
import com.euii.jager.contracts.commands.AbstractCommand;
import com.euii.jager.factories.MessageFactory;
import com.euii.jager.factories.RequestFactory;
import com.euii.jager.requests.Response;
import com.euii.jager.requests.services.UrbanDictionaryService;
import com.euii.jager.utilities.EmoteReference;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class UrbanDictionaryCommand extends AbstractCommand {

    public UrbanDictionaryCommand(Jager jager) {
        super(jager);
    }

    @Override
    public String getName() {
        return "Urban Dictionary";
    }

    @Override
    public String getDescription() {
        return "Search the Urban Dictionary for definitions. Everyone's favorite ~~proof of concept~~ command";
    }

    @Override
    public List<String> getUsageInstructions() {
        return null;
    }

    @Override
    public String getExampleUsage() {
        return null;
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("urban");
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        // TODO: Add NSFW protection for this command.

        message.getChannel().sendTyping().queue();

        RequestFactory.makeGetRequest("https://api.urbandictionary.com/v0/define")
                .addParameter("term", String.join(" ", args))
                .send((Consumer<Response>) response -> {
                    UrbanDictionaryService service = (UrbanDictionaryService) response
                            .toService(UrbanDictionaryService.class);

                    if (!service.hasData()) {
                        MessageFactory.makeWarning(message,
                                String.format("I found no results for %s " + EmoteReference.CRYING_FACE
                                        , String.join(" ", args))).queue();
                        return;
                    }

                    UrbanDictionaryService.UrbanDictionaryObject definition = service.getList().get(0);

                    EmbedBuilder embed = MessageFactory.createEmbeddedBuilder()
                            .setColor(MessageFactory.MessagePriority.INFO.getColour())
                            .setDescription(definition.getDefinition())
                            .setTitle(definition.getPermalink())
                            .addField("Example", definition.getExample(), false)
                            .setFooter("Results from urbandictionary.com");

                    message.getChannel().sendMessage(embed.build()).queue();
                });
        return true;
    }
}
