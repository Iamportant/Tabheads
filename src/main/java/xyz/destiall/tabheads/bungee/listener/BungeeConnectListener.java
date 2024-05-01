package xyz.destiall.tabheads.bungee.listener;

import com.github.games647.craftapi.UUIDAdapter;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import xyz.destiall.tabheads.bungee.TabheadsBungee;
import xyz.destiall.tabheads.bungee.task.BungeeAsyncPremiumCheck;
import xyz.destiall.tabheads.core.PremiumManager;
import xyz.destiall.tabheads.core.RateLimiter;
import xyz.destiall.tabheads.core.Tabheads;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class BungeeConnectListener implements Listener {
    private static final String UUID_FIELD_NAME = "uniqueId";
    private static final MethodHandle uniqueIdSetter;
    private final RateLimiter rateLimiter;

    static {
        MethodHandle setHandle = null;
        try {
            Lookup lookup = MethodHandles.lookup();
            Class.forName("net.md_5.bungee.connection.InitialHandler");
            Field uuidField = InitialHandler.class.getDeclaredField(UUID_FIELD_NAME);
            uuidField.setAccessible(true);
            setHandle = lookup.unreflectSetter(uuidField);
        } catch (Exception e) {
            TabheadsBungee.INSTANCE.getLogger().warning("Cannot find Bungee initial handler; Disabling premium UUID and skin won't work.");
            e.printStackTrace();
        }
        uniqueIdSetter = setHandle;
    }

    static {
        try {
            Lookup lookup = MethodHandles.lookup();
            Class.forName("net.md_5.bungee.connection.InitialHandler");
            Field uuidField = InitialHandler.class.getDeclaredField(UUID_FIELD_NAME);
            uuidField.setAccessible(true);
            lookup.unreflectSetter(uuidField);
        } catch (ClassNotFoundException classNotFoundException) {
            TabheadsBungee.INSTANCE.getLogger().warning("Cannot find Bungee initial handler; Disabling premium UUID and skin won't work.");
            classNotFoundException.printStackTrace();
        } catch (ReflectiveOperationException reflectiveOperationException) {
            reflectiveOperationException.printStackTrace();
        }

    }
    public BungeeConnectListener() {
        this.rateLimiter = Tabheads.RATE_LIMITER;
    }

    @EventHandler
    public void onPreLogin(PreLoginEvent preLoginEvent) {
        PendingConnection connection = preLoginEvent.getConnection();
        if (preLoginEvent.isCancelled()) {
            return;
        }
        if (!rateLimiter.tryAcquire()) {
            Tabheads.get().getTabLogger().warning("Anti-Bot join limit - Ignoring " + connection);
            return;
        }
        String username = connection.getName();
        preLoginEvent.registerIntent(TabheadsBungee.INSTANCE);
        Runnable asyncPremiumCheck = new BungeeAsyncPremiumCheck(preLoginEvent, connection, username);
        ProxyServer.getInstance().getScheduler().runAsync(TabheadsBungee.INSTANCE, asyncPremiumCheck);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLogin(LoginEvent e) {
        if (e.isCancelled()) return;
        PendingConnection connection = e.getConnection();
        if (connection.isOnlineMode()) {
            if (uniqueIdSetter != null) {
                InitialHandler initialHandler = (InitialHandler) connection;
                UUID offline = UUIDAdapter.generateOfflineId(e.getConnection().getName());
                if (Tabheads.get().getTabConfig().isSettingEnabled("auto-name-change")) {
                    offline = TabheadsBungee.INSTANCE.getSession(connection).getOffline();
                }
                setOfflineId(initialHandler, offline);
            }
        }
    }

    private void setOfflineId(InitialHandler connection, UUID offline) {
        try {
            uniqueIdSetter.invokeExact(connection, offline);
            if (Tabheads.get().getTabConfig().isSettingEnabled("debug")) {
                Tabheads.get().getTabLogger().info("Setting uuid of player " + connection.getName() + " to " + offline);
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent disconnectEvent) {
        ProxiedPlayer player = disconnectEvent.getPlayer();
        TabheadsBungee.INSTANCE.removeSession(player.getPendingConnection());
    }

    @EventHandler
    public void onConnect(ServerConnectedEvent e) {
        ProxiedPlayer player = e.getPlayer();
        PremiumManager pm = Tabheads.get().getPremiumManager();
        if (pm.isPending(player.getName())) {
            pm.saveProfile(player.getName());
            pm.removePending(player.getName());
            forceLogin(player);
            return;
        } else if (pm.isUnconfirmed(player.getName())) {
            pm.removeUnconfirmed(player.getName());
            ProxyServer.getInstance().getScheduler().schedule(TabheadsBungee.INSTANCE, () ->
                player.sendMessage(TextComponent.fromLegacyText(Tabheads.get().getTabConfig().getMessage("unconfirmed", "&cYou did not log in time! Perhaps you were trying but you kept getting kicked? Then that means you are not premium!")))
            , 2L, TimeUnit.SECONDS);
            return;
        }
        if (player.getPendingConnection().isOnlineMode()) {
            forceLogin(player);
        }
    }

    private void forceLogin(ProxiedPlayer player) {
        ProxyServer.getInstance().getScheduler().schedule(TabheadsBungee.INSTANCE, () -> {
            player.sendMessage(TextComponent.fromLegacyText(Tabheads.get().getTabConfig().getMessage("premium-login", "&aYou have successfully logged in as a premium player!")));
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF(player.getName());
            player.getServer().sendData("tabheads:premium", out.toByteArray());
            if (Tabheads.get().getTabConfig().isAuthEnabled("authme")) {
                out = ByteStreams.newDataOutput();
                out.writeUTF("AuthMe.v2");
                out.writeUTF("perform.login");
                out.writeUTF(player.getName());
                player.getServer().getInfo().sendData("BungeeCord", out.toByteArray(), true);
            }
        }, 2L, TimeUnit.SECONDS);
    }
}
