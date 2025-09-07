package br.ars.vnhapi.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.UuidGenerator
import java.util.UUID

@Entity
@Table(
    name = "profile_user_country",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["id_usuario", "id_pais"])
    ],
    indexes = [
        Index(name = "ix_puc_usuario", columnList = "id_usuario"),
        Index(name = "ix_puc_pais", columnList = "id_pais")
    ]
)
class ProfileUserCountryEntity(

    @Id
    @GeneratedValue
    @UuidGenerator
    var id: UUID? = null,

    @Column(name = "id_usuario", nullable = false)
    var idUsuario: UUID,

    @Column(name = "id_pais", nullable = false)
    var idPais: UUID,

    @Column(name = "nome_usuario", nullable = false, length = 255)
    var nomeUsuario: String,

    @Column(name = "sigla_pais", nullable = false, length = 2)
    var siglaPais: String
) {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", referencedColumnName = "id", insertable = false, updatable = false)
    var user: UserEntity? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pais", referencedColumnName = "id", insertable = false, updatable = false)
    var country: CountryEntity? = null
}
