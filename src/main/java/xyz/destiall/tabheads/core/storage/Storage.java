package xyz.destiall.tabheads.core.storage;

import com.github.games647.craftapi.model.Profile;

import java.util.UUID;

public interface Storage {

    UUID get(Profile profile);
    void save(Profile profile, String name);
    void delete(Profile profile);
}
