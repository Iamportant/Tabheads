package xyz.destiall.tabheads.core;

import com.github.games647.craftapi.model.Profile;

import java.util.UUID;

public class PremiumProfile extends Profile {
    private final UUID offline;
    public PremiumProfile(UUID online, UUID offline, String name) {
        super(online, name);
        this.offline = offline;
    }

    public UUID getOfflineId() {
        return offline;
    }
}
