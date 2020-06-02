package com.euii.jager.commands.debug;

import com.euii.jager.Jager;
import com.euii.jager.contracts.commands.AbstractCommand;
import com.euii.jager.factories.MessageFactory;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class EvalCommand extends AbstractCommand {

    public EvalCommand(Jager jager) {
        super(jager);
    }

    @Override
    public String getName() {
        return "Eval";
    }

    @Override
    public String getDescription() {
        return "Evaluates and executes code server-side.";
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
        return Collections.singletonList("eval");
    }

    @Override
    public List<String> getMiddleware() {
        return Collections.singletonList("is-bot-developer");
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        if (args.length == 0) {
            MessageFactory.makeWarning(message, "No arguments given therefore there is no expression to " +
                    "evaluate.").queue();
            return false;
        }

        String[] argumentsRaw = message.getContentRaw().split(" ");
        String evalStatement = String.join(" ", Arrays.copyOfRange(argumentsRaw, 1, argumentsRaw.length));

        try {
            Object out = buildScriptEngine(message).eval("(function() { with (imports) {return " + evalStatement
                    + "}})();");
            String output = out == null ? "Executed. Return type: void." : out.toString();

            if (output.length() > 1890)
                output = output.substring(0, 1890) + "...";

            message.getChannel().sendMessage("```xl\n" + output + "```").queue();
        } catch (ScriptException e) {
            message.getChannel().sendMessage("**ERROR:**\n```xl\n" + e.toString() + "```").queue();
        }

        return true;
    }

    private ScriptEngine buildScriptEngine(Message message) throws ScriptException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("nashorn");

        engine.put("message", message);
        engine.put("channel", message.getChannel());
        engine.put("jda", message.getJDA());
        engine.put("jager", jager);

        if (message.isFromType(ChannelType.TEXT)) {
            engine.put("guild", message.getGuild());
            engine.put("member", message.getMember());
        }

        engine.eval("var imports = new JavaImporter(" +
                "java.io," +
                "java.lang," +
                "java.util," +
                "Packages.net.dv8tion.jda.core," +
                "Packages.net.dv8tion.jda.core.entities," +
                "Packages.net.dv8tion.jda.core.entities.impl," +
                "Packages.net.dv8tion.jda.core.managers," +
                "Packages.net.dv8tion.jda.core.managers.impl," +
                "Packages.net.dv8tion.jda.core.utils);");

        return engine;
    }
}
