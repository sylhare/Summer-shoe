package sun.flower.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import sun.flower.LOGGER
import sun.flower.model.Example
import sun.flower.resiliency.SummerCircuitBreaker

@Service
class CloudyService {
    private var clouds = 2;

    @Autowired
    private lateinit var circuitBreaker: SummerCircuitBreaker

    fun shine(id: String): Example {
        return circuitBreaker.run(Example::class.java, id) { callExternal() }
    }

    fun isCloudy(): Boolean = --clouds > 0

    fun callExternal(): Example {
        val isCloudy = this.isCloudy();
        LOGGER.info("Calling API - will succeed: ${!isCloudy}}")
        return when (isCloudy) {
            true -> throw RuntimeException("Not working")
            false -> Example("hello")
        }
    }

    fun veryCloudy() {
        this.clouds = 10
    }
}
