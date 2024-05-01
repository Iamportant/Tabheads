package xyz.destiall.tabheads.velocity.flatfile;

import xyz.destiall.tabheads.core.PremiumManager;
import xyz.destiall.tabheads.core.Tabheads;
import xyz.destiall.tabheads.velocity.TabheadsVelocity;

import java.util.concurrent.TimeUnit;

public class PremiumManagerVelocity extends PremiumManager {
    private final TabheadsVelocity plugin;

    public PremiumManagerVelocity(TabheadsVelocity plugin) {
        this.plugin = plugin;
    }

    @Override
    public void addPending(String name) {
        pendingConfirms.add(name);
        plugin.getServer().getScheduler().buildTask(plugin, () -> {
            if (isPending(name)) {
                removePending(name);
                addUnconfirmed(name);
                if (Tabheads.get().getTabConfig().isSettingEnabled("debug")) {
                    Tabheads.get().getTabLogger().info("Cancelled premium session of " + name);
                }
            }
        }).delay(1L, TimeUnit.MINUTES).schedule();
    }
}
