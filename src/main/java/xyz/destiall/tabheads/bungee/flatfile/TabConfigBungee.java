package xyz.destiall.tabheads.bungee.flatfile;

import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.YamlConfiguration;
import xyz.destiall.tabheads.bungee.TabheadsBungee;
import xyz.destiall.tabheads.core.TabConfig;
import xyz.destiall.tabheads.core.Tabheads;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

public class TabConfigBungee extends TabConfig {
    private Configuration config;
    public TabConfigBungee(File dataFolder) {
        super(dataFolder);
        reload();
        update();
    }

    @Override
    public void reload() {
        try {
            config = YamlConfiguration.getProvider(YamlConfiguration.class).load(configFile);
        } catch (Exception e) {
            Tabheads.get().getTabLogger().warning("Unable to load config.yml");
            e.printStackTrace();
        }
    }

    @Override
    public void save() {
        try (InputStream input = TabheadsBungee.INSTANCE.getResourceAsStream("config.yml");
            OutputStream output = Files.newOutputStream(configFile.toPath())) {
            ByteStreams.copy(input, output);
        } catch (Exception e) {
            Tabheads.get().getTabLogger().warning("Unable to save config.yml");
            e.printStackTrace();
        }
    }

    @Override
    public void update() {
        boolean update = false;
        if (!config.contains("settings.auto-name-change")) {
            config.set("settings.auto-name-change", false);
            update = true;
        }
        if (update) {
            try {
                YamlConfiguration.getProvider(YamlConfiguration.class).save(config, configFile);
            } catch (Exception e) {
                Tabheads.get().getTabLogger().warning("Unable to update config.yml");
                e.printStackTrace();
            }
        }
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
