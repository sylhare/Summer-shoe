package sun.flower

import KGenericContainer
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.net.URI


@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension::class)
internal class AppTests {

    companion object {
        @Container
        var memcached: GenericContainer<*> = KGenericContainer("memcached:1.6.17-bullseye")
            .withExposedPorts(11211)
            .withAccessToHost(true)
    }

    @Value("\${local.server.port}")
    var applicationPort: Int = 0

    var testRestTemplate = TestRestTemplate()

    @Test
    fun setupTest() {
        Assertions.assertTrue(memcached.isRunning)
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

            Assertions.assertEquals(HttpStatus.OK, result.statusCode)
        }
    }


    @Nested
    inner class Controller {
        @Test
        fun infoTest() {
            val result = testRestTemplate.exchange(
                URI(applicationUrl() + "/v1/info"),
                HttpMethod.GET,
                HttpEntity(""),
                Info::class.java)

            Assertions.assertEquals(HttpStatus.OK, result.statusCode)
        }
    }

    private fun applicationUrl() = "http://localhost:$applicationPort"
}
