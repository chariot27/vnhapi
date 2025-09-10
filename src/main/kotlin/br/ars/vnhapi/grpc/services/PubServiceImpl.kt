// src/main/kotlin/br/ars/vnhapi/grpc/services/PubServiceImpl.kt
package br.ars.vnhapi.grpc.services

import br.ars.vnhapi.domain.PublicationEntity
import br.ars.vnhapi.grpc.mappers.toProto
import br.ars.vnhapi.grpc.repositories.PublicationRepository
import br.ars.vnhapi.proto.*
import com.google.protobuf.Empty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.devh.boot.grpc.server.service.GrpcService
import org.springframework.data.domain.PageRequest
import org.springframework.transaction.annotation.Transactional
import java.util.*

@GrpcService
class PubServiceImpl(
    private val repo: PublicationRepository
) : PubServiceGrpcKt.PubServiceCoroutineImplBase() {

    @Transactional
    override suspend fun create(request: CreatePublicationRequest): Publication = withContext(Dispatchers.IO) {
        val entity = PublicationEntity(
            idUser = UUID.fromString(request.idUser),
            pubType = request.pubType,
            userTags = request.userTags.takeIf { it.isNotBlank() },
            postImgCdnPath = request.postImgCdnPath.takeIf { it.isNotBlank() },
            postVidCdnPath = request.postVidCdnPath.takeIf { it.isNotBlank() },
            postTxt = request.postTxt.takeIf { it.isNotBlank() }
        )
        repo.save(entity).toProto()
    }

    override suspend fun get(request: GetPublicationRequest): Publication = withContext(Dispatchers.IO) {
        val id = UUID.fromString(request.id)
        repo.findById(id).orElseThrow { NoSuchElementException("publication not found") }.toProto()
    }

    @Transactional
    override suspend fun update(request: UpdatePublicationRequest): Publication = withContext(Dispatchers.IO) {
        val id = UUID.fromString(request.id)
        val e = repo.findById(id).orElseThrow { NoSuchElementException("publication not found") }

        if (request.hasPubType())      e.pubType = request.pubType
        if (request.hasUserTags())     e.userTags = request.userTags.takeIf { it.isNotBlank() }
        if (request.hasPostImgCdnPath()) e.postImgCdnPath = request.postImgCdnPath.takeIf { it.isNotBlank() }
        if (request.hasPostVidCdnPath()) e.postVidCdnPath = request.postVidCdnPath.takeIf { it.isNotBlank() }
        if (request.hasPostTxt())      e.postTxt = request.postTxt.takeIf { it.isNotBlank() }

        repo.save(e).toProto()
    }

    @Transactional
    override suspend fun delete(request: DeletePublicationRequest): Empty = withContext(Dispatchers.IO) {
        val id = UUID.fromString(request.id)
        if (repo.existsById(id)) repo.deleteById(id)
        Empty.getDefaultInstance()
    }

    override suspend fun list(request: ListPublicationsRequest): ListPublicationsResponse = withContext(Dispatchers.IO) {
        val pageable = PageRequest.of(
            if (request.page > 0) request.page else 0,
            if (request.size > 0) request.size else 20
        )

        val page = if (request.idUser.isNotBlank()) {
            repo.findByIdUser(UUID.fromString(request.idUser), pageable)
        } else {
            repo.findAll(pageable)
        }

        ListPublicationsResponse.newBuilder()
            .addAllItems(page.content.map { it.toProto() })
            .setTotal(page.totalElements)
            .setPage(pageable.pageNumber)
            .setSize(pageable.pageSize)
            .build()
    }
}
