package com.euii.jager;

import com.euii.jager.config.Configuration;
import com.euii.jager.config.ConfigurationLoader;
import com.euii.jager.listeners.MessageListener;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

import javax.security.auth.login.LoginException;
import java.io.IOException;

public class Jager {

    public final Configuration config;

    private JDA jda;

    public Jager() throws IOException {
        ConfigurationLoader configLoader = new ConfigurationLoader();
        this.config = (Configuration) configLoader.load("configuration.json", Configuration.class);

        if (this.config == null) {
            System.out.println("An error occurred whilst loading the configuration. Exiting program...");
            System.exit(0);
        }

        try {
            jda = prepareJDA().build();
        } catch (LoginException e) {
            System.out.println("Something went wrong when connecting to Discord. Exiting program...");
            System.exit(0);
        }
    }

    private JDABuilder prepareJDA() {
        JDABuilder builder = new JDABuilder(AccountType.BOT).setToken(this.config.getBotAuth().getToken());
        builder.addEventListeners(new MessageListener());

        return builder.setAutoReconnect(true);
    }

    public static void main(String[] args) throws IOException {
        new Jager();
    }
}
