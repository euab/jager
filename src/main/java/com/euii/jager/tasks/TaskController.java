package com.euii.jager.tasks;

import com.euii.jager.contracts.tasks.AbstractTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

public class TaskController {

    private static final Map<String, ScheduledFuture<?>> TASKS = new HashMap<>();
    private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(1);

    public static String registerTask(AbstractTask task) {
        TASKS.put(task.getUuid(), SCHEDULER.scheduleAtFixedRate(task, task.getDelay(), task.getPeriod(), task.getUnit()));
        return task.getUuid();
    }

    public static ScheduledFuture<?> getScheduledFuture(String taskId) {
        return TASKS.getOrDefault(taskId, null);
    }

    public static Set<Map.Entry<String, ScheduledFuture<?>>> entrySet() {
        return TASKS.entrySet();
    }
}
