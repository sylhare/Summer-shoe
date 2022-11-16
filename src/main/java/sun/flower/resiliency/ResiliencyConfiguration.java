package sun.flower.resiliency;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.kotlin.KotlinModule;
import net.spy.memcached.MemcachedClient;

@Configuration
@EnableConfigurationProperties
public class ResiliencyConfiguration {

    @Value("${memcached.ip}")
    private String ip;
    @Value("${memcached.port}")
    private int port;

    private final Logger LOGGER = LoggerFactory.getLogger("Memcached");

    @Bean
    public MemcachedClient memcachedClient() {
        try {
            return new MemcachedClient(new InetSocketAddress(ip, port));
        } catch (Exception e) {
            LOGGER.error(String.format("Error with client: %s:%s", ip, port), e);
            return null;
        }
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new KotlinModule.Builder().build());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }
}
