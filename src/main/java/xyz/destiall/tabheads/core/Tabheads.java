package xyz.destiall.tabheads.core;

import com.github.games647.craftapi.resolver.MojangResolver;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

public interface Tabheads<K> {
    AtomicReference<Tabheads<?>> INSTANCE = new AtomicReference<>();
    RateLimiter RATE_LIMITER = new RateLimiter(200, 5 * 60 * 1_000L);
    static void setInstance(Tabheads<?> inst) {
        if (INSTANCE.get() == null || inst == null) {
            INSTANCE.set(inst);
        }
    }

    static Tabheads<?> get() {
        return INSTANCE.get();
    }
    MojangResolver getResolver();
    LoginSession getSession(K key);
    String getSessionId(K key);
    void putSession(K key, LoginSession session);
    void removeSession(K key);
    ConcurrentMap<String, Object> getPendingLogin();
    TabLogger getTabLogger();
    String getPluginPath();
    PremiumManager getPremiumManager();
    TabConfig getTabConfig();
}
