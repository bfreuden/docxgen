package org.bfreuden.docxgen;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class Configuration {
    private final File configDir = new File(System.getProperty("user.home"), "AppData/Local/ConstatPhoto");
    private final File configFile = new File(configDir, "config.xml");
    private final Properties properties = new Properties();
    private static final Configuration INSTANCE = new Configuration();
    private boolean loaded = false;

    private Configuration() {
    }

    public static Configuration configuration() throws IOException {
        INSTANCE.maybeLoad();
        return INSTANCE;
    }

    private synchronized void maybeLoad() throws IOException {
        if (!this.loaded) {
            load();
            this.loaded = true;
        }
    }
    private synchronized void load() throws IOException {
        if (!configDir.exists())
            configDir.mkdirs();
        if (configFile.exists()) {
            try (var is = new FileInputStream(configFile)) {
                properties.loadFromXML(is);
            }
        }
    }

    private synchronized void save() throws IOException {
        if (!configDir.exists())
            configDir.mkdirs();
        try (var is = new FileOutputStream(configFile)) {
            properties.storeToXML(is, "Configuration de Constat Photo", StandardCharsets.UTF_8);
        }
    }

    public String getTemplate() {
        return getString("template");
    }

    public void setTemplate(String template) throws IOException {
        setString("template", template);
    }

    private synchronized String getString(String key) {
        return properties.getProperty(key);
    }

    private synchronized void setString(String key, String value) throws IOException {
        properties.put(key, value);
        save();
    }

    public void setLastPhotoDirectory(String lastPhotoDirectory) throws IOException {
        setString("lastPhotoDirectory", lastPhotoDirectory);
    }

    public String getLastPhotoDirectory() {
        return getString("lastPhotoDirectory");
    }
}
