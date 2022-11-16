package sun.flower

import KGenericContainer
import net.spy.memcached.MemcachedClient
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
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
import java.net.InetSocketAddress
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

    @Test
    fun setupTest() {
        assertTrue(memcached.isRunning)
        assertTrue(memcached.isHostAccessible)
        assertEquals(listOf(11211), memcached.exposedPorts)
        assertEquals("localhost", memcached.host)
    }

    @Test
    fun cacheTest() {
        val client = MemcachedClient(InetSocketAddress(memcached.host, memcached.firstMappedPort))
        client.set("test", 5000, "value")
        assertEquals(client.get("test"), "value")
    }

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
    }

    private fun applicationUrl() = "http://localhost:$applicationPort"
}
