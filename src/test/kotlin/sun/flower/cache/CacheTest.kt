package sun.flower.cache

import KGenericContainer
import net.spy.memcached.MemcachedClient
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.net.InetSocketAddress

@Testcontainers
internal class CacheTest {

    @Container
    var memcached: GenericContainer<*> = KGenericContainer("memcached:1.6.17-alpine")
        .withExposedPorts(11211)
        .withAccessToHost(true)


    @Test
    fun setupTest() {
        Assertions.assertTrue(memcached.isRunning)
        Assertions.assertTrue(memcached.isHostAccessible)
        Assertions.assertEquals(listOf(11211), memcached.exposedPorts)
        Assertions.assertEquals("localhost", memcached.host)
    }

    @Test
    fun cacheTest() {
        val client = MemcachedClient(InetSocketAddress(memcached.host, memcached.firstMappedPort))
        client.set("test", 5000, "value")
        Assertions.assertEquals(client.get("test"), "value")
    }

    @Test
    fun cacheMissTest() {
        val client = MemcachedClient(InetSocketAddress(memcached.host, memcached.firstMappedPort))
        Assertions.assertEquals(client.get("error"), null)
    }
}
