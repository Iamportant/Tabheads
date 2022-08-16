package xyz.destiall.tabheads.bukkit.auth;

import java.security.PublicKey;
import java.time.Instant;

public class ClientPublicKey {
    public final Instant expiry;
    public final PublicKey key;
    public final byte[] signature;

    public ClientPublicKey(Instant expiry, PublicKey key, byte[] signature) {
        this.expiry = expiry;
        this.key = key;
        this.signature = signature;
    }

    public boolean isExpired(Instant verifyTimestamp) {
        return !verifyTimestamp.isBefore(expiry);
    }
}
