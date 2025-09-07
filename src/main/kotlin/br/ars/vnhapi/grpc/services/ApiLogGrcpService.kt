// src/main/kotlin/br/ars/vnhapi/grpc/services/ApiLogGrpcService.kt
package br.ars.vnhapi.grpc.services

import api_log.*
import br.ars.vnhapi.domain.ApiLogEntity
import br.ars.vnhapi.grpc.mappers.toEntity
import br.ars.vnhapi.grpc.mappers.toProto
import br.ars.vnhapi.grpc.repositories.ApiLogRepository
import io.grpc.Status
import io.grpc.stub.StreamObserver
import net.devh.boot.grpc.server.service.GrpcService
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import java.util.UUID

@GrpcService
class ApiLogGrpcService(
    private val repo: ApiLogRepository
) : ApiLogServiceGrpc.ApiLogServiceImplBase() {

    override fun create(request: CreateApiLogRequest, responseObserver: StreamObserver<ApiLog>) {
        try {
            val saved = repo.save(request.toEntity())
            responseObserver.onNext(saved.toProto()); responseObserver.onCompleted()
        } catch (e: Exception) {
            responseObserver.onError(Status.INTERNAL.withDescription(e.message).asRuntimeException())
        }
    }

    override fun getById(request: GetApiLogRequest, responseObserver: StreamObserver<ApiLog>) {
        try {
            val id = UUID.fromString(request.id)
            val e = repo.findById(id).orElseThrow()
            responseObserver.onNext(e.toProto()); responseObserver.onCompleted()
        } catch (_: NoSuchElementException) {
            responseObserver.onError(Status.NOT_FOUND.withDescription("log não encontrado").asRuntimeException())
        } catch (_: IllegalArgumentException) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("id inválido").asRuntimeException())
        }
    }

    override fun list(request: ListApiLogsRequest, responseObserver: StreamObserver<ListApiLogsResponse>) {
        try {
            val page = if (request.page >= 0) request.page else 0
            val size = if (request.size > 0) request.size else 20
            val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
            val pageData =
                if (request.status.isNotBlank()) repo.findAllByStatus(request.status, pageable)
                else repo.findAll(pageable)

            val resp = ListApiLogsResponse.newBuilder()
                .addAllItems(pageData.content.map(ApiLogEntity::toProto))
                .setTotal(pageData.totalElements)
                .setPage(pageData.number)
                .setSize(pageData.size)
                .build()

            responseObserver.onNext(resp); responseObserver.onCompleted()
        } catch (e: Exception) {
            responseObserver.onError(Status.INTERNAL.withDescription(e.message).asRuntimeException())
        }
    }
}
