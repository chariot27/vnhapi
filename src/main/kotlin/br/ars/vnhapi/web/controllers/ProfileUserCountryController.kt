// src/main/kotlin/br/ars/vnhapi/http/ProfileUserCountryController.kt
package br.ars.vnhapi.http

import br.ars.vnhapi.domain.ProfileUserCountryEntity
import br.ars.vnhapi.grpc.mappers.mergeInto
import br.ars.vnhapi.grpc.mappers.toEntity
import br.ars.vnhapi.grpc.mappers.toProto
import br.ars.vnhapi.grpc.repositories.ProfileUserCountryRepository
import br.ars.vnhapi.shared.constants.ApiUrls
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import profile_user_country.CreateProfileUserCountryRequest
import profile_user_country.ListProfileUserCountryResponse
import profile_user_country.ProfileUserCountry
import profile_user_country.UpdateProfileUserCountryRequest
import java.util.UUID

@RestController
@RequestMapping(ApiUrls.PROFILE_ROUTE)
@Validated
class ProfileUserCountryController(
    private val repo: ProfileUserCountryRepository
) {

    // CREATE
    @PostMapping
    @Transactional
    suspend fun create(@RequestBody body: CreateProfileUserCountryRequest)
            : ResponseEntity<ProfileUserCountry> = withContext(Dispatchers.IO) {
        val saved = repo.save(body.toEntity())
        ResponseEntity.status(HttpStatus.CREATED).body(saved.toProto())
    }

    // GET BY ID
    @GetMapping("/{id}")
    suspend fun getById(@PathVariable id: String)
            : ResponseEntity<ProfileUserCountry> = withContext(Dispatchers.IO) {
        val uuid = UUID.fromString(id)
        val entity = repo.findById(uuid).orElseThrow { EmptyResultDataAccessException(1) }
        ResponseEntity.ok(entity.toProto())
    }

    // LIST com filtros opcionais e paginação
    @GetMapping
    suspend fun list(
        @RequestParam(required = false) idUsuario: String?,
        @RequestParam(required = false) idPais: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ListProfileUserCountryResponse> = withContext(Dispatchers.IO) {
        val pageable = PageRequest.of(page.coerceAtLeast(0), size.coerceAtLeast(1), Sort.by(Sort.Direction.DESC, "id"))

        val u = idUsuario?.takeIf { it.isNotBlank() }?.let(UUID::fromString)
        val p = idPais?.takeIf { it.isNotBlank() }?.let(UUID::fromString)

        val data = when {
            u != null && p != null -> repo.findAllByIdUsuarioAndIdPais(u, p, pageable)
            u != null -> repo.findAllByIdUsuario(u, pageable)
            p != null -> repo.findAllByIdPais(p, pageable)
            else -> repo.findAll(pageable)
        }

        val resp = ListProfileUserCountryResponse.newBuilder()
            .addAllItems(data.content.map(ProfileUserCountryEntity::toProto))
            .setTotal(data.totalElements)
            .setPage(data.number)
            .setSize(data.size)
            .build()

        ResponseEntity.ok(resp)
    }

    // UPDATE parcial
    @PutMapping("/{id}")
    @Transactional
    suspend fun update(
        @PathVariable id: String,
        @RequestBody body: UpdateProfileUserCountryRequest
    ): ResponseEntity<ProfileUserCountry> = withContext(Dispatchers.IO) {
        val uuid = UUID.fromString(id)
        val entity = repo.findById(uuid).orElseThrow { EmptyResultDataAccessException(1) }

        // garante que o ID do path prevalece
        val bodyWithId = body.toBuilder().setId(uuid.toString()).build()

        val merged = bodyWithId.mergeInto(entity)
        val saved = repo.save(merged)
        ResponseEntity.ok(saved.toProto())
    }

    // DELETE
    @DeleteMapping("/{id}")
    @Transactional
    suspend fun delete(@PathVariable id: String): ResponseEntity<Void> = withContext(Dispatchers.IO) {
        val uuid = UUID.fromString(id)
        repo.deleteById(uuid)
        ResponseEntity.noContent().build()
    }
}
