// src/main/kotlin/br/ars/vnhapi/grpc/services/UserServiceImpl.kt
package br.ars.vnhapi.grpc.services

import br.ars.vnhapi.grpc.mappers.toProto
import br.ars.vnhapi.domain.UserEntity
import br.ars.vnhapi.grpc.repositories.UserRepository
import br.ars.vnhapi.proto.CreateUserRequest
import br.ars.vnhapi.proto.DeleteUserRequest
import br.ars.vnhapi.proto.EmptyResponse
import br.ars.vnhapi.proto.GetUserRequest
import br.ars.vnhapi.proto.UpdateUserRequest
import br.ars.vnhapi.proto.User
import br.ars.vnhapi.proto.UserServiceGrpcKt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.devh.boot.grpc.server.service.GrpcService
import org.springframework.security.crypto.bcrypt.BCrypt
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@GrpcService
class UserServiceImpl(
    private val repo: UserRepository
) : UserServiceGrpcKt.UserServiceCoroutineImplBase() {

    @Transactional
    override suspend fun create(request: CreateUserRequest): User = withContext(Dispatchers.IO) {
        require(request.email.isNotBlank()) { "email required" }
        require(request.password.isNotBlank()) { "password required" }

        val saved = repo.save(
            UserEntity(
                avatar   = request.avatar.takeIf { it.isNotBlank() },
                email    = request.email,
                username = request.username.takeIf { it.isNotBlank() }, // entidade aceita null
                password = BCrypt.hashpw(request.password, BCrypt.gensalt()),
                phone    = request.phone.takeIf { it.isNotBlank() },
                bio      = request.bio.takeIf { it.isNotBlank() },
                tags     = request.tagsList,
                ativo    = request.ativo
            )
        )
        saved.toProto()
    }

    override suspend fun get(request: GetUserRequest): User = withContext(Dispatchers.IO) {
        val id = UUID.fromString(request.id)
        repo.findById(id).orElseThrow { NoSuchElementException("user not found") }.toProto()
    }

    @Transactional
    override suspend fun update(request: UpdateUserRequest): User = withContext(Dispatchers.IO) {
        val id = UUID.fromString(request.id)
        val e = repo.findById(id).orElseThrow { NoSuchElementException("user not found") }

        if (request.hasAvatar())   e.avatar   = request.avatar.takeIf { it.isNotBlank() }
        if (request.hasUsername()) e.username = request.username.takeIf { it.isNotBlank() }
        if (request.hasPhone())    e.phone    = request.phone.takeIf { it.isNotBlank() }
        if (request.hasBio())      e.bio      = request.bio.takeIf { it.isNotBlank() }
        if (request.tagsCount > 0) e.tags     = request.tagsList
        if (request.hasAtivo())    e.ativo    = request.ativo
        if (request.hasPassword() && request.password.isNotBlank()) {
            e.password = BCrypt.hashpw(request.password, BCrypt.gensalt())
        }

        repo.save(e).toProto()
    }

    @Transactional
    override suspend fun delete(request: DeleteUserRequest): EmptyResponse = withContext(Dispatchers.IO) {
        val id = UUID.fromString(request.id)
        if (repo.existsById(id)) repo.deleteById(id)
        EmptyResponse.getDefaultInstance()
    }
}
