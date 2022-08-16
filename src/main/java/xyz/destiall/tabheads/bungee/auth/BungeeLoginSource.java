package xyz.destiall.tabheads.bungee.auth;

import net.md_5.bungee.api.connection.PendingConnection;
import xyz.destiall.tabheads.core.LoginSource;

public class BungeeLoginSource implements LoginSource {
    private final PendingConnection connection;

    public BungeeLoginSource(PendingConnection connection) {
        this.connection = connection;
    }

    @Override
    public void enableOnlinemode() {
        connection.setOnlineMode(true);
    }

    public PendingConnection getConnection() {
        return connection;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + '{' +
                "connection=" + connection +
                '}';
    }
}
