package xyz.destiall.tabheads.bukkit.listener;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import xyz.destiall.tabheads.bukkit.TabheadsBukkit;
import xyz.destiall.tabheads.core.PremiumManager;
import xyz.destiall.tabheads.core.Tabheads;

public class ConnectListener implements Listener {
    private static final ConnectListener inst = new ConnectListener();

    private ConnectListener() {}
    public static void register() {
        Bukkit.getPluginManager().registerEvents(inst, TabheadsBukkit.INSTANCE);
    }

    public static void unregister() {
        HandlerList.unregisterAll(inst);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        PremiumManager pm = Tabheads.get().getPremiumManager();
        Player p = e.getPlayer();
        if (pm.isPending(p.getName())) {
            pm.saveProfile(p.getName());
            pm.removePending(p.getName());
            login(p);
            return;
        }
        if (pm.loadProfile(p.getName()).isPresent()) {
            login(e.getPlayer());
            return;
        }
        if (pm.isUnconfirmed(p.getName())) {
            pm.removeUnconfirmed(p.getName());
            Bukkit.getScheduler().runTaskLater(TabheadsBukkit.INSTANCE, () ->
                    p.sendMessage(Tabheads.get().getTabConfig().getMessage("unconfirmed", "&cYou did not log in time! Perhaps you were trying but you kept getting kicked? Then that means you are not premium!"))
                    , 2 * 20L);
        }
    }

    private void login(Player player) {
        Bukkit.getScheduler().runTaskLater(TabheadsBukkit.INSTANCE, () -> {
            if (Tabheads.get().getTabConfig().isSettingEnabled("debug")) {
                Tabheads.get().getTabLogger().info("Logging " + player.getName() + " as premium");
            }
            player.sendMessage(Tabheads.get().getTabConfig().getMessage("premium-login", "&aYou have successfully logged in as a premium player!"));
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF(player.getName());
            player.sendPluginMessage(TabheadsBukkit.INSTANCE, "tabheads:premium", out.toByteArray());
            if (Tabheads.get().getTabConfig().isAuthEnabled("authme")) {
                if (Tabheads.get().getTabConfig().isSettingEnabled("debug")) {
                    Tabheads.get().getTabLogger().info("Sending AuthMe login message for " + player.getName());
                }
                out = ByteStreams.newDataOutput();
                out.writeUTF("AuthMe.v2");
                out.writeUTF("perform.login");
                out.writeUTF(player.getName());
                player.sendPluginMessage(TabheadsBukkit.INSTANCE, "BungeeCord", out.toByteArray());
            }
        }, 2 * 20L);
    }
}
