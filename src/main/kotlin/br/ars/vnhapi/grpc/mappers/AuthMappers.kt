// src/main/kotlin/br/ars/vnhapi/grpc/mappers/AuthMappers.kt
package br.ars.vnhapi.grpc.mappers

import br.ars.vnhapi.domain.AuthEntity
import java.time.Instant

data class TokenPairDTO(
    val accessToken: String,
    val accessExpiresIn: Int,
    val refreshToken: String,
    val refreshExpiresAt: Long // epoch millis
)

object AuthMappers {
    fun toTokenPairDTO(
        accessJwt: String,
        accessTtlSeconds: Int,
        refreshOpaque: String,
        refreshExp: Instant
    ) = TokenPairDTO(
        accessToken = accessJwt,
        accessExpiresIn = accessTtlSeconds,
        refreshToken = refreshOpaque,
        refreshExpiresAt = refreshExp.toEpochMilli()
    )

    fun isActive(e: AuthEntity): Boolean =
        e.reuseDetected.not() && e.revokedAt == null && Instant.now().isBefore(e.refreshExpiresAt)
}
