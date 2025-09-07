// src/main/kotlin/br/ars/vnhapi/http/ApiLogController.kt
package br.ars.vnhapi.web.controllers

import api_log.ApiLog
import api_log.CreateApiLogRequest
import br.ars.vnhapi.domain.ApiLogEntity
import br.ars.vnhapi.grpc.mappers.toEntity
import br.ars.vnhapi.grpc.repositories.ApiLogRepository
import br.ars.vnhapi.shared.constants.ApiUrls
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(ApiUrls.LOGGER_ROUTE)
class ApiLogController(private val repo: ApiLogRepository) {

    @PostMapping
    fun push(@RequestBody body: CreateApiLogRequest): ResponseEntity<Void> {
        repo.save(body.toEntity())
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    @GetMapping
    fun list(): ResponseEntity<List<ApiLogEntity?>> {
        val items = repo.findAll()
        return ResponseEntity.ok(items)
    }
}
