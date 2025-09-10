// src/main/kotlin/br/ars/vnhapi/shared/cdn/BunnyCdnStorage.kt
package br.ars.vnhapi.shared.clients

import br.ars.vnhapi.shared.constants.CdnAccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.*
import java.util.regex.Pattern

object BunnyCdnStorage {
    private val client: HttpClient = HttpClient.newHttpClient()
    private val dataUrlRe = Pattern.compile("^data:([\\w\\-]+/[\\w\\-+.]+);base64,(.+)$", Pattern.CASE_INSENSITIVE)

    private fun storageUrl(path: String): String =
        "${CdnAccess.URI.trimEnd('/')}/${CdnAccess.ZONE_NAME}/${path.trimStart('/')}"

    private fun extFromMime(mime: String): String = when (mime.lowercase(Locale.ROOT)) {
        "image/jpeg", "image/jpg" -> "jpg"
        "image/png" -> "png"
        "image/webp" -> "webp"
        "image/gif" -> "gif"
        else -> "bin"
    }

    private fun isDataUrl(s: String): Boolean = dataUrlRe.matcher(s).find()

    data class DataUrl(val mime: String, val bytes: ByteArray)
    private fun parseDataUrl(dataUrl: String): DataUrl {
        val m = dataUrlRe.matcher(dataUrl)
        require(m.find()) { "invalid data url" }
        val mime = m.group(1)
        val b64 = m.group(2)
        val bytes = Base64.getDecoder().decode(b64)
        return DataUrl(mime, bytes)
    }

    suspend fun uploadUserAvatar(userId: UUID, dataUrl: String): String = withContext(Dispatchers.IO) {
        require(isDataUrl(dataUrl)) { "avatar must be data url base64" }
        val parsed = parseDataUrl(dataUrl)
        val ext = extFromMime(parsed.mime)
        val path = "${CdnAccess.PREFIX_USERS}/$userId/avatar.$ext"
        val url = storageUrl(path)

        val req = HttpRequest.newBuilder(URI.create(url))
            .header("AccessKey", CdnAccess.ACCESS_KEY)
            .PUT(HttpRequest.BodyPublishers.ofByteArray(parsed.bytes))
            .build()

        val resp = client.send(req, HttpResponse.BodyHandlers.discarding())
        require(resp.statusCode() in 200..299) { "cdn upload failed: ${resp.statusCode()}" }
        path // retornamos o path. Use seu Pull Zone para servir publicamente.
    }

    suspend fun deleteObject(pathOrUrl: String) = withContext(Dispatchers.IO) {
        val path =
            if (pathOrUrl.startsWith("http", ignoreCase = true)) {
                // aceita URL completa e converte para path após o storage zone
                val idx = pathOrUrl.indexOf(CdnAccess.ZONE_NAME)
                if (idx >= 0) pathOrUrl.substring(idx + CdnAccess.ZONE_NAME.length).trimStart('/')
                else pathOrUrl // fallback
            } else pathOrUrl.trimStart('/')

        if (path.isBlank()) return@withContext

        val url = storageUrl(path)
        val req = HttpRequest.newBuilder(URI.create(url))
            .header("AccessKey", CdnAccess.ACCESS_KEY)
            .DELETE()
            .build()

        val resp = client.send(req, HttpResponse.BodyHandlers.discarding())
        // Bunny retorna 404 para inexistente. Consideramos idempotente.
        if (resp.statusCode() !in 200..299 && resp.statusCode() != 404) {
            throw IllegalStateException("cdn delete failed: ${resp.statusCode()}")
        }
    }

    fun looksLikeBase64Data(s: String?): Boolean = s?.let { isDataUrl(it) } == true
}
