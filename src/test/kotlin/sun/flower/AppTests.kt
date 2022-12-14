package sun.flower

import KGenericContainer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.context.support.TestPropertySourceUtils
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import sun.flower.endpoint.Info
import sun.flower.model.Example
import java.net.URI


@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = [AppTests.Companion.MemcacheInitializer::class])
@ExtendWith(SpringExtension::class)
internal class AppTests {

    companion object {
        @Container
        var memcached: GenericContainer<*> = KGenericContainer("memcached:1.6.17-alpine")
            .withExposedPorts(11211)
            .withAccessToHost(true)

        class MemcacheInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
            override fun initialize(context: ConfigurableApplicationContext) {
                TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                    context,
                    "memcached.ip=${memcached.host}", "memcached.port=${memcached.firstMappedPort}"
                )
            }
        }
    }

    @Value("\${local.server.port}")
    var applicationPort: Int = 0

    var testRestTemplate = TestRestTemplate()

    @Nested
    inner class Metrics {

        @Test
        fun healthTest() {
            println(applicationPort)
            val result: ResponseEntity<Void> = testRestTemplate.exchange(
                URI(applicationUrl() + "/actuator/health"),
                HttpMethod.GET,
                HttpEntity(""),
                Void::class.java
            )

            assertEquals(HttpStatus.OK, result.statusCode)
        }
    }

    @Nested
    inner class Controller {
        @Test
        fun cacheTest() {
            val result = testRestTemplate.exchange(
                URI(applicationUrl() + "/v1/cache"),
                HttpMethod.GET,
                HttpEntity(""),
                Info::class.java
            )

            assertEquals(HttpStatus.OK, result.statusCode)
            assertEquals(Info.none, result.body)
        }

        @Test
        fun cacheableTest() {
            var result = testRestTemplate.exchange(
                URI(applicationUrl() + "/v1/cacheable"),
                HttpMethod.GET,
                HttpEntity(""),
                Info::class.java
            )
            repeat(5) {
                result = testRestTemplate.exchange(
                    URI(applicationUrl() + "/v1/cacheable"),
                    HttpMethod.GET,
                    HttpEntity(""),
                    Info::class.java
                )
            }

            assertEquals(HttpStatus.OK, result.statusCode)
            assertEquals(Info("goldy"), result.body)
        }

        @Test
        fun circuitBreakableTest() {
            val result = testRestTemplate.exchange(
                URI(applicationUrl() + "/v1/circuitBreakable"),
                HttpMethod.GET,
                HttpEntity(""),
                Example::class.java
            )

            assertEquals(HttpStatus.OK, result.statusCode)
            assertEquals(Example("hello"), result.body)
        }

        @Test
        fun circuitBreakableWithCacheTest() {
            val result = testRestTemplate.exchange(
                URI(applicationUrl() + "/v1/circuitBreakableWithCache"),
                HttpMethod.GET,
                HttpEntity(""),
                Example::class.java
            )

            assertEquals(HttpStatus.OK, result.statusCode)
            assertEquals(Example("hello"), result.body)
        }
    }

    private fun applicationUrl() = "http://localhost:$applicationPort"
}
