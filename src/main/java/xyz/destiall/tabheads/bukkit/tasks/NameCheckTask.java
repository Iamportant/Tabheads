package xyz.destiall.tabheads.bukkit.tasks;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.entity.Player;
import xyz.destiall.tabheads.bukkit.TabheadsBukkit;
import xyz.destiall.tabheads.bukkit.auth.ProtocolLibLoginSource;
import xyz.destiall.tabheads.bukkit.session.BukkitLoginSession;
import xyz.destiall.tabheads.core.JoinTask;
import xyz.destiall.tabheads.core.Tabheads;

import java.security.PublicKey;
import java.util.Random;
import java.util.UUID;

public class NameCheckTask extends JoinTask<ProtocolLibLoginSource> implements Runnable {
    private final PacketEvent packetEvent;
    private final PublicKey publicKey;
    private final Random random;
    private final Player player;
    private final String username;

    public NameCheckTask(Random random, Player player, PacketEvent packetEvent, String username, PublicKey publicKey) {
        this.packetEvent = packetEvent;
        this.publicKey = publicKey;
        this.random = random;
        this.player = player;
        this.username = username;
    }

    @Override
    public void run() {
        try {
            super.onLogin(username, new ProtocolLibLoginSource(player, random, publicKey));
        } finally {
            ProtocolLibrary.getProtocolManager().getAsynchronousManager().signalPacketTransmission(packetEvent);
        }
    }

    @Override
    public void requestPremiumLogin(ProtocolLibLoginSource source, UUID offline, String username) {
        try {
            source.enableOnlinemode();
        } catch (Exception ex) {
            if (Tabheads.get().getTabConfig().isSettingEnabled("debug")) {
                TabheadsBukkit.INSTANCE.getLogger().warning("Unable to send encryption packet for " + username);
                ex.printStackTrace();
            }
            startCrackedSession(source, username);
            return;
        }
        String ip = player.getAddress().getAddress().getHostAddress();
        TabheadsBukkit.INSTANCE.getPendingLogin().put(ip + username, new Object());
        byte[] verify = source.getVerifyToken();
        TabheadsBukkit.INSTANCE.putSession(player.getAddress(), new BukkitLoginSession(username, verify, publicKey));
        synchronized (packetEvent.getAsyncMarker().getProcessingLock()) {
            packetEvent.setCancelled(true);
        }
    }

    @Override
    public void startCrackedSession(ProtocolLibLoginSource source, String username)  {
        TabheadsBukkit.INSTANCE.putSession(player.getAddress(), new BukkitLoginSession(username));
    }
}
