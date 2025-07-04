package sun.flower.resiliency;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.util.Arrays;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.vavr.control.Try;

public class Resilience4jTests {
    private final ResiliencyExample resiliencyExample = new ResiliencyExample();
    private final TestSupplier supplier = new TestSupplier();

    @BeforeEach
    public void setup() {
        supplier.count = 0;
        supplier.hasFailed = false;
    }

    @Test
    public void noRetryTest() {
        assertEquals("test", resiliencyExample.retry(supplier));
        assertEquals(1, supplier.count);
    }

    @Test
    public void retryTest() {
        supplier.hasFailed = true;
        assertEquals("Recover function when the retry failed", resiliencyExample.retry(supplier));
        assertEquals(2, supplier.count);
    }

    @Test
    public void noCircuitBreakerFailureTest() {
        assertEquals("test", resiliencyExample.runWithCircuitBreaker(supplier));
        assertEquals(1, supplier.count);
    }

    @Test
    public void circuitBreakerWithFailureTest() {
        supplier.hasFailed = true;
        assertEquals("Hello from Recovery", resiliencyExample.runWithCircuitBreaker(supplier));
        supplier.hasFailed = false;
        assertEquals("test", resiliencyExample.runWithCircuitBreaker(supplier));
        assertEquals(2, supplier.count);
        assertEquals(CircuitBreaker.State.CLOSED, resiliencyExample.circuitBreaker.getState());
    }

    @Test
    public void circuitBreakerFailureTest() {
        supplier.hasFailed = true;
        Arrays.stream("1".repeat(200).split(""))
                .forEach((String lol) -> resiliencyExample.runWithCircuitBreaker(supplier));
        assertEquals("Hello from Recovery", resiliencyExample.runWithCircuitBreaker(supplier));
        assertEquals(100, supplier.count);
        assertEquals(CircuitBreaker.State.OPEN, resiliencyExample.circuitBreaker.getState());
    }

    static class ResiliencyExample {

        final Retry retry = Retry.of(SummerCircuitBreaker.class.getSimpleName(),
                RetryConfig.custom()
                        .maxAttempts(2)
                        .intervalFunction(IntervalFunction.of(Duration.ofMillis(200)))
                        .build());
        final CircuitBreaker circuitBreaker = CircuitBreaker.of(SummerCircuitBreaker.class.getSimpleName(),
                CircuitBreakerConfig.custom()
                        .waitDurationInOpenState(Duration.ofSeconds(60))
                        .recordException(throwable -> throwable instanceof Exception)
                        .build());

        public String retry(Supplier<String> supplier) {
            Supplier<String> retryableSupplier = Retry.decorateSupplier(this.retry, supplier);
            return Try.of(retryableSupplier::get)
                    .recover(throwable -> "Recover function when the retry failed").get();
        }

        public String runWithCircuitBreaker(Supplier<String> supplier) {
            Supplier<String> decoratedSupplier = CircuitBreaker.decorateSupplier(this.circuitBreaker, supplier);
            return Try.ofSupplier(decoratedSupplier)
                    .recover(throwable -> "Hello from Recovery").get();
        }
    }

    static class TestSupplier implements Supplier<String> {
        public int count = 0;
        public boolean hasFailed = false;

        @Override
        public String get() {
            ++count;
            if (hasFailed) throw new RuntimeException();
            return "test";
        }
    }
}
