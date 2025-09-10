// src/main/kotlin/br/ars/vnhapi/grpc/repositories/FilterRepository.kt
package br.ars.vnhapi.grpc.repositories

import br.ars.vnhapi.domain.PublicationEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.*

interface FilterRepository : JpaRepository<PublicationEntity, UUID> {

    @Query("""
    SELECT p FROM PublicationEntity p
    WHERE (:cursor IS NULL OR p.createdAt < :cursor)
      AND LOWER(p.userTags) LIKE CONCAT('%', LOWER(:tag), '%')
    ORDER BY p.createdAt DESC
""")
    fun findByTag(
        @Param("tag") tag: String,
        @Param("cursor") cursor: Instant?
    ): List<PublicationEntity>

}
