// src/main/kotlin/br/ars/vnhapi/grpc/mappers/ApiLogMappers.kt
package br.ars.vnhapi.grpc.mappers

import api_log.ApiLog
import api_log.CreateApiLogRequest
import br.ars.vnhapi.domain.ApiLogEntity
import java.time.format.DateTimeFormatter

private val ISO = DateTimeFormatter.ISO_OFFSET_DATE_TIME

fun ApiLogEntity.toProto(): ApiLog =
    ApiLog.newBuilder()
        .setId(requireNotNull(id).toString())
        .setMensagem(mensagem)
        .setLocalizacao(localizacao)
        .setStatus(status)
        .setCreatedAt(createdAt?.format(ISO) ?: "")
        .build()

fun CreateApiLogRequest.toEntity(): ApiLogEntity =
    ApiLogEntity(
        mensagem = mensagem.ifBlank { "-" },
        localizacao = localizacao.ifBlank { "-" },
        status = status.ifBlank { "correto" }
    )
