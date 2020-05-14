package com.euii.jager.contracts.tasks;

import com.euii.jager.Jager;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public abstract class AbstractTask implements Runnable {

    protected final Jager jager;

    private final long delay;

    private final long period;

    private final TimeUnit unit;

    private final String uuid;

    public AbstractTask(Jager jager) {
        this(jager, 0);
    }

    public AbstractTask(Jager jager, long delay) {
        this(jager, delay, 1);
    }

    public AbstractTask(Jager jager, long delay, long period) {
        this(jager, delay, period, TimeUnit.MINUTES);
    }

    public AbstractTask(Jager jager, long delay, long period, TimeUnit unit) {
        this.jager = jager;
        this.delay = delay;
        this.period = period;
        this.unit = unit;
        this.uuid = Long.toHexString(System.nanoTime());
    }

    public long getDelay() {
        return delay;
    }

    public long getPeriod() {
        return period;
    }

    public TimeUnit getUnit() {
        return unit;
    }

    public String getUuid() {
        return Integer.toHexString(hashCode()) + uuid;
    }

    @Override
    public int hashCode() {
        return Objects.hash(jager, delay, period, unit, uuid);
    }
}
