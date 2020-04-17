package com.euii.jager.commands.utility;

import com.euii.jager.Jager;
import com.euii.jager.contracts.commands.AbstractCommand;
import com.euii.jager.factories.MessageFactory;
import com.euii.jager.utilities.Time;
import net.dv8tion.jda.api.entities.Message;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Collections;
import java.util.List;

public class UptimeCommand extends AbstractCommand {

    public UptimeCommand(Jager jager) {
        super(jager);
    }

    @Override
    public String getName() {
        return "Uptime Command";
    }

    @Override
    public String getDescription() {
        return "Displays how long the bot has been running for alongside other relevant information.";
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
        return Collections.singletonList("uptime");
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        long uptime = runtimeMXBean.getUptime();
        String humanUptime = Time.makeHumanReadableTime(uptime);

        MessageFactory.makeInfo(message, ":stopwatch: " + humanUptime).queue();

        return true;
    }
}
