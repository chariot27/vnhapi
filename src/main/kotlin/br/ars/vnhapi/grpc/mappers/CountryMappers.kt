// src/main/kotlin/br/ars/vnhapi/grpc/mappers/CountryMappers.kt
package br.ars.vnhapi.grpc.mappers

import br.ars.vnhapi.domain.CountryEntity
import country.CountryOuterClass
import java.util.*

// Entity -> Proto
fun CountryEntity.toProto(): CountryOuterClass.Country =
    CountryOuterClass.Country.newBuilder()
        .setId(requireNotNull(id).toString())
        .setNome(nome)
        .setSigla(sigla)
        .build()

// Proto -> Entity (útil em creates)
fun CountryOuterClass.Country.toEntity(): CountryEntity =
    CountryEntity(
        id = if (id.isNullOrBlank()) null else UUID.fromString(id),
        nome = nome,
        sigla = sigla
    )

// Merge Proto -> Entity (útil em updates parciais)
fun CountryOuterClass.Country.mergeInto(target: CountryEntity): CountryEntity = target.apply {
    if (nome.isNotBlank()) this.nome = nome
    if (sigla.isNotBlank()) this.sigla = sigla
}
