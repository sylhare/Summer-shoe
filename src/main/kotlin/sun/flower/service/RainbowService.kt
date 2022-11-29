package sun.flower.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import sun.flower.LOGGER
import sun.flower.endpoint.Info

@Service
class RainbowService {

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Cacheable("rainbow")
    fun shine(name: String): String {
        LOGGER.info("Called for $name")
        return objectMapper.writeValueAsString(Info(name))
    }
}
