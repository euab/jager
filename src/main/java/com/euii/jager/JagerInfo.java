package com.euii.jager;

import java.io.IOException;
import java.util.Properties;

public class JagerInfo {
    protected final Properties properties = new Properties();

    private static JagerInfo instance;

    public final String version;
    public final String groupId;
    public final String artifactId;

    private JagerInfo() throws IOException {
        loadProperty(getClass().getClassLoader());

        this.version = properties.getProperty("version");
        this.groupId = properties.getProperty("groupId");
        this.artifactId = properties.getProperty("artifactId");
    }

    public static JagerInfo getJagerInfo() {
        if (instance == null) {
            try {
                instance = new JagerInfo();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return instance;
    }

    protected void loadProperty(ClassLoader classLoader) {
        try {
            properties.load(
                    classLoader.getResourceAsStream("jager.properties")
            );
        } catch (Exception e) {
            System.out.println("Failed to load properties file. " + "jager.properties" + " " + e);
        }
    }
}
