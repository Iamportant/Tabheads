package xyz.destiall.tabheads.core;

import com.github.games647.craftapi.model.Profile;
import xyz.destiall.tabheads.core.storage.FlatFile;
import xyz.destiall.tabheads.core.storage.Storage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public abstract class PremiumManager {
    protected Storage storage;
    protected List<String> pendingConfirms;
    protected List<String> unconfirmed;

    public PremiumManager() {
        storage = new FlatFile(new File(Tabheads.get().getPluginPath(), "premiums" + File.separator));

        pendingConfirms = new ArrayList<>();
        unconfirmed = new ArrayList<>();
    }

    public Optional<PremiumProfile> loadProfile(String name) {
        try {
            Optional<Profile> profile = Tabheads.get().getResolver().findProfile(name);
            if (profile.isPresent()) {
                UUID id = storage.get(profile.get());
                return profile.map(value -> new PremiumProfile(value.getId(), id, value.getName()));
            }
        } catch (Exception ignored) {}
        return Optional.empty();
    }

    public void saveProfile(String name) {
        try {
            Optional<Profile> profile = Tabheads.get().getResolver().findProfile(name);
            profile.ifPresent(value -> storage.save(value, name));
        } catch (Exception ignored) {}
    }

    public boolean deleteProfile(String name) {
        try {
            Optional<Profile> profile = Tabheads.get().getResolver().findProfile(name);
            profile.ifPresent(value -> storage.delete(value));
        } catch (Exception ignored) {}
        return false;
    }

    public boolean isPending(String name) {
        return pendingConfirms.contains(name);
    }

    public void removePending(String name) {
        pendingConfirms.remove(name);
    }

    public abstract void addPending(String name);

    public void addUnconfirmed(String name) {
        unconfirmed.add(name);
    }

    public boolean isUnconfirmed(String name) {
        return unconfirmed.contains(name);
    }

    public void removeUnconfirmed(String name) {
        unconfirmed.remove(name);
    }
}
