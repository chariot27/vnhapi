// src/main/kotlin/br/ars/vnhapi/http/filters/ApiLogFilter.kt
package br.ars.vnhapi.grpc.http.filters

import br.ars.vnhapi.domain.ApiLogEntity
import br.ars.vnhapi.grpc.repositories.ApiLogRepository
import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.util.ContentCachingRequestWrapper
import org.springframework.web.util.ContentCachingResponseWrapper
import java.nio.charset.StandardCharsets

@Component
class ApiLogFilter(
    private val repo: ApiLogRepository
) : Filter {

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val req = ContentCachingRequestWrapper(request as HttpServletRequest)
        val res = ContentCachingResponseWrapper(response as HttpServletResponse)

        val path = req.requestURI
        val method = req.method

        try {
            chain.doFilter(req, res)
        } catch (ex: Exception) {
            // erro 500
            salvarLog(
                mensagem = "EX:${ex::class.simpleName} msg=${ex.message}",
                localizacao = "${controllerFrom(req)}#${methodFrom(req)} $method $path",
                status = "erro"
            )
            throw ex
        } finally {
            val body = req.contentAsByteArray.takeIf { it.isNotEmpty() }
                ?.toString(StandardCharsets.UTF_8).orEmpty()
            val query = req.queryString?.let { "?$it" }.orEmpty()
            val statusCode = res.status

            val status = when {
                statusCode >= 500 -> "erro"
                suspeita(path, body, req.queryString) || statusCode in 400..499 -> "requisição suspeita"
                else -> "correto"
            }

            val msg = "HTTP $method $path$query status=$statusCode ip=${req.remoteAddr} body=${body.take(2000)}"
            salvarLog(
                mensagem = msg,
                localizacao = "${controllerFrom(req)}#${methodFrom(req)}",
                status = status
            )
            res.copyBodyToResponse()
        }
    }

    private fun salvarLog(mensagem: String, localizacao: String, status: String) {
        try { repo.save(ApiLogEntity(mensagem = mensagem, localizacao = localizacao, status = status)) }
        catch (_: Exception) { /* não quebra a requisição */ }
    }

    // Heurística simples de suspeita
    private fun suspeita(path: String, body: String, query: String?): Boolean {
        val p = (path + "|" + (query ?: "") + "|" + body).lowercase()
        val needles = listOf("' or 1=1", " union ", "select ", "<script", " drop ", "/*", "--", " xp_")
        return needles.any { p.contains(it) }
    }

    // Melhor esforço: tenta extrair controller/método de atributos do Spring MVC
    private fun controllerFrom(req: HttpServletRequest): String =
        req.getAttribute("org.springframework.web.servlet.HandlerMapping.bestMatchingHandler")?.let {
            val c = it::class.java
            c.declaringClass?.simpleName ?: c.simpleName ?: "UnknownController"
        } ?: "UnknownController"

    private fun methodFrom(req: HttpServletRequest): String =
        req.getAttribute("org.springframework.web.servlet.HandlerMapping.bestMatchingPattern")?.toString() ?: "unknown"
}
