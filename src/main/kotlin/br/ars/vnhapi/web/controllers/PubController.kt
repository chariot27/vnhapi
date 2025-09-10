// src/main/kotlin/br/ars/vnhapi/http/PubController.kt
package br.ars.vnhapi.web.controllers

import br.ars.vnhapi.grpc.services.PubServiceImpl
import br.ars.vnhapi.proto.*
import br.ars.vnhapi.shared.constants.ApiUrls
import com.google.protobuf.Empty
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import kotlinx.coroutines.runBlocking
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.security.Principal

@RestController
@RequestMapping(ApiUrls.PUBS_ROUTE)
@Validated
class PubController(
    private val service: PubServiceImpl
) {

    data class CreatePubRequest(
        @field:NotBlank val pubType: Int,
        val userTags: String? = null,
        val postImgCdnPath: String? = null,
        val postVidCdnPath: String? = null,
        val postTxt: String? = null
    )

    data class UpdatePubRequest(
        @field:NotBlank val id: String,
        val pubType: Int? = null,
        val userTags: String? = null,
        val postImgCdnPath: String? = null,
        val postVidCdnPath: String? = null,
        val postTxt: String? = null
    )

    @PostMapping
    fun create(principal: Principal, @Valid @RequestBody body: CreatePubRequest): ResponseEntity<Publication> = runBlocking {
        // idUser deve vir do JWT (extraído do principal)
        val userId = principal.name
        val req = CreatePublicationRequest.newBuilder()
            .setIdUser(userId)
            .setPubType(body.pubType)
            .setUserTags(body.userTags ?: "")
            .setPostImgCdnPath(body.postImgCdnPath ?: "")
            .setPostVidCdnPath(body.postVidCdnPath ?: "")
            .setPostTxt(body.postTxt ?: "")
            .build()

        ResponseEntity.ok(service.create(req))
    }

    @GetMapping("/{id}")
    fun get(@PathVariable id: String): ResponseEntity<Publication> = runBlocking {
        ResponseEntity.ok(service.get(GetPublicationRequest.newBuilder().setId(id).build()))
    }

    @PutMapping
    fun update(@Valid @RequestBody body: UpdatePubRequest): ResponseEntity<Publication> = runBlocking {
        val builder = UpdatePublicationRequest.newBuilder().setId(body.id)
        body.pubType?.let { builder.pubType = it }
        body.userTags?.let { builder.userTags = it }
        body.postImgCdnPath?.let { builder.postImgCdnPath = it }
        body.postVidCdnPath?.let { builder.postVidCdnPath = it }
        body.postTxt?.let { builder.postTxt = it }
        ResponseEntity.ok(service.update(builder.build()))
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: String): ResponseEntity<Empty> = runBlocking {
        ResponseEntity.ok(service.delete(DeletePublicationRequest.newBuilder().setId(id).build()))
    }

    @GetMapping
    fun list(
        @RequestParam(required = false) userId: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ListPublicationsResponse> = runBlocking {
        val req = ListPublicationsRequest.newBuilder()
            .setIdUser(userId ?: "")
            .setPage(page)
            .setSize(size)
            .build()
        ResponseEntity.ok(service.list(req))
    }
}
