package xyz.destiall.tabheads.core.storage;

import com.github.games647.craftapi.UUIDAdapter;
import com.github.games647.craftapi.model.Profile;

import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;
import java.util.UUID;

public class FlatFile implements Storage {
    private final File datafile;

    public FlatFile(File dataFolder) {
        this.datafile = dataFolder;
        datafile.mkdir();
        File[] files = datafile.listFiles();
        if (files == null)
            return;
        for (File data : files) {
            String name = data.getName().replace(".tabheads", "").trim();
            try {
                UUID.fromString(name);
            } catch (Exception e) {
                data.delete();
            }
        }
    }

    @Override
    public UUID get(Profile profile) {
        File profileFile = new File(datafile, profile.getId().toString() + ".tabheads");
        if (!profileFile.exists())
            return null;

        try {
            Scanner scan = new Scanner(profileFile);
            return UUID.fromString(scan.next());
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void save(Profile profile, String name) {
        try {
            File profileFile = new File(datafile, profile.getId().toString() + ".tabheads");
            if (profileFile.exists()) {
                profileFile.delete();
            }
            profileFile.createNewFile();
            FileWriter writer = new FileWriter(profileFile.getAbsolutePath());
            writer.write("" + UUIDAdapter.generateOfflineId(name));
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(Profile profile) {
        File profileFile = new File(datafile, profile.getId().toString() + ".tabheads");
        if (profileFile.exists()) {
            profileFile.delete();
        }
    }
}
