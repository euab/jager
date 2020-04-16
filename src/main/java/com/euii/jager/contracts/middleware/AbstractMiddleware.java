package com.euii.jager.contracts.middleware;

import com.euii.jager.Jager;
import com.euii.jager.middleware.MiddlewareStack;
import net.dv8tion.jda.api.entities.Message;

public abstract class AbstractMiddleware {

    protected final Jager jager;

    public AbstractMiddleware(Jager jager) {
        this.jager = jager;
    }

    public abstract boolean handle(Message message, MiddlewareStack stack, String... args);
}
