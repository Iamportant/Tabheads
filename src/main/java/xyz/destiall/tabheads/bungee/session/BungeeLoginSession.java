package xyz.destiall.tabheads.bungee.session;

import com.github.games647.craftapi.UUIDAdapter;
import xyz.destiall.tabheads.core.LoginSession;

import java.util.UUID;

public class BungeeLoginSession extends LoginSession {
    private final UUID offline;
    public BungeeLoginSession(String username) {
        super(username);
        offline = UUIDAdapter.generateOfflineId(username);
    }

    public BungeeLoginSession(String username, UUID offline) {
        super(username);
        this.offline = offline;
    }

    public UUID getOffline() {
        return offline;
    }

    @Override
    public synchronized String toString() {
        return this.getClass().getSimpleName() + '{' +
                "username=" + getRequestUsername() +
                "} " + super.toString();
    }
}
