// src/main/kotlin/br/ars/vnhapi/grpc/repositories/PublicationRepository.kt
package br.ars.vnhapi.grpc.repositories

import br.ars.vnhapi.domain.PublicationEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface PublicationRepository : JpaRepository<PublicationEntity, UUID> {
    fun findByIdUser(idUser: UUID, pageable: Pageable): Page<PublicationEntity>
}
