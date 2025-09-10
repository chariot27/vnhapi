// src/main/kotlin/br/ars/vnhapi/grpc/mappers/MatchMappers.kt
package br.ars.vnhapi.grpc.mappers

import br.ars.vnhapi.domain.MatchEntity
import br.ars.vnhapi.domain.MatchStatus

data class MatchDTO(
    val id: String,
    val idUsuarioFrom: String,
    val idUsuarioTo: String,
    val status: String,
    val createdAt: Long,
    val updatedAt: Long
)

object MatchMappers {
    fun toDTO(e: MatchEntity) = MatchDTO(
        id = e.id.toString(),
        idUsuarioFrom = e.idUsuarioFrom.toString(),
        idUsuarioTo = e.idUsuarioTo.toString(),
        status = e.status.name,
        createdAt = e.createdAt.toEpochMilli(),
        updatedAt = e.updatedAt.toEpochMilli()
    )

    fun parseStatus(value: String?): MatchStatus? =
        value?.uppercase()?.let { runCatching { MatchStatus.valueOf(it) }.getOrNull() }
}
