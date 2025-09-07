// src/main/kotlin/br/ars/vnhapi/domain/CountryDTO.kt
package br.ars.vnhapi.domain

import java.util.UUID

data class CountryDTO(
    val id: UUID? = null,
    val nome: String,
    val sigla: String
)
