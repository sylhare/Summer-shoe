package sun.flower.resiliency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.code.ssm.config.DefaultAddressProvider;
import com.google.code.ssm.providers.CacheClient;
import com.google.code.ssm.providers.CacheConfiguration;
import com.google.code.ssm.providers.xmemcached.MemcacheClientFactoryImpl;

@Configuration
@EnableConfigurationProperties
public class ResiliencyConfiguration {

    @Value("${memcached.servers:localhost:11211}")
    private String hosts;

    private final Logger LOGGER = LoggerFactory.getLogger("Memcached");

    @Bean
    public CacheClient memcachedClient() {
        try {
            final DefaultAddressProvider addressProvider = new DefaultAddressProvider();
            addressProvider.setAddress(hosts);
            final CacheConfiguration cacheConfiguration = new CacheConfiguration();
            cacheConfiguration.setConsistentHashing(true);
            return new MemcacheClientFactoryImpl().create(addressProvider.getAddresses(), cacheConfiguration);
        } catch (Exception e) {
            LOGGER.error(String.format("Error with client: %s", hosts), e);
            return null;
        }
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }

}
