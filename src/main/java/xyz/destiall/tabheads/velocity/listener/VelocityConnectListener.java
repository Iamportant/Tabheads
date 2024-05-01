package xyz.destiall.tabheads.velocity.listener;

import com.github.games647.craftapi.UUIDAdapter;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.player.GameProfileRequestEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.InboundConnection;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import xyz.destiall.tabheads.core.LoginSession;
import xyz.destiall.tabheads.core.PremiumManager;
import xyz.destiall.tabheads.core.RateLimiter;
import xyz.destiall.tabheads.core.Tabheads;
import xyz.destiall.tabheads.velocity.TabheadsVelocity;
import xyz.destiall.tabheads.velocity.auth.AuthMeVelocityHook;
import xyz.destiall.tabheads.velocity.task.VelocityAsyncPremiumCheck;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class VelocityConnectListener {
    private final TabheadsVelocity plugin;
    private final RateLimiter rateLimiter;

    public VelocityConnectListener(TabheadsVelocity plugin) {
        this.rateLimiter = Tabheads.RATE_LIMITER;
        this.plugin = plugin;
    }

    @Subscribe
    public EventTask onPreLogin(PreLoginEvent preLoginEvent) {
        InboundConnection connection = preLoginEvent.getConnection();
        if (!preLoginEvent.getResult().isAllowed()) {
            return EventTask.async(() -> {});
        }
        if (!rateLimiter.tryAcquire()) {
            Tabheads.get().getTabLogger().warning("Anti-Bot join limit - Ignoring " + connection);
            return EventTask.async(() -> {});
        }
        String username = preLoginEvent.getUsername();
        Runnable asyncPremiumCheck = new VelocityAsyncPremiumCheck(preLoginEvent, username);
        CompletableFuture<?> future = new CompletableFuture<>();
        plugin.getServer().getScheduler().buildTask(TabheadsVelocity.INSTANCE, () -> {
            asyncPremiumCheck.run();
            future.complete(null);
        }).schedule();
        return EventTask.resumeWhenComplete(future);
    }

    @Subscribe
    public void onGameProfileRequest(GameProfileRequestEvent event) {
        if (event.isOnlineMode()) {
            LoginSession session = plugin.getSession(event.getConnection().getRemoteAddress());
            if (session == null) {
                plugin.getTabLogger().warning("No active login session found for player " + event.getUsername());
                return;
            }

            UUID verifiedUUID = event.getGameProfile().getId();
            String verifiedUsername = event.getUsername();
            session.setUuid(verifiedUUID);
            session.setVerifiedUsername(verifiedUsername);
            UUID offline = UUIDAdapter.generateOfflineId(event.getUsername());
            if (Tabheads.get().getTabConfig().isSettingEnabled("auto-name-change")) {
                offline = TabheadsVelocity.INSTANCE.getSession(event.getConnection().getRemoteAddress()).getOffline();
            }
            event.setGameProfile(event.getGameProfile().withId(offline));
        }
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent e) {
        plugin.removeSession(e.getPlayer());
    }

    @Subscribe
    public void onConnect(ServerConnectedEvent e) {
        Player player = e.getPlayer();
        PremiumManager pm = Tabheads.get().getPremiumManager();
        if (pm.isPending(player.getUsername())) {
            pm.saveProfile(player.getUsername());
            pm.removePending(player.getUsername());
            forceLogin(player);
            return;
        } else if (pm.isUnconfirmed(player.getUsername())) {
            pm.removeUnconfirmed(player.getUsername());
            plugin.getServer().getScheduler().buildTask(TabheadsVelocity.INSTANCE, () ->
                    player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Tabheads.get().getTabConfig().getMessage("unconfirmed", "&cYou did not log in time! Perhaps you were trying but you kept getting kicked? Then that means you are not premium!")))
            ).delay(2L, TimeUnit.SECONDS).schedule();
            return;
        }
        if (player.isOnlineMode()) {
            forceLogin(player);
        }
    }

    private void forceLogin(Player player) {
        plugin.getServer().getScheduler().buildTask(TabheadsVelocity.INSTANCE, () -> {
            player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Tabheads.get().getTabConfig().getMessage("premium-login", "&aYou have successfully logged in as a premium player!")));
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF(player.getUsername());
            player.sendPluginMessage(MinecraftChannelIdentifier.from("tabheads:premium"), out.toByteArray());

            out = ByteStreams.newDataOutput();
            out.writeUTF("AuthMe.v2");
            out.writeUTF("perform.login");
            out.writeUTF(player.getUsername());
            player.sendPluginMessage(MinecraftChannelIdentifier.from("bungeecord:main"), out.toByteArray());

            AuthMeVelocityHook.logIn(player);
        }).delay(2L, TimeUnit.SECONDS).schedule();
    }
}
