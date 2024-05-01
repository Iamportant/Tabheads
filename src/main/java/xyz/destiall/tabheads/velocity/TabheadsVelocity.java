package xyz.destiall.tabheads.velocity;

import com.github.games647.craftapi.resolver.MojangResolver;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;
import xyz.destiall.tabheads.SLF4JLogger;
import xyz.destiall.tabheads.core.LoginSession;
import xyz.destiall.tabheads.core.PremiumManager;
import xyz.destiall.tabheads.core.TabConfig;
import xyz.destiall.tabheads.core.TabLogger;
import xyz.destiall.tabheads.core.Tabheads;
import xyz.destiall.tabheads.velocity.auth.AuthMeVelocityHook;
import xyz.destiall.tabheads.velocity.commands.Premium;
import xyz.destiall.tabheads.velocity.commands.Reload;
import xyz.destiall.tabheads.velocity.commands.Unpremium;
import xyz.destiall.tabheads.velocity.flatfile.PremiumManagerVelocity;
import xyz.destiall.tabheads.velocity.flatfile.TabConfigVelocity;
import xyz.destiall.tabheads.velocity.listener.VelocityConnectListener;
import xyz.destiall.tabheads.velocity.session.VelocityLoginSession;

import java.io.File;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

@Plugin(id = "tabheadsvelocity", name = "Tabheads", version = "2.1",
        url = "https://www.destial.xyz", description = "Tabheads", authors = {"destiall"})
public class TabheadsVelocity implements Tabheads<InetSocketAddress> {

    public static TabheadsVelocity INSTANCE;
    private final ProxyServer server;
    private final TabLogger logger;
    private final Path dataDirectory;

    private LoadingCache<InetSocketAddress, VelocityLoginSession> loginSession;
    private PremiumManager premiumManager;
    private TabConfig config;
    public boolean AMB;

    private MojangResolver resolver;

    @Inject
    public TabheadsVelocity(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = new SLF4JLogger(logger);
        this.dataDirectory = dataDirectory;
    }

    public ProxyServer getServer() {
        return server;
    }

    public File getDataFolder() {
        return dataDirectory.toFile();
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent ignored) {
        INSTANCE = this;
        Tabheads.setInstance(this);
        resolver = new MojangResolver();
        loginSession = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).build(CacheLoader.from(() -> {
            throw new UnsupportedOperationException();
        }));
        getDataFolder().mkdir();
        try {
            config = new TabConfigVelocity(dataDirectory);
        } catch (Exception ex) {
            getTabLogger().warning("Unable to load configuration");
            ex.printStackTrace();
        }
        premiumManager = new PremiumManagerVelocity(this);

        AMB = server.getPluginManager().getPlugin("authmevelocity").isPresent();
        if (AMB) {
            AuthMeVelocityHook.setup(server);
        }

        server.getEventManager().register(this, new VelocityConnectListener(this));
        CommandManager commandManager = server.getCommandManager();
        CommandMeta premium = commandManager
                .metaBuilder("premium")
                .aliases("legit")
                .plugin(this)
                .build();

        CommandMeta unpremium = commandManager
                .metaBuilder("unpremium")
                .aliases("cracked")
                .plugin(this)
                .build();

        CommandMeta reload = commandManager
                .metaBuilder("reload")
                .plugin(this)
                .build();

        commandManager.register(premium, new Premium());
        commandManager.register(unpremium, new Unpremium());
        commandManager.register(reload, new Reload());
    }

    @Override
    public MojangResolver getResolver() {
        return resolver;
    }

    @Override
    public VelocityLoginSession getSession(InetSocketAddress key) {
        return loginSession.getUnchecked(key);
    }

    @Override
    public String getSessionId(InetSocketAddress key) {
        return key.getAddress().getHostAddress();
    }

    @Override
    public void putSession(InetSocketAddress key, LoginSession session) {
        loginSession.put(key, (VelocityLoginSession) session);
    }

    @Override
    public void removeSession(InetSocketAddress key) {
        loginSession.invalidate(key);
    }

    public void removeSession(Player player) {
        InetSocketAddress e = loginSession.asMap().keySet().stream().filter(l -> l.equals(player.getRemoteAddress())).findFirst().orElse(null);
        if (e == null)
            return;

        removeSession(e);
    }

    @Override
    public ConcurrentMap<String, Object> getPendingLogin() {
        return null;
    }

    @Override
    public TabLogger getTabLogger() {
        return logger;
    }

    @Override
    public String getPluginPath() {
        return getDataFolder().getPath();
    }

    @Override
    public PremiumManager getPremiumManager() {
        return premiumManager;
    }

    @Override
    public TabConfig getTabConfig() {
        return config;
    }

    public InputStream getResourceAsStream(String s) {
        return getClass().getClassLoader().getResourceAsStream(s);
    }
}
