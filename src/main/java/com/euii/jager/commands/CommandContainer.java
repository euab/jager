package com.euii.jager.commands;

import com.euii.jager.contracts.commands.AbstractCommand;

public class CommandContainer {

    public final AbstractCommand command;
    public final Category category;

    public CommandContainer(AbstractCommand command, Category category) {
        this.command = command;
        this.category = category;
    }

    public AbstractCommand getCommand() {
        return command;
    }

    public Category getCategory() {
        return category;
    }

    public String getDefaultPrefix() {
        // TODO: Get default prefix from config system or Category based context. This is just a stub.
        return "!";
    }
}
