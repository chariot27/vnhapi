// src/main/kotlin/br/ars/vnhapi/http/MatchController.kt
package br.ars.vnhapi.http

import br.ars.vnhapi.grpc.mappers.MatchDTO
import br.ars.vnhapi.grpc.services.MatchServiceImpl
import br.ars.vnhapi.shared.constants.ApiUrls
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import kotlinx.coroutines.runBlocking
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(ApiUrls.MATCH_ROUTE)
@Validated
class MatchController(
    private val service: MatchServiceImpl
) {

    data class SendInviteRequest(
        @field:NotBlank val idUsuarioFrom: String,
        @field:NotBlank val idUsuarioTo: String
    )

    data class RespondInviteRequest(
        @field:NotBlank val id: String,
        @field:NotNull  val accept: Boolean
    )

    @PostMapping
    fun send(@Valid @RequestBody body: SendInviteRequest): ResponseEntity<MatchDTO> = runBlocking {
        ResponseEntity.ok(service.sendInvite(body.idUsuarioFrom, body.idUsuarioTo))
    }

    @PostMapping("/respond")
    fun respond(@Valid @RequestBody body: RespondInviteRequest): ResponseEntity<MatchDTO> = runBlocking {
        ResponseEntity.ok(service.respondInvite(body.id, body.accept))
    }

    @GetMapping("/{id}")
    fun get(@PathVariable id: String): ResponseEntity<MatchDTO> = runBlocking {
        ResponseEntity.ok(service.getInvite(id))
    }

    @GetMapping
    fun list(
        @RequestParam("userId") userId: String,
        @RequestParam("role", required = false) role: String?,
        @RequestParam("status", required = false) status: String?,
        @RequestParam("page", defaultValue = "1") page: Int,
        @RequestParam("size", defaultValue = "20") size: Int
    ): ResponseEntity<Page<MatchDTO>> = runBlocking {
        ResponseEntity.ok(service.listInvites(userId, role, status, page, size))
    }
}
