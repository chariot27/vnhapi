// src/main/kotlin/br/ars/vnhapi/grpc/repositories/ApiLogRepository.kt
package br.ars.vnhapi.grpc.repositories

import br.ars.vnhapi.domain.ApiLogEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ApiLogRepository : JpaRepository<ApiLogEntity, UUID> {
    fun findAllByStatus(status: String, pageable: Pageable): Page<ApiLogEntity>
}
