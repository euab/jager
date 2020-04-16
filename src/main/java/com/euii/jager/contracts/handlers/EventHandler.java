package com.euii.jager.contracts.handlers;

import com.euii.jager.Jager;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public abstract class EventHandler extends ListenerAdapter {

    protected final Jager jager;

    public EventHandler(Jager jager) {
        this.jager = jager;
    }
}
