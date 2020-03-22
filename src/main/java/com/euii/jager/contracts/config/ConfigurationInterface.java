package com.euii.jager.contracts.config;

import java.io.IOException;

public interface ConfigurationInterface {

    CastableInterface load(String fileName, Class<?> type) throws IOException;

    String defaultConfig(String name);
}
