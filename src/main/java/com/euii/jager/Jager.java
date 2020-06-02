package com.euii.jager;

import com.euii.jager.api.Prometheus;
import com.euii.jager.commands.CommandHandler;
import com.euii.jager.commands.debug.EvalCommand;
import com.euii.jager.commands.music.*;
import com.euii.jager.commands.search.UrbanDictionaryCommand;
import com.euii.jager.commands.utility.*;
import com.euii.jager.config.Configuration;
import com.euii.jager.config.ConfigurationLoader;
import com.euii.jager.contracts.handlers.EventHandler;
import com.euii.jager.handlers.EventTypes;
import com.euii.jager.tasks.ActivityTask;
import com.euii.jager.tasks.AudioInactivityTask;
import com.euii.jager.tasks.TaskController;
import com.sedmelluq.discord.lavaplayer.tools.PlayerLibrary;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.JDAInfo;
import net.dv8tion.jda.api.entities.SelfUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class Jager {

    private static final Logger LOGGER = LoggerFactory.getLogger("Jager");

    public final Configuration config;
    private final Prometheus prometheus;

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
        this.registerTasks();

        LOGGER.info("Starting prometheus...");
        prometheus = new Prometheus(this);

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
        CommandHandler.register(new HelpCommand(this));
        CommandHandler.register(new UrbanDictionaryCommand(this));
        CommandHandler.register(new PlayCommand(this));
        CommandHandler.register(new SkipCommand(this));
        CommandHandler.register(new NowPlayingCommand(this));
        CommandHandler.register(new PauseCommand(this));
        CommandHandler.register(new ResumeCommand(this));
        CommandHandler.register(new SummonCommand(this));
        CommandHandler.register(new RepeatCommand(this));
        CommandHandler.register(new ClearQueueCommand(this));
        CommandHandler.register(new LeaveCommand(this));
        CommandHandler.register(new ShuffleCommand(this));
        CommandHandler.register(new DenverCommand(this));
        CommandHandler.register(new StatusCommand(this));
        CommandHandler.register(new EvalCommand(this));

        LOGGER.info(String.format("- Registered %s commands.", CommandHandler.getCommands().size()));
    }

    private void registerTasks() {
        LOGGER.info("Registering background tasks...");

        TaskController.registerTask(new ActivityTask(this));
        TaskController.registerTask(new AudioInactivityTask(this));

        LOGGER.info("Registered {} tasks", TaskController.entrySet().size());
    }

    private String getBanner() {
        return "\n\n" +
                "      _                         \n"+
                "     | | __ _  __ _  ___ _ __   \n"+
                "  _  | |/ _` |/ _` |/ _ \\ '__| \n"+
                " | |_| | (_| | (_| |  __/ |     \n"+
                "  \\___/ \\__,_|\\__, |\\___|_| \n"+
                "              |___/             \n\n"+
                "-------------------------------------------------"+
                "\n == Version information ==" +
                "\n Jager         : " + JagerInfo.getJagerInfo().version +
                "\n JVM           : " + System.getProperty("java.version") +
                "\n JDA           : " + JDAInfo.VERSION +
                "\n Lavaplayer    : " + PlayerLibrary.VERSION +
                "\n=================================================";

    }

    public Configuration getConfig() {
        return config;
    }

    public Logger getLogger() {
        return LOGGER;
    }

    public SelfUser getSelfUser() {
        return jda.getSelfUser();
    }

    public JDA getJda() {
        return jda;
    }

    public boolean isReady() {
        return jda.getStatus() == JDA.Status.CONNECTED;
    }
}
