// src/main/kotlin/br/ars/vnhapi/http/UsersController.kt
package br.ars.vnhapi.http

import br.ars.vnhapi.proto.*
import br.ars.vnhapi.shared.constants.ApiUrls
import kotlinx.coroutines.runBlocking
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(ApiUrls.USER_ROUTE)
class UsersController(
    // Chamando diretamente o serviço gRPC registrado como bean Spring
    private val userServiceImpl: br.ars.vnhapi.grpc.services.UserServiceImpl
) {

    @PostMapping
    fun create(@RequestBody body: CreateUserJson): UserJson = runBlocking {
        val req = CreateUserRequest.newBuilder()
            .setEmail(body.email)
            .setPassword(body.password)
            .setUsername(body.username ?: "")
            .setAvatar(body.avatar ?: "")
            .setPhone(body.phone ?: "")
            .setBio(body.bio ?: "")
            .addAllTags(body.tags ?: emptyList())
            .setAtivo(body.ativo ?: false)
            .build()

        userServiceImpl.create(req).toJson()
    }

    @GetMapping("/{id}")
    fun get(@PathVariable id: String): UserJson = runBlocking {
        userServiceImpl.get(GetUserRequest.newBuilder().setId(id).build()).toJson()
    }

    @PatchMapping("/{id}")
    fun update(@PathVariable id: String, @RequestBody body: UpdateUserJson): UserJson = runBlocking {
        val b = UpdateUserRequest.newBuilder().setId(id)

        body.avatar?.let   { b.setAvatar(it) }
        body.username?.let { b.setUsername(it) }
        body.phone?.let    { b.setPhone(it) }
        body.bio?.let      { b.setBio(it) }
        body.password?.let { b.setPassword(it) }
        body.ativo?.let    { b.setAtivo(it) }
        body.tags?.let     { if (it.isNotEmpty()) b.addAllTags(it) }

        userServiceImpl.update(b.build()).toJson()
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: String): Unit = runBlocking {
        userServiceImpl.delete(DeleteUserRequest.newBuilder().setId(id).build())
        Unit
    }

    @GetMapping
    fun list(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "") q: String,
        @RequestParam(defaultValue = "false") onlyActive: Boolean
    ): ListUsersJson = runBlocking {
        val resp = userServiceImpl.list(
            ListUsersRequest.newBuilder()
                .setPage(page)
                .setSize(size)
                .setQ(q)
                .setOnlyActive(onlyActive)
                .build()
        )
        ListUsersJson(
            items = resp.itemsList.map { it.toJson() },
            total = resp.total,
            page = resp.page,
            size = resp.size
        )
    }

    // ----- DTOs -----
    data class CreateUserJson(
        val email: String,
        val password: String,
        val username: String? = null,
        val avatar: String? = null,
        val phone: String? = null,
        val bio: String? = null,
        val tags: List<String>? = null,
        val ativo: Boolean? = null
    )

    data class UpdateUserJson(
        val username: String? = null,
        val avatar: String? = null,
        val phone: String? = null,
        val bio: String? = null,
        val password: String? = null,
        val tags: List<String>? = null,
        val ativo: Boolean? = null
    )

    data class UserJson(
        val id: String,
        val email: String,
        val username: String?,
        val avatar: String?,
        val phone: String?,
        val bio: String?,
        val tags: List<String>,
        val ativo: Boolean
    )

    data class ListUsersJson(
        val items: List<UserJson>,
        val total: Long,
        val page: Int,
        val size: Int
    )

    // ----- mappers -----
    private fun User.toJson() = UserJson(
        id = id,
        email = email,
        username = if (username.isBlank()) null else username,
        avatar = if (avatar.isBlank()) null else avatar,
        phone = if (phone.isBlank()) null else phone,
        bio = if (bio.isBlank()) null else bio,
        tags = tagsList,
        ativo = ativo
    )
}
