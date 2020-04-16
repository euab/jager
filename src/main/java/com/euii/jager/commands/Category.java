package com.euii.jager.commands;

import com.euii.jager.contracts.commands.AbstractCommand;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public enum Category {

    //NOTE: More categories will be added over time but the layout for an extension of categories is in place here.

    UTILITY("Utility");

    private static final List<Category> VALUES = Collections.unmodifiableList(Arrays.asList(values()));
    private static final Random RANDOM = new Random();

    private final String name;

    Category(String name) {
        this.name = name;
    }

    public static Category fromCommand(AbstractCommand command) {
        String commandPackage = command.getClass().getName().split("\\.")[4];

        for (Category category : Category.values()) {
            if (category.toString().equalsIgnoreCase(commandPackage))
                return category;
        }

        return null;
    }

    public static Category fromLazyName(String name) {
        name = name.toLowerCase();

        for (Category category : values()) {
            if (category.getName().toLowerCase().startsWith(name))
                return category;
        }

        return null;
    }

    public static Category random() {
        return VALUES.get(RANDOM.nextInt(VALUES.size()));
    }

    public String getName() {
        return name;
    }
}
