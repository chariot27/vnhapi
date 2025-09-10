// src/main/kotlin/br/ars/vnhapi/grpc/services/FilterServiceImpl.kt
package br.ars.vnhapi.grpc.services

import br.ars.vnhapi.domain.PublicationEntity
import br.ars.vnhapi.grpc.mappers.toProto
import br.ars.vnhapi.grpc.repositories.FilterRepository
import br.ars.vnhapi.proto.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.devh.boot.grpc.server.service.GrpcService
import java.time.Instant
import jakarta.persistence.EntityManager
import jakarta.persistence.criteria.Predicate

@GrpcService
class FilterServiceImpl(
    private val repo: FilterRepository,
    private val em: EntityManager
) : FilterServiceGrpcKt.FilterServiceCoroutineImplBase() {

    override suspend fun filterByTags(request: FilterByTagsRequest): FilterByTagsResponse =
        withContext(Dispatchers.IO) {
            val limit = if (request.limit > 0) request.limit else 20
            val cursorInstant = request.cursor.takeIf { it.isNotBlank() }?.let {
                Instant.ofEpochSecond(it.toLong())
            }

            val cb = em.criteriaBuilder
            val cq = cb.createQuery(PublicationEntity::class.java)
            val root = cq.from(PublicationEntity::class.java)

            val predicates = mutableListOf<Predicate>()

            // cursor (createdAt < cursor)
            cursorInstant?.let {
                predicates.add(cb.lessThan(root.get("createdAt"), it))
            }

            // tags
            if (request.tagsCount > 0) {
                val tagsPredicates = request.tagsList.map { tag ->
                    cb.like(cb.lower(root.get("userTags")), "%${tag.lowercase()}%")
                }
                if (request.matchAll) {
                    predicates.add(cb.and(*tagsPredicates.toTypedArray()))
                } else {
                    predicates.add(cb.or(*tagsPredicates.toTypedArray()))
                }
            }

            cq.where(*predicates.toTypedArray())
            cq.orderBy(cb.desc(root.get<Instant>("createdAt")))

            val query = em.createQuery(cq)
            query.maxResults = limit + 1 // buscar 1 extra p/ saber se há mais
            val resultList = query.resultList

            val items = resultList.take(limit).map { it.toProto() }
            val nextCursor = if (resultList.size > limit) {
                items.last().createdAt.toString()
            } else ""

            FilterByTagsResponse.newBuilder()
                .addAllItems(items)
                .setNextCursor(nextCursor)
                .setHasMore(resultList.size > limit)
                .build()
        }
}
