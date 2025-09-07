// src/main/kotlin/br/ars/vnhapi/grpc/mappers/ProfileUserCountryMappers.kt
package br.ars.vnhapi.grpc.mappers

import br.ars.vnhapi.domain.ProfileUserCountryEntity
import profile_user_country.CreateProfileUserCountryRequest
import profile_user_country.ProfileUserCountry
import profile_user_country.UpdateProfileUserCountryRequest
import java.util.UUID

private fun String?.toUuidOrNull(): UUID? =
    if (this.isNullOrBlank()) null else UUID.fromString(this)

// ===== Entity -> Proto =====
fun ProfileUserCountryEntity.toProto(): ProfileUserCountry =
    ProfileUserCountry.newBuilder()
        .setId(requireNotNull(id).toString())
        .setIdUsuario(idUsuario.toString())
        .setIdPais(idPais.toString())
        .setNomeUsuario(nomeUsuario)
        .setSiglaPais(siglaPais)
        .build()

// ===== Create request -> Entity =====
fun CreateProfileUserCountryRequest.toEntity(): ProfileUserCountryEntity =
    ProfileUserCountryEntity(
        id = null,
        idUsuario = requireNotNull(idUsuario.toUuidOrNull()) { "id_usuario inválido" },
        idPais = requireNotNull(idPais.toUuidOrNull()) { "id_pais inválido" },
        nomeUsuario = nomeUsuario,
        siglaPais = siglaPais
    )

// ===== Update parcial -> merge =====
fun UpdateProfileUserCountryRequest.mergeInto(
    target: ProfileUserCountryEntity
): ProfileUserCountryEntity = target.apply {
    val req = this@mergeInto
    if (!req.idUsuario.isNullOrBlank()) this.idUsuario = UUID.fromString(req.idUsuario)
    if (!req.idPais.isNullOrBlank()) this.idPais = UUID.fromString(req.idPais)
    if (!req.nomeUsuario.isNullOrBlank()) this.nomeUsuario = req.nomeUsuario
    if (!req.siglaPais.isNullOrBlank()) this.siglaPais = req.siglaPais
}
