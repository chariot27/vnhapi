// src/main/kotlin/br/ars/vnhapi/http/FilterController.kt
package br.ars.vnhapi.web.controllers

import br.ars.vnhapi.grpc.services.FilterServiceImpl
import br.ars.vnhapi.proto.FilterByTagsRequest
import br.ars.vnhapi.proto.FilterByTagsResponse
import jakarta.validation.constraints.NotEmpty
import kotlinx.coroutines.runBlocking
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/filter")
@Validated
class FilterController(
    private val service: FilterServiceImpl
) {

    @GetMapping
    fun filter(
        @RequestParam("tags") @NotEmpty tags: List<String>,
        @RequestParam(name = "matchAll", defaultValue = "false") matchAll: Boolean,
        @RequestParam(name = "limit", defaultValue = "20") limit: Int,
        @RequestParam(name = "cursor", required = false) cursor: String?
    ): ResponseEntity<FilterByTagsResponse> = runBlocking {
        val req = FilterByTagsRequest.newBuilder()
            .addAllTags(tags)
            .setMatchAll(matchAll)
            .setLimit(limit)
            .setCursor(cursor ?: "")
            .build()

        ResponseEntity.ok(service.filterByTags(req))
    }
}
