package xyz.destiall.tabheads.bukkit.session;

import xyz.destiall.tabheads.bukkit.auth.ClientPublicKey;
import xyz.destiall.tabheads.core.LoginSession;

import java.security.PublicKey;
import java.time.Instant;

public class BukkitLoginSession extends LoginSession {
    private static final byte[] EMPTY_ARRAY = {};
    private final byte[] verifyToken;
    private final ClientPublicKey key;

    public BukkitLoginSession(String username, byte[] verifyToken, PublicKey key) {
        super(username);
        this.verifyToken = verifyToken.clone();
        this.key = new ClientPublicKey(Instant.MAX, key, this.verifyToken);
    }

    public BukkitLoginSession(String username) {
        this(username, EMPTY_ARRAY, null);
    }

    public synchronized byte[] getVerifyToken() {
        return verifyToken.clone();
    }

    public ClientPublicKey getClientPublicKey() {
        return key;
    }
}
