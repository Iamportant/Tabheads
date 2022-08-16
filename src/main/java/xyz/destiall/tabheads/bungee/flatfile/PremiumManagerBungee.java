package xyz.destiall.tabheads.bungee.flatfile;

import net.md_5.bungee.api.ProxyServer;
import xyz.destiall.tabheads.bungee.TabheadsBungee;
import xyz.destiall.tabheads.core.PremiumManager;
import xyz.destiall.tabheads.core.Tabheads;

import java.util.concurrent.TimeUnit;

public class PremiumManagerBungee extends PremiumManager {

    @Override
    public void addPending(String name) {
        pendingConfirms.add(name);
        ProxyServer.getInstance().getScheduler().schedule(TabheadsBungee.INSTANCE, () -> {
            if (isPending(name)) {
                removePending(name);
                addUnconfirmed(name);
                if (Tabheads.get().getTabConfig().isSettingEnabled("debug")) {
                    Tabheads.get().getLogger().info("Cancelled premium session of " + name);
                }
            }
        }, 1L, TimeUnit.MINUTES);
    }
}
