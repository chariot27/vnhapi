// src/main/kotlin/br/ars/vnhapi/domain/ApiLogEntity.kt
package br.ars.vnhapi.domain

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UuidGenerator
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "api_logs", indexes = [Index(name = "ix_api_logs_status", columnList = "status")])
class ApiLogEntity(
    @Id @GeneratedValue @UuidGenerator
    var id: UUID? = null,

    @Column(columnDefinition = "text", nullable = false)
    var mensagem: String,

    @Column(columnDefinition = "text", nullable = false)
    var localizacao: String, // controller#metodo

    @Column(length = 32, nullable = false)
    var status: String, // "erro" | "correto" | "requisição suspeita"

    @CreationTimestamp
    var createdAt: OffsetDateTime? = null
)
