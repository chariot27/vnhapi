// src/main/kotlin/br/ars/vnhapi/grpc/mappers/UserMappers.kt
package br.ars.vnhapi.grpc.mappers

import br.ars.vnhapi.domain.UserEntity
import br.ars.vnhapi.proto.User

fun UserEntity.toProto(): User =
    User.newBuilder()
        .setId(requireNotNull(id).toString())
        .setEmail(email)
        .apply { username?.let { setUsername(it) } }
        .apply { avatar?.let   { setAvatar(it) } }
        .apply { phone?.let    { setPhone(it) } }
        .apply { bio?.let      { setBio(it) } }
        .addAllTags(tags)
        .setAtivo(ativo)
        .build()
