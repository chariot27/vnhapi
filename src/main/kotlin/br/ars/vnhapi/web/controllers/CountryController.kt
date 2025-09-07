// src/main/kotlin/br/ars/vnhapi/http/CountryController.kt
package br.ars.vnhapi.http

import br.ars.vnhapi.domain.CountryEntity
import br.ars.vnhapi.grpc.repositories.CountryRepository
import br.ars.vnhapi.shared.constants.ApiUrls
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping(ApiUrls.COUNTRY_ROUTE)
class CountryController(
    private val repo: CountryRepository
) {

    // DTOs
    data class CountryResponse(
        val id: UUID,
        val nome: String,
        val sigla: String
    )

    data class CreateCountryRequest(
        val nome: String,
        val sigla: String
    )

    data class UpdateCountryRequest(
        val nome: String? = null,
        val sigla: String? = null
    )

    private fun CountryEntity.toResponse() =
        CountryResponse(id = requireNotNull(id), nome = nome, sigla = sigla)

    @PostMapping
    @Transactional
    suspend fun create(
        @RequestBody body: CreateCountryRequest
    ): ResponseEntity<CountryResponse> = withContext(Dispatchers.IO) {
        require(body.nome.isNotBlank()) { "nome obrigatório" }
        require(body.sigla.isNotBlank()) { "sigla obrigatória" }

        if (repo.existsBySiglaIgnoreCase(body.sigla)) {
            return@withContext ResponseEntity.status(HttpStatus.CONFLICT).build()
        }

        val saved = repo.save(CountryEntity(nome = body.nome, sigla = body.sigla))
        ResponseEntity.status(HttpStatus.CREATED).body(saved.toResponse())
    }

    @GetMapping("/{id}")
    suspend fun getById(@PathVariable id: String): ResponseEntity<CountryResponse> =
        withContext(Dispatchers.IO) {
            val uuid = runCatching { UUID.fromString(id) }.getOrNull()
                ?: return@withContext ResponseEntity.badRequest().build()

            val entity = repo.findById(uuid).orElse(null)
                ?: return@withContext ResponseEntity.notFound().build()

            ResponseEntity.ok(entity.toResponse())
        }

    @GetMapping
    suspend fun list(): ResponseEntity<List<CountryResponse>> =
        withContext(Dispatchers.IO) {
            val items = repo.findAll().map { it.toResponse() }
            ResponseEntity.ok(items)
        }

    @PutMapping("/{id}")
    @Transactional
    suspend fun update(
        @PathVariable id: String,
        @RequestBody body: UpdateCountryRequest
    ): ResponseEntity<CountryResponse> = withContext(Dispatchers.IO) {
        val uuid = runCatching { UUID.fromString(id) }.getOrNull()
            ?: return@withContext ResponseEntity.badRequest().build()

        val entity = repo.findById(uuid).orElse(null)
            ?: return@withContext ResponseEntity.notFound().build()

        val newNome = body.nome?.takeIf { it.isNotBlank() } ?: entity.nome
        val newSigla = body.sigla?.takeIf { it.isNotBlank() } ?: entity.sigla

        if (!newSigla.equals(entity.sigla, ignoreCase = true) &&
            repo.existsBySiglaIgnoreCase(newSigla)
        ) {
            return@withContext ResponseEntity.status(HttpStatus.CONFLICT).build()
        }

        entity.nome = newNome
        entity.sigla = newSigla

        ResponseEntity.ok(repo.save(entity).toResponse())
    }

    @DeleteMapping("/{id}")
    @Transactional
    suspend fun delete(@PathVariable id: String): ResponseEntity<Void> =
        withContext(Dispatchers.IO) {
            val uuid = runCatching { UUID.fromString(id) }.getOrNull()
                ?: return@withContext ResponseEntity.badRequest().build()

            try {
                repo.deleteById(uuid)
            } catch (_: EmptyResultDataAccessException) {
                // idempotente
            }
            ResponseEntity.noContent().build()
        }
}
