// src/main/kotlin/br/ars/vnhapi/grpc/services/AuthServiceImpl.kt
package br.ars.vnhapi.grpc.services

import br.ars.vnhapi.domain.AuthEntity
import br.ars.vnhapi.grpc.mappers.AuthMappers
import br.ars.vnhapi.grpc.mappers.TokenPairDTO
import br.ars.vnhapi.grpc.repositories.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest
import java.time.*
import java.util.*
import kotlin.random.Random

// Abstrações simples para usuários e JWT.
// Substitua por seus repos/claims reais.
interface UserLookup {
    fun findIdByUsername(username: String): UUID?
    fun passwordMatches(userId: UUID, raw: String): Boolean
}

interface TokenProvider {
    fun mintAccessToken(userId: UUID, clientId: String, deviceId: String, scope: String?, aud: String?, ttlSeconds: Int): String
}

data class LoginCmd(
    val username: String,
    val password: String,
    val clientId: String,
    val deviceId: String,
    val scope: String? = null,
    val aud: String? = "gateway"
)

data class RefreshCmd(
    val refreshToken: String,
    val clientId: String,
    val deviceId: String
)

@Service
class AuthServiceImpl(
    private val authRepo: AuthRepository,
    private val userLookup: UserLookup,
    private val tokenProvider: TokenProvider,
    private val passwordEncoder: PasswordEncoder, // se precisar validar direto
    private val clock: Clock = Clock.systemUTC()
) {

    private val ACCESS_TTL = 600 // 10min

    @Transactional
    suspend fun login(cmd: LoginCmd): TokenPairDTO = withContext(Dispatchers.IO) {
        val userId = userLookup.findIdByUsername(cmd.username)
            ?: throw EmptyResultDataAccessException("user not found", 1)
        if (!userLookup.passwordMatches(userId, cmd.password)) {
            throw SecurityException("invalid credentials")
        }

        val now = Instant.now(clock)
        val refreshOpaque = newOpaque()
        val refreshHash = hash(refreshOpaque)
        val refreshExp = endOfDayUTC(now, ZoneOffset.UTC)

        // encerra sessões antigas do mesmo device opcionalmente
        authRepo.findActiveSession(userId, cmd.clientId, cmd.deviceId, now).forEach {
            it.revoke(now)
            authRepo.save(it)
        }

        val session = AuthEntity(
            userId = userId,
            clientId = cmd.clientId,
            deviceId = cmd.deviceId,
            refreshHash = refreshHash,
            refreshExpiresAt = refreshExp,
            scope = cmd.scope,
            aud = cmd.aud,
            accessTtlSeconds = ACCESS_TTL
        )
        authRepo.save(session)

        val accessJwt = tokenProvider.mintAccessToken(
            userId = userId,
            clientId = cmd.clientId,
            deviceId = cmd.deviceId,
            scope = cmd.scope,
            aud = cmd.aud,
            ttlSeconds = ACCESS_TTL
        )

        AuthMappers.toTokenPairDTO(
            accessJwt, ACCESS_TTL,
            refreshOpaque, refreshExp
        )
    }

    @Transactional
    suspend fun refresh(cmd: RefreshCmd): TokenPairDTO = withContext(Dispatchers.IO) {
        val now = Instant.now(clock)
        val hash = hash(cmd.refreshToken)
        val current = authRepo.findByRefreshHash(hash).orElseThrow { SecurityException("invalid_grant") }

        // valida amarrações
        if (current.clientId != cmd.clientId || current.deviceId != cmd.deviceId) {
            throw SecurityException("invalid_grant")
        }
        if (!current.isActive) {
            throw SecurityException("invalid_grant")
        }

        // rotação: marca uso e cria novo
        val newOpaque = newOpaque()
        val newHash = hash(newOpaque)
        val newExp = endOfDayUTC(now, ZoneOffset.UTC)

        current.markUsed(now)
        authRepo.save(current)

        val rotated = current.rotateTo(newHash, newExp, now, current.ip, current.userAgent)
        authRepo.save(rotated)

        val accessJwt = tokenProvider.mintAccessToken(
            userId = rotated.userId,
            clientId = rotated.clientId,
            deviceId = rotated.deviceId,
            scope = rotated.scope,
            aud = rotated.aud,
            ttlSeconds = rotated.accessTtlSeconds
        )

        AuthMappers.toTokenPairDTO(accessJwt, rotated.accessTtlSeconds, newOpaque, newExp)
    }

    @Transactional
    suspend fun revoke(refreshToken: String, clientId: String, deviceId: String) = withContext(Dispatchers.IO) {
        val hash = hash(refreshToken)
        val e = authRepo.findByRefreshHash(hash).orElseThrow { SecurityException("invalid_grant") }
        if (e.clientId != clientId || e.deviceId != deviceId) throw SecurityException("invalid_grant")
        e.revoke(Instant.now(clock))
        authRepo.save(e)
        true
    }

    suspend fun introspect(accessToken: String): Map<String, Any> = withContext(Dispatchers.IO) {
        // delegue ao provider real. Aqui retornamos shape mínimo.
        mapOf("active" to true) // substitua pela validação real do JWT
    }

    // ---------- helpers ----------
    private fun newOpaque(): String {
        val bytes = Random.Default.nextBytes(32)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    private fun hash(token: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(token.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(digest)
    }

    private fun endOfDayUTC(now: Instant, zone: ZoneId): Instant {
        val zdt = now.atZone(zone)
        val end = zdt.toLocalDate().atTime(23, 59, 59, 999_000_000)
        return end.atZone(zone).toInstant()
    }
}
