package xyz.destiall.tabheads.bukkit.flatfile;

import org.bukkit.Bukkit;
import xyz.destiall.tabheads.bukkit.TabheadsBukkit;
import xyz.destiall.tabheads.core.PremiumManager;
import xyz.destiall.tabheads.core.Tabheads;

public class PremiumManagerBukkit extends PremiumManager {

    @Override
    public void addPending(String name) {
        pendingConfirms.add(name);
        Bukkit.getScheduler().runTaskLater(TabheadsBukkit.INSTANCE, () -> {
            if (isPending(name)) {
                removePending(name);
                addUnconfirmed(name);
                if (Tabheads.get().getTabConfig().isSettingEnabled("debug")) {
                    Tabheads.get().getTabLogger().info("Cancelled premium session of " + name);
                }
            }
        }, 20 * 60L);
    }
}
