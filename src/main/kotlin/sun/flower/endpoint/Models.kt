package sun.flower.endpoint

import tools.jackson.core.type.TypeReference
import tools.jackson.databind.ObjectMapper

data class Info(val name: String) {
    companion object {
        val none = Info("none")
    }
}

inline fun <reified T> ObjectMapper.readValue(s: String): T = this.readValue(s, object : TypeReference<T>() {})
