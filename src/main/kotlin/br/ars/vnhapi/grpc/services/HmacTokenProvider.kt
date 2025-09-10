// build.gradle.kts: adicione
// implementation("com.auth0:java-jwt:4.4.0")

// src/main/kotlin/br/ars/vnhapi/grpc/services/HmacTokenProvider.kt
package br.ars.vnhapi.grpc.services

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.*

@Component
class HmacTokenProvider(
    @Value("\${security.jwt.issuer:vnhapi}") private val issuer: String,
    @Value("\${security.jwt.hmacSecret}") private val hmacSecret: String
) : TokenProvider {

    override fun mintAccessToken(
        userId: UUID,
        clientId: String,
        deviceId: String,
        scope: String?,
        aud: String?,
        ttlSeconds: Int
    ): String {
        val now = Instant.now()
        val alg = Algorithm.HMAC256(hmacSecret)
        return JWT.create()
            .withIssuer(issuer)
            .withSubject(userId.toString())
            .withAudience(aud ?: "gateway")
            .withClaim("cid", clientId)
            .withClaim("did", deviceId)
            .withClaim("scope", scope ?: "")
            .withJWTId(UUID.randomUUID().toString())
            .withIssuedAt(Date.from(now))
            .withExpiresAt(Date.from(now.plusSeconds(ttlSeconds.toLong())))
            .sign(alg)
    }
}
