package br.ars.vnhapi.http

import br.ars.vnhapi.grpc.mappers.TokenPairDTO
import br.ars.vnhapi.grpc.services.AuthServiceImpl
import br.ars.vnhapi.grpc.services.LoginCmd
import br.ars.vnhapi.grpc.services.RefreshCmd
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import kotlinx.coroutines.runBlocking
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
@Validated
class AuthController(
    private val auth: AuthServiceImpl
) {

    data class LoginRequest(
        @field:NotBlank val username: String,
        @field:NotBlank val password: String,
        @field:NotBlank val clientId: String,
        @field:NotBlank val deviceId: String,
        val scope: String? = null,
        val aud: String? = "gateway"
    )

    data class RefreshRequest(
        @field:NotBlank val refreshToken: String,
        @field:NotBlank val clientId: String,
        @field:NotBlank val deviceId: String
    )

    data class RevokeRequest(
        @field:NotBlank val refreshToken: String,
        @field:NotBlank val clientId: String,
        @field:NotBlank val deviceId: String
    )

    @PostMapping("/login")
    fun login(@Valid @RequestBody body: LoginRequest): ResponseEntity<TokenPairDTO> = runBlocking {
        val tokens = auth.login(
            LoginCmd(
                username = body.username,
                password = body.password,
                clientId = body.clientId,
                deviceId = body.deviceId,
                scope = body.scope,
                aud = body.aud
            )
        )
        ResponseEntity.ok(tokens)
    }

    @PostMapping("/refresh")
    fun refresh(@Valid @RequestBody body: RefreshRequest): ResponseEntity<TokenPairDTO> = runBlocking {
        val tokens = auth.refresh(
            RefreshCmd(
                refreshToken = body.refreshToken,
                clientId = body.clientId,
                deviceId = body.deviceId
            )
        )
        ResponseEntity.ok(tokens)
    }

    @PostMapping("/revoke")
    fun revoke(@Valid @RequestBody body: RevokeRequest): ResponseEntity<Map<String, Boolean>> = runBlocking {
        val ok = auth.revoke(body.refreshToken, body.clientId, body.deviceId)
        ResponseEntity.ok(mapOf("revoked" to ok))
    }

    @PostMapping("/introspect")
    fun introspect(@RequestBody payload: Map<String, String>): ResponseEntity<Map<String, Any>> = runBlocking {
        val token = payload["accessToken"] ?: return@runBlocking ResponseEntity.badRequest().build()
        ResponseEntity.ok(auth.introspect(token))
    }
}
