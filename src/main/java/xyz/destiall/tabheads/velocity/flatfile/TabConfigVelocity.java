package xyz.destiall.tabheads.velocity.flatfile;

import com.google.common.io.ByteStreams;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;
import xyz.destiall.tabheads.core.TabConfig;
import xyz.destiall.tabheads.core.Tabheads;
import xyz.destiall.tabheads.velocity.TabheadsVelocity;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class TabConfigVelocity extends TabConfig {
    private ConfigurationNode config;
    private final YamlConfigurationLoader loader;

    public TabConfigVelocity(Path dataFolder) {
        super(dataFolder.toFile());
        loader = YamlConfigurationLoader.builder().path(configFile.toPath()).build();
        reload();
        update();
    }

    @Override
    public void reload() {
        try {
            config = loader.load();
        } catch (Exception e) {
            Tabheads.get().getTabLogger().warning("Unable to load config.yml");
            e.printStackTrace();
        }
    }

    @Override
    public void save() {
        try (InputStream input = TabheadsVelocity.INSTANCE.getResourceAsStream("config.yml");
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
        if (!config.hasChild("settings", "auto-name-change")) {
            try {
                config.node("settings", "auto-name-change").set(false);
            } catch (SerializationException e) {
                e.printStackTrace();
            }
            update = true;
        }
        if (update) {
            save();
        }
    }

    @Override
    public String getMessage(String key, String def) {
        return config.node("message", key).getString(def);
    }

    @Override
    public boolean isAuthEnabled(String key) {
        return config.node("auth-hooks", key).getBoolean();
    }

    @Override
    public boolean isSettingEnabled(String key) {
        return config.node("settings", key).getBoolean();
    }
}
