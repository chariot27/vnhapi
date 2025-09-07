package br.ars.vnhapi.domain

import br.ars.vnhapi.shared.utils.TagsConverter
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.UuidGenerator
import java.util.UUID

@Entity
@Table(
    name = "users",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["email"]),
        UniqueConstraint(columnNames = ["username"])
    ]
)
class UserEntity(
    @Id @GeneratedValue @UuidGenerator
    var id: UUID? = null,

    @Column(columnDefinition = "text")
    var avatar: String? = null,

    @Column(nullable = false, length = 255)
    var email: String,

    // agora pode ser nulo
    @Column(nullable = true, length = 100)
    var username: String? = null,

    @Column(nullable = false, length = 255)
    var password: String,

    @Column(length = 20)
    var phone: String? = null,

    @Column(length = 500)
    var bio: String? = null,

    @Convert(converter = TagsConverter::class)
    @Column(columnDefinition = "text")
    var tags: List<String> = emptyList(),

    @Column(nullable = false)
    var ativo: Boolean = true
)