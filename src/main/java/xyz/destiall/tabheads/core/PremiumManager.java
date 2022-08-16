package xyz.destiall.tabheads.core;

import com.github.games647.craftapi.UUIDAdapter;
import com.github.games647.craftapi.model.Profile;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.UUID;

public abstract class PremiumManager {
    protected File datafile;
    protected List<String> pendingConfirms;
    protected List<String> unconfirmed;

    public PremiumManager() {
        datafile = new File(Tabheads.get().getPluginPath(), "premiums" + File.separator);
        datafile.mkdir();
        pendingConfirms = new ArrayList<>();
        unconfirmed = new ArrayList<>();
        File[] files = datafile.listFiles();
        if (files == null || files.length == 0) return;
        for (File data : files) {
            String name = data.getName().replace(".tabheads", "").trim();
            try {
                UUID.fromString(name);
            } catch (Exception e) {
                saveProfile(name);
                data.delete();
            }
        }
    }

    public Optional<PremiumProfile> loadProfile(String name) {
        try {
            Optional<Profile> profile = Tabheads.get().getResolver().findProfile(name);
            if (profile.isPresent()) {
                File profileFile = new File(datafile, profile.get().getId().toString() + ".tabheads");
                if (!profileFile.exists()) return Optional.empty();
                Scanner scan = new Scanner(profileFile);
                UUID id = UUID.fromString(scan.next());
                return profile.map(value -> new PremiumProfile(value.getId(), id, value.getName()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public void saveProfile(String name) {
        try {
            Optional<Profile> profile = Tabheads.get().getResolver().findProfile(name);
            if (profile.isPresent()) {
                File profileFile = new File(datafile, profile.get().getId().toString() + ".tabheads");
                if (profileFile.exists()) {
                    profileFile.delete();
                }
                profileFile.createNewFile();
                FileWriter writer = new FileWriter(profileFile.getAbsolutePath());
                writer.write("" + UUIDAdapter.generateOfflineId(name));
                writer.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean deleteProfile(String name) {
        try {
            Optional<Profile> profile = Tabheads.get().getResolver().findProfile(name);
            if (profile.isPresent()) {
                File profileFile = new File(datafile, profile.get().getId().toString() + ".tabheads");
                if (profileFile.exists()) {
                    return profileFile.delete();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
