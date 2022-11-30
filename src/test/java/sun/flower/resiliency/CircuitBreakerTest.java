package sun.flower.resiliency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import net.spy.memcached.MemcachedClient;
import sun.flower.cache.MemcachedConfiguration;
import sun.flower.model.Example;

@Testcontainers
public class CircuitBreakerTest {

    @Container
    private GenericContainer<?> memcached = new GenericContainer<>("memcached:1.6.17-alpine")
            .withExposedPorts(11211)
            .withAccessToHost(true);

    private SummerCircuitBreaker summerCircuitBreaker;
    private MemcachedClient cache;

    @BeforeEach
    public void setup() throws IOException {
        cache = new MemcachedClient(new InetSocketAddress(memcached.getHost(), memcached.getFirstMappedPort()));
        summerCircuitBreaker = new SummerCircuitBreaker(cache, new MemcachedConfiguration().objectMapper());
    }

    @Test
    public void runReturnTheCorrectValue() {
        Example result = summerCircuitBreaker.run(Example.class, "key", () -> new Example("value"));
        assertEquals(new Example("value"), result);
    }

    @Test
    public void onSuccessCache() {
        summerCircuitBreaker.run(Example.class, "hello", () -> new Example("world"));
        assertEquals("{\"name\":\"world\"}", cache.get("hello"));
    }

    @Test
    public void onFailureRetrieve() {
        Example result1 = summerCircuitBreaker.run(Example.class, "flaky", () -> new Example("service"));
        Example result2 = summerCircuitBreaker.run(Example.class, "flaky", () -> {
            throw new RuntimeException();
        });
        assertEquals(new Example("service"), result1);
        assertEquals(new Example("service"), result2);
    }

    @Test
    public void onCacheMissNull() {
        Example result = summerCircuitBreaker.run(Example.class, "null", () -> {
            throw new RuntimeException();
        });
        assertNull(result);
    }
}
