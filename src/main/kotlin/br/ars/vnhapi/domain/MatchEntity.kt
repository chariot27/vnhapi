// src/main/kotlin/br/ars/vnhapi/domain/match/MatchEntity.kt
package br.ars.vnhapi.domain

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "matches",
    indexes = [
        Index(name = "ix_match_from", columnList = "id_usuario_from"),
        Index(name = "ix_match_to", columnList = "id_usuario_to"),
        Index(name = "ix_match_status", columnList = "status")
    ]
)
class MatchEntity(
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    var id: UUID = UUID.randomUUID(),

    @Column(name = "id_usuario_from", nullable = false, updatable = false)
    var idUsuarioFrom: UUID,

    @Column(name = "id_usuario_to", nullable = false, updatable = false)
    var idUsuarioTo: UUID,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: MatchStatus = MatchStatus.PENDING,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
) {
    @PrePersist fun prePersist() {
        val now = Instant.now()
        createdAt = now
        updatedAt = now
    }
    @PreUpdate fun preUpdate() { updatedAt = Instant.now() }
}

enum class MatchStatus { PENDING, ACCEPTED, REJECTED, CANCELED }
