// src/main/kotlin/br/ars/vnhapi/grpc/services/MatchServiceImpl.kt
package br.ars.vnhapi.grpc.services

import br.ars.vnhapi.domain.MatchEntity
import br.ars.vnhapi.domain.MatchStatus
import br.ars.vnhapi.grpc.mappers.MatchMappers
import br.ars.vnhapi.grpc.mappers.MatchDTO
import br.ars.vnhapi.grpc.repositories.MatchRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class MatchServiceImpl(
    private val repo: MatchRepository
) {

    @Transactional
    suspend fun sendInvite(idFrom: String, idTo: String): MatchDTO = withContext(Dispatchers.IO) {
        val from = UUID.fromString(idFrom)
        val to = UUID.fromString(idTo)
        require(from != to) { "id_usuario_from cannot equal id_usuario_to" }

        val existing = repo.findByIdUsuarioFromAndIdUsuarioTo(from, to)
        if (existing.isPresent) {
            // se já existe pendente, retorne; se estava rejeitado, pode reabrir regra de negócio sua
            return@withContext MatchMappers.toDTO(existing.get())
        }

        val e = MatchEntity(
            idUsuarioFrom = from,
            idUsuarioTo = to,
            status = MatchStatus.PENDING
        )
        MatchMappers.toDTO(repo.save(e))
    }

    @Transactional
    suspend fun respondInvite(id: String, accept: Boolean): MatchDTO = withContext(Dispatchers.IO) {
        val uid = UUID.fromString(id)
        val e = repo.findById(uid).orElseThrow { NoSuchElementException("invite not found") }
        e.status = if (accept) MatchStatus.ACCEPTED else MatchStatus.REJECTED
        MatchMappers.toDTO(repo.save(e))
    }

    suspend fun getInvite(id: String): MatchDTO = withContext(Dispatchers.IO) {
        val uid = UUID.fromString(id)
        MatchMappers.toDTO(repo.findById(uid).orElseThrow { NoSuchElementException("invite not found") })
    }

    suspend fun listInvites(
        userId: String,
        role: String?,                 // "from" | "to" | null
        status: String?,               // PENDING, ACCEPTED, ...
        page: Int, size: Int
    ): Page<MatchDTO> = withContext(Dispatchers.IO) {
        val uid = UUID.fromString(userId)
        val pageable = PageRequest.of(maxOf(page - 1, 0), size.coerceIn(1, 200))
        val st = MatchMappers.parseStatus(status)

        val pageRes = when (role?.lowercase()) {
            "from" -> if (st != null) repo.findByIdUsuarioFromAndStatus(uid, st, pageable)
            else repo.findByIdUsuarioFrom(uid, pageable)
            "to"   -> if (st != null) repo.findByIdUsuarioToAndStatus(uid, st, pageable)
            else repo.findByIdUsuarioTo(uid, pageable)
            else   -> {
                // role indefinido: mescle client-side. Aqui retornamos os "to" por padrão.
                if (st != null) repo.findByIdUsuarioToAndStatus(uid, st, pageable)
                else repo.findByIdUsuarioTo(uid, pageable)
            }
        }

        pageRes.map { MatchMappers.toDTO(it) }
    }
}
