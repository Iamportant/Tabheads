package xyz.destiall.tabheads.bukkit;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.github.games647.craftapi.resolver.MojangResolver;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.destiall.tabheads.bukkit.commands.Premium;
import xyz.destiall.tabheads.bukkit.commands.Reload;
import xyz.destiall.tabheads.bukkit.commands.Unpremium;
import xyz.destiall.tabheads.bukkit.flatfile.PremiumManagerBukkit;
import xyz.destiall.tabheads.bukkit.flatfile.TabConfigBukkit;
import xyz.destiall.tabheads.bukkit.listener.ConnectListener;
import xyz.destiall.tabheads.bukkit.listener.ProtocolListener;
import xyz.destiall.tabheads.bukkit.session.BukkitLoginSession;
import xyz.destiall.tabheads.bukkit.utils.CommonUtil;
import xyz.destiall.tabheads.core.LoginSession;
import xyz.destiall.tabheads.core.PremiumManager;
import xyz.destiall.tabheads.core.TabConfig;
import xyz.destiall.tabheads.core.Tabheads;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentMap;

public final class TabheadsBukkit extends JavaPlugin implements Tabheads<InetSocketAddress> {
    public static ProtocolManager PROTOCOL;
    public static TabheadsBukkit INSTANCE;
    private final ConcurrentMap<String, BukkitLoginSession> loginSession = CommonUtil.buildCache(1, -1);
    private final ConcurrentMap<String, Object> pendingLogin = CommonUtil.buildCache(5, -1);
    private MojangResolver resolver;
    private PremiumManager premiumManager;
    private TabConfig config;

    @Override
    public void onLoad() {
        INSTANCE = this;
        Tabheads.setInstance(this);
        PROTOCOL = ProtocolLibrary.getProtocolManager();
        resolver = new MojangResolver();
        getDataFolder().mkdir();
        try {
            config = new TabConfigBukkit(getDataFolder());
        } catch (Exception ignored) {
            getLogger().severe("Unable to load configuration");
        }
        premiumManager = new PremiumManagerBukkit();
    }

    @Override
    public void onEnable() {
        if (getServer().getOnlineMode()) {
            getLogger().warning("Server is in online mode! This plugin is useless! Disabling...");
            getServer().getPluginManager().disablePlugin(this);
            setEnabled(false);
            return;
        }
        if (isBungeecord()) {
            getLogger().warning("BungeeCord is enabled! Will not enable this plugin! Use this on BungeeCord instead!");
            getServer().getPluginManager().disablePlugin(this);
            setEnabled(false);
            return;
        }
        ProtocolListener.register();
        ConnectListener.register();
        getServer().getPluginCommand("premium").setExecutor(new Premium());
        getServer().getPluginCommand("unpremium").setExecutor(new Unpremium());
        getServer().getPluginCommand("tabheadsreload").setExecutor(new Reload());
        getServer().getMessenger().registerOutgoingPluginChannel(this, "tabheads:premium");
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
    }

    public boolean isBungeecord() {
        try {
            Class.forName("org.bukkit.Server.Spigot");
        } catch (Exception e) {
            return false;
        }
        return getServer().spigot().getConfig().getBoolean("settings.bungeecord", false);
    }

    @Override
    public void onDisable() {
        loginSession.clear();
        pendingLogin.clear();
        if (!getServer().getOnlineMode() && !isBungeecord()) {
            ProtocolListener.unregister();
            ConnectListener.unregister();
            getServer().getPluginCommand("premium").setExecutor(null);
            getServer().getPluginCommand("unpremium").setExecutor(null);
            getServer().getPluginCommand("tabheadsreload").setExecutor(null);
        }
        Tabheads.setInstance(null);
    }

    @Override
    public BukkitLoginSession getSession(InetSocketAddress addr) {
        String id = getSessionId(addr);
        return loginSession.get(id);
    }

    @Override
    public String getSessionId(InetSocketAddress addr) {
        return addr.getAddress().getHostAddress() + ':' + addr.getPort();
    }

    @Override
    public void putSession(InetSocketAddress addr, LoginSession session) {
        String id = getSessionId(addr);
        loginSession.put(id, (BukkitLoginSession) session);
    }

    @Override
    public void removeSession(InetSocketAddress addr) {
        String id = getSessionId(addr);
        loginSession.remove(id);
    }

    @Override
    public MojangResolver getResolver() {
        return resolver;
    }

    @Override
    public ConcurrentMap<String, Object> getPendingLogin() {
        return pendingLogin;
    }

    @Override
    public String getPluginPath() {
        return getDataFolder().getAbsolutePath();
    }

    @Override
    public PremiumManager getPremiumManager() {
        return premiumManager;
    }

    @Override
    public TabConfig getTabConfig() {
        return config;
    }
}
