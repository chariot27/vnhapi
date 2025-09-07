package br.ars.vnhapi.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.UuidGenerator
import java.util.UUID

@Entity
@Table(
    name = "countries",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["sigla"])
    ]
)
class CountryEntity(
    @Id
    @GeneratedValue
    @UuidGenerator
    var id: UUID? = null,

    @Column(nullable = false, columnDefinition = "text")
    var nome: String,

    @Column(nullable = false, length = 10)
    var sigla: String
)