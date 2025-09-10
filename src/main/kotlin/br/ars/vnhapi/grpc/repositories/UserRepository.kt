// src/main/kotlin/br/ars/vnhapi/grpc/repositories/UserRepository.kt
package br.ars.vnhapi.grpc.repositories

import br.ars.vnhapi.domain.UserEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface UserRepository : JpaRepository<UserEntity, UUID> {
    fun existsByEmail(email: String): Boolean
    fun existsByUsername(username: String): Boolean

    fun findByEmailIgnoreCase(email: String): UserEntity?
    fun findByUsernameIgnoreCase(username: String): UserEntity?

    @Query("select u.id from UserEntity u where lower(u.email) = lower(:email)")
    fun findIdByEmailIgnoreCase(@Param("email") email: String): UUID?

    @Query("select u.id from UserEntity u where u.username is not null and lower(u.username) = lower(:username)")
    fun findIdByUsernameIgnoreCase(@Param("username") username: String): UUID?

    // pega somente o hash correto
    @Query("select u.password from UserEntity u where u.id = :id")
    fun findPasswordById(@Param("id") id: UUID): String?

    @Query(
        """
        select u from UserEntity u
        where (:q is null 
               or lower(u.email) like lower(concat('%', :q, '%'))
               or lower(coalesce(u.username,'')) like lower(concat('%', :q, '%')))
          and (:onlyActive = false or u.ativo = true)
        """
    )
    fun search(
        @Param("q") q: String?,
        @Param("onlyActive") onlyActive: Boolean,
        pageable: Pageable
    ): Page<UserEntity>
}
