package com.euii.jager.commands.utility;

import com.euii.jager.Jager;
import com.euii.jager.JagerInfo;
import com.euii.jager.api.Prometheus;
import com.euii.jager.audio.AudioHandler;
import com.euii.jager.contracts.commands.AbstractCommand;
import com.euii.jager.factories.MessageFactory;
import com.euii.jager.utilities.Numbers;
import com.euii.jager.utilities.Time;
import io.prometheus.client.Collector;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Arrays;
import java.util.List;

public class StatusCommand extends AbstractCommand {

    public StatusCommand(Jager jager) {
        super(jager);
    }

    @Override
    public String getName() {
        return "Status";
    }

    @Override
    public String getDescription() {
        return "Get the current status and metrics of the bot.";
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
        return Arrays.asList("status", "stats", "about", "bot");
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        MessageEmbed embed = MessageFactory.createEmbeddedBuilder()
                .setColor(MessageFactory.MessagePriority.INFO.getColour())
                .setTitle("Current status")
                .setAuthor("Jager v" + JagerInfo.getJagerInfo().version,
                        null, jager.getSelfUser().getEffectiveAvatarUrl())
                .addField("Author", "Euab#3685", true)
                .addField("Uptime", getUptime(), true)
                .addField("Ping", getPing(message), true)
                .addField("Memory", getMemoryUsage(), true)
                .addField("Commands", getExecutedCommands(), true)
                .addField("Players", Numbers.formatValue(AudioHandler.getControllerMapSize()), true)
                .addField("Servers", getGuilds(), true)
                .addField("Channels", getChannels(), true)
                .addField("Users", getUsers(), true)
                .build();

        message.getChannel().sendMessage(embed).queue();

        return true;
    }

    private String getExecutedCommands() {
        return formatMetrics(getMetricsByFamily(Prometheus.processedCommands.collect()));
    }

    private String getUptime() {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        long uptime = runtimeMXBean.getUptime();

        return Time.makeTimestampedUptime(uptime);
    }

    private String getPing(Message message) {
        return String.format("%sms", message.getJDA().getGatewayPing());
    }

    private String getMemoryUsage() {
        return String.format("%sMB/%sMB",
                (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024),
                Runtime.getRuntime().totalMemory() / (1024 * 1024)
        );
    }

    private int getMetricsByFamily(List<Collector.MetricFamilySamples> familySamples) {
        double total = 0.0D;
        for (Collector.MetricFamilySamples family : familySamples) {
            for (Collector.MetricFamilySamples.Sample sample : family.samples)
                total += sample.value;
        }

        return (int) total;
    }

    private String formatMetrics(int n) {
        double value = n / ((double) ManagementFactory.getRuntimeMXBean().getUptime() / 1000D);
        return String.format(
                value < 1.5D ? "%s (%s cmds/m)" : "%s (%s cmds/s)",
                Numbers.formatValue(n),
                Numbers.formatDecimals(value < 1.5D ? value * 60D : value)
        );
    }

    private String getGuilds() {
        return String.format(
                "%s servers",
                jager.getJda().getGuilds().size()
        );
    }

    private String getChannels() {
        return String.format(
                "%s channels",
                jager.getJda().getTextChannels().size()
        );
    }

    private String getUsers() {
        return String.format(
                "%s users",
                jager.getJda().getUsers().size()
        );
    }
}
