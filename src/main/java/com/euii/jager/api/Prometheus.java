package com.euii.jager.api;

import ch.qos.logback.classic.LoggerContext;
import com.euii.jager.Jager;
import com.euii.jager.api.routes.GetMetrics;
import com.euii.jager.api.routes.GetStatus;
import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;
import io.prometheus.client.hotspot.DefaultExports;
import io.prometheus.client.logback.InstrumentedAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Spark;

public class Prometheus {

    public static Counter audioRequests = Counter.build()
            .name("jager_audio_requests")
            .help("The amount of audio requests processed by the bot.")
            .register();

    public static Counter tracksLoaded = Counter.build()
            .name("jager_audio_tracks_loaded")
            .help("The amount of tracks that Jager has loaded into Lavaplayer")
            .register();

    public static Counter trackLoadedFailures = Counter.build()
            .name("jager_audio_track_load_failures")
            .help("The amount of times that a track has failed to load successfully.")
            .register();

    public static Counter processedCommands = Counter.build()
            .name("jager_processed_commands")
            .help("The amount of commands Jager has processed.")
            .labelNames("class")
            .register();

    public static Histogram executionTime = Histogram.build()
            .name("jager_command_execution_time")
            .help("The execution time for commands.")
            .labelNames("class")
            .register();

    public static Counter commandFailures = Counter.build()
            .name("jager_command_uncaught_exceptions")
            .help("The amount of execeptions that Jager was not prepared to handle.")
            .labelNames("class")
            .register();

    public static final Logger LOGGER = LoggerFactory.getLogger(Prometheus.class);
    private static final int PORT = 9091;
    private final Jager jager;

    public Prometheus(Jager jager) {
        this.jager = jager;

        final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        final ch.qos.logback.classic.Logger root = context.getLogger(Logger.ROOT_LOGGER_NAME);
        final InstrumentedAppender prometheusAppender = new InstrumentedAppender();
        prometheusAppender.setContext(root.getLoggerContext());
        prometheusAppender.start();
        root.addAppender(prometheusAppender);

        DefaultExports.initialize();

        LOGGER.info("Starting metrics API on port: " + PORT);

        Spark.port(PORT);

        Spark.get("api/v1/metrics", new GetMetrics(this));
        Spark.get("api/v1/status", new GetStatus(this));
    }

    public Jager getJager() {
        return jager;
    }
}
