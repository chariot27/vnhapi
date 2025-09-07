// src/main/kotlin/br/ars/vnhapi/grpc/services/CountryGrpcService.kt
package br.ars.vnhapi.grpc.services

import br.ars.vnhapi.domain.CountryEntity
import br.ars.vnhapi.grpc.repositories.CountryRepository
import country.CountryOuterClass
import country.CountryServiceGrpcKt
import io.grpc.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class CountryGrpcService(
    private val repo: CountryRepository
) : CountryServiceGrpcKt.CountryServiceCoroutineImplBase() {

    // ========= Helpers de conversão =========
    private fun CountryEntity.toProto(): CountryOuterClass.Country =
        CountryOuterClass.Country.newBuilder()
            .setId(requireNotNull(id).toString())
            .setNome(nome)
            .setSigla(sigla)
            .build()

    private fun CountryOuterClass.Country.toEntity(): CountryEntity =
        CountryEntity(
            id = if (id.isBlank()) null else UUID.fromString(id),
            nome = nome,
            sigla = sigla
        )
    // ========================================

    @Transactional
    override suspend fun createCountry(
        request: CountryOuterClass.Country
    ): CountryOuterClass.Country = withContext(Dispatchers.IO) {
        require(request.nome.isNotBlank()) { "nome obrigatório" }
        require(request.sigla.isNotBlank()) { "sigla obrigatória" }

        if (repo.existsBySiglaIgnoreCase(request.sigla)) {
            throw Status.ALREADY_EXISTS.withDescription("sigla já existe").asRuntimeException()
        }

        val saved = repo.save(request.toEntity())
        saved.toProto()
    }

    override suspend fun getCountryById(
        request: CountryOuterClass.GetCountryRequest
    ): CountryOuterClass.Country = withContext(Dispatchers.IO) {
        val id = runCatching { UUID.fromString(request.id) }
            .getOrElse { throw Status.INVALID_ARGUMENT.withDescription("id inválido").asRuntimeException() }

        val entity = repo.findById(id).orElseThrow {
            Status.NOT_FOUND.withDescription("country não encontrado").asRuntimeException()
        }
        entity.toProto()
    }

    override suspend fun listCountries(
        request: CountryOuterClass.ListCountriesRequest
    ): CountryOuterClass.ListCountriesResponse = withContext(Dispatchers.IO) {
        val items = repo.findAll().map { it.toProto() }
        CountryOuterClass.ListCountriesResponse.newBuilder()
            .addAllCountries(items)
            .build()
    }

    @Transactional
    override suspend fun updateCountry(
        request: CountryOuterClass.Country
    ): CountryOuterClass.Country = withContext(Dispatchers.IO) {
        if (request.id.isBlank()) {
            throw Status.INVALID_ARGUMENT.withDescription("id obrigatório").asRuntimeException()
        }

        val id = runCatching { UUID.fromString(request.id) }
            .getOrElse { throw Status.INVALID_ARGUMENT.withDescription("id inválido").asRuntimeException() }

        val entity = repo.findById(id).orElseThrow {
            Status.NOT_FOUND.withDescription("country não encontrado").asRuntimeException()
        }

        val newNome = if (request.nome.isNotBlank()) request.nome else entity.nome
        val newSigla = if (request.sigla.isNotBlank()) request.sigla else entity.sigla

        if (!newSigla.equals(entity.sigla, ignoreCase = true) &&
            repo.existsBySiglaIgnoreCase(newSigla)
        ) {
            throw Status.ALREADY_EXISTS.withDescription("sigla já existe").asRuntimeException()
        }

        entity.nome = newNome
        entity.sigla = newSigla

        repo.save(entity).toProto()
    }

    @Transactional
    override suspend fun deleteCountry(
        request: CountryOuterClass.DeleteCountryRequest
    ): CountryOuterClass.DeleteCountryResponse = withContext(Dispatchers.IO) {
        val id = runCatching { UUID.fromString(request.id) }
            .getOrElse { throw Status.INVALID_ARGUMENT.withDescription("id inválido").asRuntimeException() }

        val success = try {
            repo.deleteById(id); true
        } catch (_: EmptyResultDataAccessException) {
            true // idempotente
        }

        CountryOuterClass.DeleteCountryResponse.newBuilder()
            .setSuccess(success)
            .build()
    }
}
