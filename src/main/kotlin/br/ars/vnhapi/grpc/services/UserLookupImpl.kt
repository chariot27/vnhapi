// src/main/kotlin/br/ars/vnhapi/grpc/services/UserLookupImpl.kt
package br.ars.vnhapi.grpc.services

import br.ars.vnhapi.grpc.repositories.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class UserLookupImpl(
    private val repo: UserRepository,
    private val encoder: PasswordEncoder
) : UserLookup {

    @Transactional(readOnly = true)
    override fun findIdByUsername(usernameOrEmail: String): UUID? =
        repo.findIdByEmailIgnoreCase(usernameOrEmail)
            ?: repo.findIdByUsernameIgnoreCase(usernameOrEmail)

    @Transactional(readOnly = true)
    override fun passwordMatches(userId: UUID, raw: String): Boolean {
        val hash = repo.findPasswordById(userId) ?: return false
        return encoder.matches(raw, hash)
    }
}
