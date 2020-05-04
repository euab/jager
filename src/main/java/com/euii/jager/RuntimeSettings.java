package com.euii.jager;

import org.apache.commons.cli.CommandLine;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Arrays;
import java.util.List;

public class RuntimeSettings {

    private final boolean useEnvironmentVariables;

    private final List<String> jarArguments;
    private final List<String> runtimeArguments;

    RuntimeSettings(CommandLine commandLine, String[] args) {
        useEnvironmentVariables = commandLine.hasOption("--use-env");

        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        this.runtimeArguments = runtimeMXBean.getInputArguments();
        this.jarArguments = Arrays.asList(args);
    }

    public boolean isUseEnvironmentVariables() {
        return useEnvironmentVariables;
    }

    public List<String> getRuntimeArguments() {
        return runtimeArguments;
    }
}
