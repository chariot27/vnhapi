// src/main/kotlin/br/ars/vnhapi/grpc/services/ProfileUserCountryGrpcService.kt
package br.ars.vnhapi.grpc.services

import br.ars.vnhapi.domain.ProfileUserCountryEntity
import br.ars.vnhapi.grpc.mappers.mergeInto
import br.ars.vnhapi.grpc.mappers.toEntity
import br.ars.vnhapi.grpc.mappers.toProto
import br.ars.vnhapi.grpc.repositories.ProfileUserCountryRepository
import io.grpc.Status
import io.grpc.stub.StreamObserver
import net.devh.boot.grpc.server.service.GrpcService
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.transaction.annotation.Transactional
import profile_user_country.CreateProfileUserCountryRequest
import profile_user_country.DeleteProfileUserCountryRequest
import profile_user_country.DeleteProfileUserCountryResponse
import profile_user_country.GetProfileUserCountryRequest
import profile_user_country.ListProfileUserCountryRequest
import profile_user_country.ListProfileUserCountryResponse
import profile_user_country.ProfileUserCountry
import profile_user_country.ProfileUserCountryServiceGrpc
import profile_user_country.UpdateProfileUserCountryRequest
import java.util.UUID

@GrpcService
class ProfileUserCountryServiceImpl(
    private val repo: ProfileUserCountryRepository
) : ProfileUserCountryServiceGrpc.ProfileUserCountryServiceImplBase() {

    // CREATE
    @Transactional
    override fun create(
        request: CreateProfileUserCountryRequest,
        responseObserver: StreamObserver<ProfileUserCountry>
    ) {
        try {
            val saved = repo.save(request.toEntity())
            responseObserver.onNext(saved.toProto())
            responseObserver.onCompleted()
        } catch (e: IllegalArgumentException) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.message).asRuntimeException())
        } catch (e: Exception) {
            responseObserver.onError(Status.INTERNAL.withDescription(e.message).asRuntimeException())
        }
    }

    // GET BY ID
    override fun getById(
        request: GetProfileUserCountryRequest,
        responseObserver: StreamObserver<ProfileUserCountry>
    ) {
        try {
            val id = UUID.fromString(request.id)
            val entity = repo.findById(id).orElseThrow { EmptyResultDataAccessException(1) }
            responseObserver.onNext(entity.toProto())
            responseObserver.onCompleted()
        } catch (e: IllegalArgumentException) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("id inválido").asRuntimeException())
        } catch (e: EmptyResultDataAccessException) {
            responseObserver.onError(Status.NOT_FOUND.withDescription("registro não encontrado").asRuntimeException())
        } catch (e: Exception) {
            responseObserver.onError(Status.INTERNAL.withDescription(e.message).asRuntimeException())
        }
    }

    // LIST com filtros e paginação
    override fun list(
        request: ListProfileUserCountryRequest,
        responseObserver: StreamObserver<ListProfileUserCountryResponse>
    ) {
        try {
            val page = if (request.page >= 0) request.page else 0
            val size = if (request.size > 0) request.size else 20
            val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"))

            val idUsuario = request.idUsuario.takeIf { it.isNotBlank() }?.let(UUID::fromString)
            val idPais = request.idPais.takeIf { it.isNotBlank() }?.let(UUID::fromString)

            val pageData = when {
                idUsuario != null && idPais != null ->
                    repo.findAllByIdUsuarioAndIdPais(idUsuario, idPais, pageable)
                idUsuario != null ->
                    repo.findAllByIdUsuario(idUsuario, pageable)
                idPais != null ->
                    repo.findAllByIdPais(idPais, pageable)
                else ->
                    repo.findAll(pageable)
            }

            val resp = ListProfileUserCountryResponse.newBuilder()
                .addAllItems(pageData.content.map(ProfileUserCountryEntity::toProto))
                .setTotal(pageData.totalElements)
                .setPage(pageData.number)
                .setSize(pageData.size)
                .build()

            responseObserver.onNext(resp)
            responseObserver.onCompleted()
        } catch (e: IllegalArgumentException) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.message).asRuntimeException())
        } catch (e: Exception) {
            responseObserver.onError(Status.INTERNAL.withDescription(e.message).asRuntimeException())
        }
    }

    // UPDATE parcial
    @Transactional
    override fun update(
        request: UpdateProfileUserCountryRequest,
        responseObserver: StreamObserver<ProfileUserCountry>
    ) {
        try {
            val id = UUID.fromString(request.id)
            val entity = repo.findById(id).orElseThrow { EmptyResultDataAccessException(1) }
            val merged = request.mergeInto(entity)
            val saved = repo.save(merged)
            responseObserver.onNext(saved.toProto())
            responseObserver.onCompleted()
        } catch (e: IllegalArgumentException) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("id inválido").asRuntimeException())
        } catch (e: EmptyResultDataAccessException) {
            responseObserver.onError(Status.NOT_FOUND.withDescription("registro não encontrado").asRuntimeException())
        } catch (e: Exception) {
            responseObserver.onError(Status.INTERNAL.withDescription(e.message).asRuntimeException())
        }
    }

    // DELETE
    @Transactional
    override fun delete(
        request: DeleteProfileUserCountryRequest,
        responseObserver: StreamObserver<DeleteProfileUserCountryResponse>
    ) {
        try {
            val id = UUID.fromString(request.id)
            repo.deleteById(id)
            responseObserver.onNext(DeleteProfileUserCountryResponse.newBuilder().setSuccess(true).build())
            responseObserver.onCompleted()
        } catch (e: IllegalArgumentException) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("id inválido").asRuntimeException())
        } catch (e: Exception) {
            responseObserver.onError(Status.INTERNAL.withDescription(e.message).asRuntimeException())
        }
    }
}
