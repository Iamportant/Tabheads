package xyz.destiall.tabheads.bukkit.utils;

import com.comphenix.protocol.utility.SafeCacheBuilder;
import com.google.common.cache.CacheLoader;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

public class CommonUtil {
    public static <K, V> ConcurrentMap<K, V> buildCache(int expireAfterWrite, int maxSize) {
        SafeCacheBuilder<Object, Object> builder = SafeCacheBuilder.newBuilder();
        if (expireAfterWrite > 0) {
            builder.expireAfterWrite(expireAfterWrite, TimeUnit.MINUTES);
        }
        if (maxSize > 0) {
            builder.maximumSize(maxSize);
        }
        return builder.build(CacheLoader.from(() -> {
            throw new UnsupportedOperationException();
        }));
    }
    private CommonUtil() {}
}
