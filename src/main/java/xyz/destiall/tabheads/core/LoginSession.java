package xyz.destiall.tabheads.core;

import java.util.UUID;

public abstract class LoginSession {
    private final String requestUsername;
    private String verifiedUsername;
    private UUID uuid;

    public LoginSession(String requestUsername) {
        this.requestUsername = requestUsername;
    }

    public String getVerifiedUsername() {
        return verifiedUsername;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public void setVerifiedUsername(String verifiedUsername) {
        this.verifiedUsername = verifiedUsername;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getRequestUsername() {
        return requestUsername;
    }

    @Override
    public synchronized String toString() {
        return requestUsername;
    }
}
