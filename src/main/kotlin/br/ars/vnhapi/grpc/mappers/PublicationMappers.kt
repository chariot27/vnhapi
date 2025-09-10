// src/main/kotlin/br/ars/vnhapi/grpc/mappers/PublicationMappers.kt
package br.ars.vnhapi.grpc.mappers

import br.ars.vnhapi.domain.PublicationEntity
import br.ars.vnhapi.proto.Publication

fun PublicationEntity.toProto(): Publication =
    Publication.newBuilder()
        .setId(this.id.toString())
        .setIdUser(this.idUser.toString())
        .setPubType(this.pubType)
        .setUserTags(this.userTags ?: "")
        .setPostImgCdnPath(this.postImgCdnPath ?: "")
        .setPostVidCdnPath(this.postVidCdnPath ?: "")
        .setPostTxt(this.postTxt ?: "")
        .setCreatedAt(this.createdAt?.epochSecond ?: 0)
        .setUpdatedAt(this.updatedAt?.epochSecond ?: 0)
        .build()
