package sun.flower.cache;

import static sun.flower.cache.MemcachedConfiguration.LOGGER;

import java.io.IOException;
import java.util.concurrent.Callable;

import org.jetbrains.annotations.NotNull;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.ConnectionFactoryBuilder;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.transcoders.SerializingTranscoder;


public class Memcached implements Cache {

    private final MemcachedClientConfiguration configuration;
    private final MemcachedClient cache;

    public Memcached(MemcachedClientConfiguration configuration) throws IOException {
        this.configuration = configuration;
        cache = new MemcachedClient(
                new ConnectionFactoryBuilder()
                        .setTranscoder(new SerializingTranscoder())
                        .setProtocol(ConnectionFactoryBuilder.Protocol.TEXT)
                        .build(),
                AddrUtil.getAddresses(configuration.address));
    }

    @NotNull
    @Override
    public String getName() {
        return configuration.name;
    }

    @NotNull
    @Override
    public Object getNativeCache() {
        return cache;
    }

    @Override
    public ValueWrapper get(@NotNull final Object key) {
        Object value = null;
        try {
            value = cache.get(key.toString());
        } catch (final Exception e) {
            LOGGER.warn(e.getMessage());
        }
        if (value == null) {
            LOGGER.debug("cache miss for key: " + key);
            return null;
        }
        LOGGER.debug("cache hit for key: " + key);
        return new SimpleValueWrapper(value);
    }

    @Override
    public void put(@NotNull final Object key, final Object value) {
        if (value != null) {
            cache.set(key.toString(), configuration.expiration, value);
            LOGGER.debug("cache put for key: " + key);
        }
    }

    @Override
    public void evict(final Object key) {
        this.cache.delete(key.toString());
        LOGGER.debug("cache delete for key: " + key);
    }

    @Override
    public void clear() {
        cache.flush();
        LOGGER.debug("cache clear completed");
    }

    @Override
    public <T> T get(@NotNull Object o, Class<T> aClass) {
        LOGGER.warn("Operation not handled");
        return null;
    }

    @Override
    public <T> T get(@NotNull Object o, @NotNull Callable<T> callable) {
        LOGGER.warn("Operation not handled");
        return null;
    }

    @Override
    public ValueWrapper putIfAbsent(@NotNull Object o, Object o1) {
        LOGGER.warn("Operation not handled");
        return null;
    }
}
