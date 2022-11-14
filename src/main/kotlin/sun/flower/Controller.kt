package sun.flower

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1")
class Controller {

    @GetMapping(value = ["/info"], produces = ["application/json"])
    fun info(): ResponseEntity<Info> = ResponseEntity(Info.none, HttpStatus.OK)
}
