package xyz.destiall.tabheads.bungee;

import com.github.games647.craftapi.resolver.MojangResolver;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.plugin.Plugin;
import xyz.destiall.tabheads.bungee.auth.AuthMeBungeeHook;
import xyz.destiall.tabheads.bungee.commands.Premium;
import xyz.destiall.tabheads.bungee.commands.Reload;
import xyz.destiall.tabheads.bungee.commands.Unpremium;
import xyz.destiall.tabheads.bungee.flatfile.PremiumManagerBungee;
import xyz.destiall.tabheads.bungee.flatfile.TabConfigBungee;
import xyz.destiall.tabheads.bungee.listener.ConnectListener;
import xyz.destiall.tabheads.bungee.session.BungeeLoginSession;
import xyz.destiall.tabheads.core.LoginSession;
import xyz.destiall.tabheads.core.PremiumManager;
import xyz.destiall.tabheads.core.TabConfig;
import xyz.destiall.tabheads.core.Tabheads;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

public final class TabheadsBungee extends Plugin implements Tabheads<PendingConnection> {
    public static TabheadsBungee INSTANCE;
    private MojangResolver resolver;
    private LoadingCache<PendingConnection, BungeeLoginSession> loginSession;
    private PremiumManager premiumManager;
    private TabConfig config;
    public boolean AMB;

    @Override
    public void onLoad() {
        INSTANCE = this;
        Tabheads.setInstance(this);
        resolver = new MojangResolver();
        loginSession = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).build(CacheLoader.from(() -> {
            throw new UnsupportedOperationException();
        }));
        getDataFolder().mkdir();
        try {
            config = new TabConfigBungee(getDataFolder());
        } catch (Exception ex) {
            getLogger().warning("Unable to load configuration");
            ex.printStackTrace();
        }
        premiumManager = new PremiumManagerBungee();
    }

    @Override
    public void onEnable() {
        getProxy().getPluginManager().registerListener(this, new ConnectListener());
        getProxy().getPluginManager().registerCommand(this, new Premium());
        getProxy().getPluginManager().registerCommand(this, new Unpremium());
        getProxy().getPluginManager().registerCommand(this, new Reload());
        AMB = ProxyServer.getInstance().getPluginManager().getPlugin("AuthMeBungee") != null;
        if (AMB) {
            AuthMeBungeeHook.setup();
        }
    }

    @Override
    public MojangResolver getResolver() {
        return resolver;
    }

    @Override
    public BungeeLoginSession getSession(PendingConnection key) {
        return loginSession.getUnchecked(key);
    }

    @Override
    public String getSessionId(PendingConnection key) {
        return key.getAddress().getAddress().getHostAddress()+":"+key.getName();
    }

    @Override
    public void putSession(PendingConnection addr, LoginSession session) {
        loginSession.put(addr, (BungeeLoginSession) session);
    }

    public void removeSession(PendingConnection con) {
        loginSession.invalidate(con);
    }

    @Override
    public void onDisable() {
        getProxy().getPluginManager().unregisterListeners(this);
        getProxy().getPluginManager().unregisterCommands(this);
        loginSession.invalidateAll();
        Tabheads.setInstance(null);
    }

    @Override
    public ConcurrentMap<String, Object> getPendingLogin() {
        return null;
    }

    @Override
    public String getPluginPath() {
        return getDataFolder().getAbsolutePath();
    }

    public PremiumManager getPremiumManager() {
        return premiumManager;
    }

    @Override
    public TabConfig getTabConfig() {
        return config;
    }
}
