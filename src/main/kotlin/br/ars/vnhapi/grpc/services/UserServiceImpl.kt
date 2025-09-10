// src/main/kotlin/br/ars/vnhapi/grpc/services/UserServiceImpl.kt
package br.ars.vnhapi.grpc.services

import br.ars.vnhapi.domain.UserEntity
import br.ars.vnhapi.grpc.mappers.toProto
import br.ars.vnhapi.grpc.repositories.UserRepository
import br.ars.vnhapi.proto.*
import br.ars.vnhapi.shared.clients.BunnyCdnStorage
import com.google.protobuf.Empty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.devh.boot.grpc.server.service.GrpcService
import org.springframework.security.crypto.bcrypt.BCrypt
import org.springframework.transaction.annotation.Transactional
import java.util.*

@GrpcService
class UserServiceImpl(
    private val repo: UserRepository
) : UserServiceGrpcKt.UserServiceCoroutineImplBase() {

    @Transactional
    override suspend fun create(request: CreateUserRequest): User = withContext(Dispatchers.IO) {
        require(request.email.isNotBlank()) { "email required" }
        require(request.password.isNotBlank()) { "password required" }

        var avatarPath: String? = request.avatar.takeIf { it.isNotBlank() }

        // Se vier base64 data URL, sobe no CDN antes de salvar
        if (BunnyCdnStorage.looksLikeBase64Data(avatarPath)) {
            val tempId = UUID.randomUUID()
            val uploaded = BunnyCdnStorage.uploadUserAvatar(tempId, avatarPath!!)
            avatarPath = uploaded.replace(tempId.toString(), "{id}")
        }

        // cria entidade
        var entity = UserEntity(
            avatar   = avatarPath,
            email    = request.email,
            username = request.username.takeIf { it.isNotBlank() },
            password = BCrypt.hashpw(request.password, BCrypt.gensalt()),
            phone    = request.phone.takeIf { it.isNotBlank() },
            bio      = request.bio.takeIf { it.isNotBlank() },
            tags     = request.tagsList,
            ativo    = request.ativo
        )

        // salva para obter ID definitivo
        entity = repo.save(entity)

        // Se subimos com {id}, re-sobe no caminho final com o ID real
        if (entity.avatar?.contains("{id}") == true && request.avatar.isNotBlank()) {
            val finalPath = entity.avatar!!.replace("{id}", entity.id.toString())
            BunnyCdnStorage.deleteObject(finalPath)
            val uploaded = BunnyCdnStorage.uploadUserAvatar(entity.id!!, request.avatar)
            entity.avatar = uploaded
            entity = repo.save(entity)
        }

        entity.toProto()
    }

    override suspend fun get(request: GetUserRequest): User = withContext(Dispatchers.IO) {
        val id = UUID.fromString(request.id)
        repo.findById(id).orElseThrow { NoSuchElementException("user not found") }.toProto()
    }

    @Transactional
    override suspend fun update(request: UpdateUserRequest): User = withContext(Dispatchers.IO) {
        val id = UUID.fromString(request.id)
        val e = repo.findById(id).orElseThrow { NoSuchElementException("user not found") }

        if (request.hasAvatar()) {
            val incoming = request.avatar
            when {
                incoming.isBlank() -> {
                    e.avatar?.let { BunnyCdnStorage.deleteObject(it) }
                    e.avatar = null
                }
                BunnyCdnStorage.looksLikeBase64Data(incoming) -> {
                    e.avatar?.let { BunnyCdnStorage.deleteObject(it) }
                    val uploaded = BunnyCdnStorage.uploadUserAvatar(e.id!!, incoming)
                    e.avatar = uploaded
                }
                else -> {
                    e.avatar = incoming
                }
            }
        }

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
    override suspend fun delete(request: DeleteUserRequest): Empty = withContext(Dispatchers.IO) {
        val id = UUID.fromString(request.id)
        val user = repo.findById(id)
        if (user.isPresent) {
            user.get().avatar?.let { BunnyCdnStorage.deleteObject(it) }
            repo.deleteById(id)
        }
        Empty.getDefaultInstance()
    }
}
