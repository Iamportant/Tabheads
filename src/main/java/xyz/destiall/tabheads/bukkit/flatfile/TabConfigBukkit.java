package xyz.destiall.tabheads.bukkit.flatfile;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import xyz.destiall.tabheads.bukkit.TabheadsBukkit;
import xyz.destiall.tabheads.core.TabConfig;

import java.io.File;

public class TabConfigBukkit extends TabConfig {
    private FileConfiguration config;
    public TabConfigBukkit(File dataFolder) {
        super(dataFolder);
        reload();
        update();
    }

    @Override
    public void reload() {
        TabheadsBukkit.INSTANCE.reloadConfig();
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    @Override
    public void save() {
        TabheadsBukkit.INSTANCE.saveDefaultConfig();
    }

    @Override
    public void update() {
    }

    @Override
    public String getMessage(String key, String def) {
        return ChatColor.translateAlternateColorCodes('&', config.getString("message." + key, def));
    }

    @Override
    public boolean isAuthEnabled(String key) {
        return config.getBoolean("auth-hooks." + key, false);
    }

    @Override
    public boolean isSettingEnabled(String key) {
        return config.getBoolean("settings." + key, false);
    }
}
