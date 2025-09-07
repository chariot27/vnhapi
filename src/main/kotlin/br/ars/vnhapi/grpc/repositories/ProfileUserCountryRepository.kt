// src/main/kotlin/br/ars/vnhapi/grpc/repositories/ProfileUserCountryRepository.kt
package br.ars.vnhapi.grpc.repositories

import br.ars.vnhapi.domain.ProfileUserCountryEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ProfileUserCountryRepository : JpaRepository<ProfileUserCountryEntity, UUID> {
    fun findAllByIdUsuario(idUsuario: UUID, pageable: Pageable): Page<ProfileUserCountryEntity>
    fun findAllByIdPais(idPais: UUID, pageable: Pageable): Page<ProfileUserCountryEntity>
    fun findAllByIdUsuarioAndIdPais(idUsuario: UUID, idPais: UUID, pageable: Pageable): Page<ProfileUserCountryEntity>
}
