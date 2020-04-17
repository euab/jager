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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class Jager {

    private static final Logger LOGGER = LoggerFactory.getLogger("Jager");

    public final Configuration config;

    private JDA jda;

    public Jager() throws IOException {
        System.out.println(getBanner());
        LOGGER.info("Welcome to Jager rolling...");

        LOGGER.info("- Getting configuration");
        ConfigurationLoader configLoader = new ConfigurationLoader();
        this.config = (Configuration) configLoader.load("configuration.json", Configuration.class);

        if (this.config == null) {
            LOGGER.error("An error occurred whilst loading the configuration. Exiting program...");
            System.exit(0);
        }

        this.registerCommands();

        try {
            LOGGER.info("Building JDA...");
            jda = prepareJDA().build();
        } catch (LoginException e) {
            LOGGER.error("Something went wrong when connecting to Discord. Exiting program...");
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
                LOGGER.warn("Invalid listener object parsed, could not create a new instance.");
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                LOGGER.warn("An attempt was made to register a handler called " + event + "but it failed.");
                e.printStackTrace();
            }
        }

        return builder.setAutoReconnect(true);
    }

    private void registerCommands() {
        LOGGER.info("Registering commands...");

        CommandHandler.register(new PingCommand(this));
        CommandHandler.register(new InviteCommand(this));
        CommandHandler.register(new UptimeCommand(this));

        LOGGER.info(String.format("- Registered %s commands.", CommandHandler.getCommands().size()));
    }

    private String getBanner() {
        return "\n\n" +
                "      _                         \n"+
                "     | | __ _  __ _  ___ _ __   \n"+
                "  _  | |/ _` |/ _` |/ _ \\ '__| \n"+
                " | |_| | (_| | (_| |  __/ |     \n"+
                "  \\___/ \\__,_|\\__, |\\___|_| \n"+
                "              |___/             \n\n"+
                "===============================";

    }

    public Configuration getConfig() {
        return config;
    }

    public Logger getLogger() {
        return LOGGER;
    }
}
