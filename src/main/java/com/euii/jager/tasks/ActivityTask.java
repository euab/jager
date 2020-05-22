package com.euii.jager.tasks;

import com.euii.jager.Jager;
import com.euii.jager.contracts.tasks.AbstractTask;
import net.dv8tion.jda.api.entities.Activity;

import java.util.Arrays;

public class ActivityTask extends AbstractTask {

    private int index = 0;

    public ActivityTask(Jager jager) {
        super(jager, 1);
    }

    @Override
    public void run() {

        jager.getLogger().debug("[BG task] ActivityTask running now. UUID: {}", getUuid());

        if (!jager.isReady())
            return;

        if (index >= jager.getConfig().getActivities().size())
            index = 0;

        jager.getJda().getPresence().setActivity(
                getActivityByType(jager.getConfig().getActivities().get(index))
        );

        index++;
    }

    private Activity getActivityByType(String status) {
        Activity activity = Activity.playing(status);
        if (status.contains(":")) {
            String[] split = status.split(":");
            status = String.join(":", Arrays.copyOfRange(split, 1, split.length));

            switch (split[0].toLowerCase()) {
                case "playing":
                    return Activity.playing(formatActivity(status));

                case "listening":
                    return Activity.listening(formatActivity(status));

                case "watching":
                    return Activity.watching(formatActivity(status));

                case "streaming":
                    return Activity.streaming(formatActivity(status), "https://www.twitch.tv/euiiontwitch");
            }
        }

        return activity;
    }

    private String formatActivity(String status) {
        status = status.replaceAll("%users%", "" + jager.getJda().getUsers().size());
        status = status.replaceAll("%guilds%", "" + jager.getJda().getGuilds().size());

        return status;
    }
}
