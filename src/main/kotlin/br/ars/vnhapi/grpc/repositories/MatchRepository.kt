// src/main/kotlin/br/ars/vnhapi/grpc/repositories/MatchRepository.kt
package br.ars.vnhapi.grpc.repositories

import br.ars.vnhapi.domain.MatchEntity
import br.ars.vnhapi.domain.MatchStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface MatchRepository : JpaRepository<MatchEntity, UUID> {

    fun findByIdUsuarioFromAndIdUsuarioTo(
        idUsuarioFrom: UUID, idUsuarioTo: UUID
    ): Optional<MatchEntity>

    fun findByIdUsuarioFrom(
        idUsuarioFrom: UUID, pageable: Pageable
    ): Page<MatchEntity>

    fun findByIdUsuarioTo(
        idUsuarioTo: UUID, pageable: Pageable
    ): Page<MatchEntity>

    fun findByIdUsuarioFromAndStatus(
        idUsuarioFrom: UUID, status: MatchStatus, pageable: Pageable
    ): Page<MatchEntity>

    fun findByIdUsuarioToAndStatus(
        idUsuarioTo: UUID, status: MatchStatus, pageable: Pageable
    ): Page<MatchEntity>
}
