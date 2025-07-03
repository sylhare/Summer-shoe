package sun.flower

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = [
    "memcached.ip=localhost",
    "memcached.port=11211"
])
class BasicAppTest {

    @Test
    fun contextLoads() {
        // This test will verify that the Spring Boot application context loads successfully
        // with all the beans configured properly
    }
} 