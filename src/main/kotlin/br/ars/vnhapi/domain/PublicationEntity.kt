// src/main/kotlin/br/ars/vnhapi/domain/PublicationEntity.kt
package br.ars.vnhapi.domain

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.hibernate.annotations.UuidGenerator
import java.time.Instant
import java.util.*

@Entity
@Table(name = "publications")
data class PublicationEntity(
    @Id
    @GeneratedValue
    @UuidGenerator
    var id: UUID? = null,

    @Column(nullable = false)
    var idUser: UUID,

    @Column(nullable = false)
    var pubType: Int,

    @Column(columnDefinition = "text")
    var userTags: String? = null,

    @Column(columnDefinition = "text")
    var postImgCdnPath: String? = null,

    @Column(columnDefinition = "text")
    var postVidCdnPath: String? = null,

    @Column(columnDefinition = "text")
    var postTxt: String? = null,

    @CreationTimestamp
    @Column(updatable = false)
    var createdAt: Instant? = null,

    @UpdateTimestamp
    var updatedAt: Instant? = null
)
