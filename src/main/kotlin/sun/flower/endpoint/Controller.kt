package sun.flower.endpoint

import com.fasterxml.jackson.databind.ObjectMapper
import net.spy.memcached.MemcachedClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import sun.flower.LOGGER

@RestController
@RequestMapping("/v1")
class Controller {

    @Autowired
    lateinit var cacheClient: MemcachedClient

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @GetMapping(value = ["/cache"], produces = ["application/json"])
    fun cache(): ResponseEntity<Info> {
        cacheClient.set("1", 5000, objectMapper.writeValueAsString(Info.none))
        val value = cacheClient.get("1") as String
        LOGGER.info("Cached value $value")
        val mappedValue = objectMapper.readValue<Info>(value)
        LOGGER.info("Mapped value $mappedValue")
        return ResponseEntity(mappedValue, HttpStatus.OK)
    }
}