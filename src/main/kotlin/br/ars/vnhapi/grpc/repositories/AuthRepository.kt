// src/main/kotlin/br/ars/vnhapi/grpc/repositories/AuthRepository.kt
package br.ars.vnhapi.grpc.repositories

import br.ars.vnhapi.domain.AuthEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.Instant
import java.util.*

interface AuthRepository : JpaRepository<AuthEntity, UUID> {

    fun findByRefreshHash(refreshHash: String): Optional<AuthEntity>

    @Query(
        """
        select a from AuthEntity a
        where a.userId = :userId
          and a.clientId = :clientId
          and a.deviceId = :deviceId
          and a.revokedAt is null
          and a.reuseDetected = false
          and a.refreshExpiresAt > :now
        """
    )
    fun findActiveSession(
        userId: UUID,
        clientId: String,
        deviceId: String,
        now: Instant
    ): List<AuthEntity>
}
