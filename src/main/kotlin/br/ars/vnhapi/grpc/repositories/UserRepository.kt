// src/main/kotlin/br/ars/vnhapi/grpc/repositories/UserRepository.kt
package br.ars.vnhapi.grpc.repositories

import br.ars.vnhapi.domain.UserEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface UserRepository : JpaRepository<UserEntity, UUID> {
    fun existsByEmail(email: String): Boolean
    fun existsByUsername(username: String): Boolean

    @Query("""
        select u from UserEntity u
        where (:q is null or lower(u.email) like lower(concat('%', :q, '%'))
               or lower(u.username) like lower(concat('%', :q, '%')))
          and (:onlyActive = false or u.ativo = true)
    """)
    fun search(q: String?, onlyActive: Boolean, pageable: Pageable): Page<UserEntity>
}
