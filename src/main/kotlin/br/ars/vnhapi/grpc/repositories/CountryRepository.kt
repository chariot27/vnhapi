// src/main/kotlin/br/ars/vnhapi/grpc/repositories/CountryRepository.kt
package br.ars.vnhapi.grpc.repositories

import br.ars.vnhapi.domain.CountryEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface CountryRepository : JpaRepository<CountryEntity, UUID> {
    fun existsBySiglaIgnoreCase(sigla: String): Boolean
}
