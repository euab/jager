package com.euii.jager;

import com.euii.jager.commands.CommandHandler;
import com.euii.jager.commands.utility.InviteCommand;
import com.euii.jager.commands.utility.PingCommand;
import com.euii.jager.commands.utility.UptimeCommand;
import com.euii.jager.config.Configuration;
import com.euii.jager.config.ConfigurationLoader;
import com.euii.jager.contracts.handlers.EventHandler;
import com.euii.jager.handlers.EventTypes;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class Jager {

    public final Configuration config;

    private JDA jda;

    public Jager() throws IOException {
        System.out.println("Welcome to Jager rolling...");

        System.out.println("- Getting configuration");
        ConfigurationLoader configLoader = new ConfigurationLoader();
        this.config = (Configuration) configLoader.load("configuration.json", Configuration.class);

        if (this.config == null) {
            System.out.println("An error occurred whilst loading the configuration. Exiting program...");
            System.exit(0);
        }

        this.registerCommands();

        try {
            System.out.println("Building JDA...");
            jda = prepareJDA().build();
        } catch (LoginException e) {
            System.out.println("Something went wrong when connecting to Discord. Exiting program...");
            System.exit(0);
        }
    }

    private JDABuilder prepareJDA() {
        JDABuilder builder = new JDABuilder(AccountType.BOT).setToken(this.config.getBotAuth().getToken());

        Class[] eventArguments = new Class[1];
        eventArguments[0] = Jager.class;

        for (EventTypes event : EventTypes.values()) {
            try {
                Object instance = event.getInstance().getDeclaredConstructor(eventArguments).newInstance(this);

                if (instance instanceof EventHandler) {
                    builder.addEventListeners(instance);
                }
            } catch (InstantiationException | NoSuchMethodException | InvocationTargetException e) {
                System.out.println("Invalid listener object parsed, could not create a new instance.");
                System.out.println(e);
            } catch (IllegalAccessException e) {
                System.out.println("An attempt was made to register a handler called " + event + "but it failed.");
                System.out.println(e);
            }
        }

        return builder.setAutoReconnect(true);
    }

    private void registerCommands() {
        System.out.println("Registering commands...");

        CommandHandler.register(new PingCommand(this));
        CommandHandler.register(new InviteCommand(this));
        CommandHandler.register(new UptimeCommand(this));

        System.out.printf(" - Successfully registered %s commands\n", CommandHandler.getCommands().size());
    }

    public Configuration getConfig() {
        return config;
    }
}
