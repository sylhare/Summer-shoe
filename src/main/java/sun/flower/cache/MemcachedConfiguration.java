package sun.flower.cache;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.kotlin.KotlinModule;
import net.spy.memcached.MemcachedClient;

@Configuration
@EnableConfigurationProperties
public class MemcachedConfiguration implements CachingConfigurer {

    @Value("${memcached.ip}")
    private String host;
    @Value("${memcached.port}")
    private int port;

    public static String RAINBOW_CACHE = "rainbow";
    public static Logger LOGGER = LoggerFactory.getLogger("Memcached");

    /**
     * For simple Memcached configuration
     *
     * @return net.spy.memcached.MemcachedClient
     */
    @Bean
    public MemcachedClient memcachedClient() {
        try {
            return new MemcachedClient(new InetSocketAddress(host, port));
        } catch (Exception e) {
            LOGGER.error(String.format("Error with client: %s:%s", host, port), e);
            return null;
        }
    }

    @Bean
    protected MemcachedClientConfiguration memcachedClientConfiguration() {
        return new MemcachedClientConfiguration(RAINBOW_CACHE, String.format("%s:%s", host, port), 5000);
    }

    /**
     * To use the @Cacheable with the custom Memcached cache.
     *
     * @return Overridden Spring CacheManager
     */
    @Override
    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        try {
            cacheManager.setCaches(List.of(new Memcached(memcachedClientConfiguration())));
            return cacheManager;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Object mapper compatible with Kotlin data class
     *
     * @return ObjectMapper
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new KotlinModule.Builder().build());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }
}
