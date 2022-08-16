package xyz.destiall.tabheads.core;

import java.io.File;

public abstract class TabConfig {
    protected File configFile;
    public TabConfig(File dataFolder) {
        configFile = new File(dataFolder, "config.yml");
        if (!configFile.exists()) {
            save();
        }
    }

    public abstract void reload();

    public abstract void save();

    public abstract void update();

    public String getMessage(String key) {
        return getMessage(key, null);
    }

    public abstract String getMessage(String key, String def);

    public abstract boolean isAuthEnabled(String key);

    public abstract boolean isSettingEnabled(String key);
}
