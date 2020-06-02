package com.euii.jager.middleware;

import com.euii.jager.Jager;
import com.euii.jager.commands.CommandContainer;
import com.euii.jager.contracts.commands.AbstractCommand;
import com.euii.jager.contracts.middleware.AbstractMiddleware;
import net.dv8tion.jda.api.entities.Message;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.atomic.AtomicReference;

public class MiddlewareStack {

    private final Jager jager;
    private final Message message;
    private final CommandContainer command;
    private final List<MiddlewareContainer> middlewares = new ArrayList<>();

    private int index = -1;

    public MiddlewareStack(Jager jager, Message message, CommandContainer command) {
        this.jager = jager;
        this.message = message;
        this.command = command;

        middlewares.add(new MiddlewareContainer(Middleware.PROCESS_COMMAND));
        this.buildMiddlewareStack();
    }

    private void buildMiddlewareStack() {
        List<String> middleware = command.getCommand().getMiddleware();
        if (middleware.isEmpty()) {
            return;
        }

        ListIterator middlewareIterator = middleware.listIterator(middleware.size());
        while (middlewareIterator.hasPrevious()) {
            String previous = (String) middlewareIterator.previous();
            String[] split = previous.split(":");

            AtomicReference<Middleware> middlewareAtomicReference = new AtomicReference<>(Middleware
                    .fromName(split[0]));
            if (middlewareAtomicReference.get() == null)
                continue;

            if (split.length == 1) {
                middlewares.add(new MiddlewareContainer(middlewareAtomicReference.get()));
                continue;
            }

            middlewares.add(new MiddlewareContainer(middlewareAtomicReference.get(), split[1].split(",")));
        }
    }

    public boolean next() {
        if (index == -1)
            index = middlewares.size();

        Class[] arguments = new Class[1];
        arguments[0] = Jager.class;

        try {
            MiddlewareContainer middlewareContainer = middlewares.get(--index);
            AbstractMiddleware middleware = (AbstractMiddleware) middlewareContainer
                    .getMiddleware()
                    .getInstance()
                    .getDeclaredConstructor(arguments)
                    .newInstance(jager);

            return middleware.handle(message, this, middlewareContainer.getArguments());

        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            System.out.println("Invalid middleware object parsed, failed to create a new middleware instance.");
            System.out.println(e);
        } catch (IllegalAccessException e) {
            System.out.println("An attempt was made to make a new middleware instance.");
            System.out.println(e);
        }

        return false;
    }

    public AbstractCommand getCommand() {
        return command.getCommand();
    }

    public CommandContainer getCommandContainer() {
        return command;
    }
}
