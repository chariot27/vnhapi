// src/main/kotlin/br/ars/vnhapi/domain/auth/AuthEntity.kt
package br.ars.vnhapi.domain

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "auth_sessions",
    indexes = [
        Index(name = "ix_auth_user", columnList = "user_id"),
        Index(name = "ix_auth_client_device", columnList = "client_id,device_id"),
        Index(name = "ix_auth_expires", columnList = "refresh_expires_at")
    ],
    uniqueConstraints = [
        UniqueConstraint(name = "uk_auth_refresh_hash", columnNames = ["refresh_hash"])
    ]
)
class AuthEntity(

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    var id: UUID = UUID.randomUUID(),

    @Column(name = "user_id", nullable = false, updatable = false)
    var userId: UUID,

    @Column(name = "client_id", nullable = false, length = 64, updatable = false)
    var clientId: String,

    @Column(name = "device_id", nullable = false, length = 128, updatable = false)
    var deviceId: String,

    // Hash do refresh token (ex.: SHA-256/Base64). Nunca armazene o token em claro.
    @Column(name = "refresh_hash", nullable = false, length = 128)
    var refreshHash: String,

    // Expiração do refresh (até 23:59:59 do dia corrente)
    @Column(name = "refresh_expires_at", nullable = false)
    var refreshExpiresAt: Instant,

    // Telemetria
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant = Instant.now(),

    @Column(name = "used_at")
    var usedAt: Instant? = null,

    // Encadeamento de rotação: novo.refresh.rotatedFrom = antigo.id
    @Column(name = "rotated_from")
    var rotatedFrom: UUID? = null,

    // Revogação explícita
    @Column(name = "revoked_at")
    var revokedAt: Instant? = null,

    // Reuso detectado: quando um refresh já rotacionado reaparece
    @Column(name = "reuse_detected", nullable = false)
    var reuseDetected: Boolean = false,

    // Contexto opcional
    @Column(name = "ip", length = 45)
    var ip: String? = null,               // IPv4/IPv6

    @Column(name = "user_agent", length = 512)
    var userAgent: String? = null,

    // Escopos e audiência associados à sessão (informativos)
    @Column(name = "scope", length = 512)
    var scope: String? = null,

    @Column(name = "aud", length = 128)
    var aud: String? = null,

    // TTL do access token emitido nesta sessão (segundos). Padrão: 600 (10 min)
    @Column(name = "access_ttl_seconds", nullable = false)
    var accessTtlSeconds: Int = 600
) {

    @get:Transient
    val isExpired: Boolean
        get() = Instant.now().isAfter(refreshExpiresAt)

    @get:Transient
    val isRevoked: Boolean
        get() = revokedAt != null

    @get:Transient
    val isActive: Boolean
        get() = !isExpired && !isRevoked && !reuseDetected

    fun markUsed(at: Instant = Instant.now()) {
        this.usedAt = at
    }

    fun revoke(at: Instant = Instant.now(), reuse: Boolean = false) {
        this.revokedAt = at
        this.reuseDetected = this.reuseDetected || reuse
    }

    /**
     * Rotaciona para um novo registro.
     * Uso típico no service:
     *  - atual.revoke(reuse=false)
     *  - persist(atual)
     *  - persist(novo)
     */
    fun rotateTo(
        newRefreshHash: String,
        newExpiresAt: Instant,
        at: Instant = Instant.now(),
        ip: String? = this.ip,
        userAgent: String? = this.userAgent
    ): AuthEntity {
        this.markUsed(at)
        // Não necessariamente revoga a cadeia inteira aqui; a política decide.
        return AuthEntity(
            id = UUID.randomUUID(),
            userId = this.userId,
            clientId = this.clientId,
            deviceId = this.deviceId,
            refreshHash = newRefreshHash,
            refreshExpiresAt = newExpiresAt,
            createdAt = at,
            rotatedFrom = this.id,
            ip = ip,
            userAgent = userAgent,
            scope = this.scope,
            aud = this.aud,
            accessTtlSeconds = this.accessTtlSeconds
        )
    }
}
