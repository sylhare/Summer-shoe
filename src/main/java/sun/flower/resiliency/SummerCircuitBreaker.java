package sun.flower.resiliency;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.vavr.CheckedFunction0;
import io.vavr.control.Try;
import net.spy.memcached.MemcachedClient;

@Component
public class SummerCircuitBreaker {

    private static final Logger LOGGER = LoggerFactory.getLogger("CircuitBreaker");
    private static final int TTL_1_DAY = 3600 * 24;

    private final MemcachedClient cacheClient;
    private final ObjectMapper objectMapper;
    private final Retry retry;
    private final CircuitBreaker circuitBreaker;

    @Autowired
    public SummerCircuitBreaker(MemcachedClient cacheClient, ObjectMapper objectMapper) {
        this.cacheClient = cacheClient;
        this.objectMapper = objectMapper;
        this.retry = Retry.of(SummerCircuitBreaker.class.getSimpleName(),
                RetryConfig.custom()
                        .maxAttempts(2)
                        .intervalFunction(IntervalFunction.of(Duration.ofMillis(200)))
                        .build());
        this.circuitBreaker = CircuitBreaker.of(SummerCircuitBreaker.class.getSimpleName(),
                CircuitBreakerConfig.custom()
                        .waitDurationInOpenState(Duration.ofSeconds(60))
                        .recordException(throwable -> throwable instanceof Exception)
                        .build());
    }

    public <T> T run(Class<T> expectedClass, String id, Supplier<T> supplier) {
        Supplier<T> decoratedSupplier = CircuitBreaker
                .decorateSupplier(circuitBreaker, supplier);
        CheckedFunction0<T> decoratedRetry = Retry
                .decorateCheckedSupplier(this.retry, decoratedSupplier::get);
        return Try.of(decoratedRetry)
                .onSuccess(storeInCache(id))
                .recover(getFromCache(id, expectedClass)).get();
    }

    private <T> Consumer<T> storeInCache(String id) {
        return result -> Try.of(() -> cacheClient.set(id, TTL_1_DAY, objectMapper.writeValueAsString(result)))
                .onFailure(throwable -> LOGGER.error("Could not store in cache", throwable));
    }

    private <T> Function<Throwable, T> getFromCache(String id, Class<T> expectedClass) {
        return throwable -> Try.of(() -> objectMapper.readValue((String) cacheClient.get(id), expectedClass))
                .onFailure(exception -> LOGGER.error("Could not retrieve from cache", exception))
                .recover(fromCache -> null)
                .get();
    }
}
