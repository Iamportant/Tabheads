package xyz.destiall.tabheads.bukkit.listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.utility.MinecraftVersion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import xyz.destiall.tabheads.bukkit.TabheadsBukkit;
import xyz.destiall.tabheads.bukkit.auth.ClientPublicKey;
import xyz.destiall.tabheads.bukkit.tasks.NameCheckTask;
import xyz.destiall.tabheads.bukkit.tasks.VerifyResponseTask;
import xyz.destiall.tabheads.core.EncryptionUtil;
import xyz.destiall.tabheads.core.RateLimiter;
import xyz.destiall.tabheads.core.Tabheads;

import java.security.KeyPair;
import java.util.Random;

import static com.comphenix.protocol.PacketType.Login.Client.ENCRYPTION_BEGIN;
import static com.comphenix.protocol.PacketType.Login.Client.START;

public class ProtocolListener extends PacketAdapter {
    public static final String SOURCE_META_KEY = "tabheads_source";
    private final RateLimiter rateLimiter;
    private final KeyPair keyPair = EncryptionUtil.generateKeyPair();
    private final Random random = new Random();
    public static final MinecraftVersion V1_19 = new MinecraftVersion("1.19");

    private ProtocolListener(Plugin plugin) {
        super(plugin, START, ENCRYPTION_BEGIN);
        this.rateLimiter = Tabheads.RATE_LIMITER;
    }

    public static void register() {
        TabheadsBukkit.PROTOCOL.getAsynchronousManager()
            .registerAsyncHandler(new ProtocolListener(TabheadsBukkit.INSTANCE))
            .start();
    }

    public static void unregister() {
        TabheadsBukkit.PROTOCOL.getAsynchronousManager().unregisterAsyncHandlers(TabheadsBukkit.INSTANCE);
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        if (isOurPacket(event)) return;
        Player sender = event.getPlayer();
        PacketType packetType = event.getPacketType();
        if (packetType == START) {
            if (!rateLimiter.tryAcquire()) {
                plugin.getLogger().warning("Anti-Bot join limit - Ignoring " + sender.getName());
                return;
            }
            if (MinecraftVersion.atOrAbove(V1_19)) {
                onLogin1_19(event, sender);
            } else {
                onLogin(event, sender);
            }
        } else {
            onEncryptionBegin(event, sender);
        }
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        super.onPacketSending(event);
    }

    private boolean isOurPacket(PacketEvent packetEvent) {
        return packetEvent.getPacket().getMeta(SOURCE_META_KEY)
                .map(val -> val.equals(plugin.getName()))
                .orElse(false);
    }

    private void onEncryptionBegin(PacketEvent packetEvent, Player sender) {
        byte[] sharedSecret = packetEvent.getPacket().getByteArrays().read(0);
        String sessionKey = sender.getAddress().toString();
        if (Tabheads.get().getTabConfig().isSettingEnabled("debug")) {
            Tabheads.get().getLogger().info(sessionKey + " with " + sender.getName() + " encrypting");
        }
        packetEvent.getAsyncMarker().incrementProcessingDelay();
        Runnable verifyTask = new VerifyResponseTask(TabheadsBukkit.INSTANCE, packetEvent, sender, sender.getName(), sharedSecret, keyPair);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, verifyTask);
    }

    private void onLogin1_19(PacketEvent packetEvent, Player player) {
        String sessionKey = player.getAddress().toString();
        TabheadsBukkit.INSTANCE.removeSession(player.getAddress());
        String username = packetEvent.getPacket().getStrings().read(0);
        if (Tabheads.get().getTabConfig().isSettingEnabled("debug")) {
            Tabheads.get().getLogger().info(sessionKey + " with " + username + " connecting");
        }
        packetEvent.getAsyncMarker().incrementProcessingDelay();
        Runnable nameCheckTask = new NameCheckTask(random, player, packetEvent, username, keyPair.getPublic());
        Bukkit.getScheduler().runTaskAsynchronously(plugin, nameCheckTask);
    }

    private void onLogin(PacketEvent packetEvent, Player player) {
        String sessionKey = player.getAddress().toString();
        TabheadsBukkit.INSTANCE.removeSession(player.getAddress());
        PacketContainer packet = packetEvent.getPacket();
        String username = packet.getGameProfiles().read(0).getName();
        if (Tabheads.get().getTabConfig().isSettingEnabled("debug")) {
            Tabheads.get().getLogger().info(sessionKey + " with " + username + " connecting");
        }
        packetEvent.getAsyncMarker().incrementProcessingDelay();
        Runnable nameCheckTask = new NameCheckTask(random, player, packetEvent, username, keyPair.getPublic());
        Bukkit.getScheduler().runTaskAsynchronously(plugin, nameCheckTask);
    }

}
