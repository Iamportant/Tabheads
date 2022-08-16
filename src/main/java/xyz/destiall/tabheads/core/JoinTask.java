package xyz.destiall.tabheads.core;

import com.github.games647.craftapi.UUIDAdapter;
import com.github.games647.craftapi.model.Profile;

import java.util.Optional;
import java.util.UUID;

public abstract class JoinTask<S extends LoginSource> {
    public JoinTask() {}

    public void onLogin(String username, S source) {
        try {
            Optional<PremiumProfile> prof = Tabheads.get().getPremiumManager().loadProfile(username);
            if (Tabheads.get().getTabConfig().isSettingEnabled("auto-premium")) {
                Optional<Profile> profile = Tabheads.get().getResolver().findProfile(username);
                if (profile.isPresent()) {
                    if (Tabheads.get().getTabConfig().isSettingEnabled("debug")) {
                        Tabheads.get().getLogger().info("Requesting premium login for " + username);
                    }
                    if (prof.isPresent()) {
                        requestPremiumLogin(source, prof.get().getOfflineId(), username);
                        return;
                    }
                    requestPremiumLogin(source, UUIDAdapter.generateOfflineId(username), username);
                }
                return;
            }
            if (prof.isPresent() ||
                Tabheads.get().getPremiumManager().isPending(username)) {
                if (Tabheads.get().getTabConfig().isSettingEnabled("debug")) {
                    Tabheads.get().getLogger().info("Requesting premium login for " + username);
                }
                requestPremiumLogin(source, prof.isPresent() ? prof.get().getOfflineId() : UUIDAdapter.generateOfflineId(username), username);
                return;
            }
            startCrackedSession(source, username);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public abstract void requestPremiumLogin(S source, UUID offline, String username);

    public abstract void startCrackedSession(S source, String username);
}
