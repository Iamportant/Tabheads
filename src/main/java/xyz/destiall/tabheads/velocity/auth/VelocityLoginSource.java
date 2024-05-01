package xyz.destiall.tabheads.velocity.auth;

import com.velocitypowered.api.event.connection.PreLoginEvent;
import xyz.destiall.tabheads.core.LoginSource;

public class VelocityLoginSource implements LoginSource {
    private final PreLoginEvent connection;
    public VelocityLoginSource(PreLoginEvent connection) {
        this.connection = connection;
    }

    @Override
    public void enableOnlinemode() {
        connection.setResult(PreLoginEvent.PreLoginComponentResult.forceOnlineMode());
    }

    public PreLoginEvent getConnection() {
        return connection;
    }
}
