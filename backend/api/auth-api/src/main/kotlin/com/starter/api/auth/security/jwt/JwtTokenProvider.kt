package com.starter.api.auth.security.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.UnsupportedJwtException
import io.jsonwebtoken.security.Keys
import io.jsonwebtoken.security.SecurityException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    private val jwtProperties: JwtProperties,
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val secretKey: SecretKey = Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray())

    fun createAccessToken(
        userId: Long,
        email: String,
        roles: List<String>,
    ): String = createToken(userId, email, roles, jwtProperties.accessTokenExpiration, TokenType.ACCESS)

    fun createRefreshToken(
        userId: Long,
        email: String,
    ): String = createToken(userId, email, emptyList(), jwtProperties.refreshTokenExpiration, TokenType.REFRESH)

    private fun createToken(
        userId: Long,
        email: String,
        roles: List<String>,
        expiration: Long,
        tokenType: TokenType,
    ): String {
        val now = Date()
        val expiryDate = Date(now.time + expiration)

        return Jwts
            .builder()
            .subject(userId.toString())
            .claim("email", email)
            .claim("roles", roles)
            .claim("type", tokenType.name)
            .issuer(jwtProperties.issuer)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(secretKey)
            .compact()
    }

    fun validateToken(token: String): Boolean {
        try {
            getClaims(token)
            return true
        } catch (e: SecurityException) {
            log.warn("Invalid JWT signature: {}", e.message)
        } catch (e: MalformedJwtException) {
            log.warn("Invalid JWT token: {}", e.message)
        } catch (e: ExpiredJwtException) {
            log.warn("Expired JWT token: {}", e.message)
        } catch (e: UnsupportedJwtException) {
            log.warn("Unsupported JWT token: {}", e.message)
        } catch (e: IllegalArgumentException) {
            log.warn("JWT claims string is empty: {}", e.message)
        }
        return false
    }

    fun getUserId(token: String): Long = getClaims(token).subject.toLong()

    fun getEmail(token: String): String = getClaims(token)["email"] as String

    @Suppress("UNCHECKED_CAST")
    fun getRoles(token: String): List<String> = getClaims(token)["roles"] as? List<String> ?: emptyList()

    fun getTokenType(token: String): TokenType {
        val type = getClaims(token)["type"] as String
        return TokenType.valueOf(type)
    }

    fun isAccessToken(token: String): Boolean = getTokenType(token) == TokenType.ACCESS

    fun isRefreshToken(token: String): Boolean = getTokenType(token) == TokenType.REFRESH

    /**
     * 토큰의 만료 시간을 LocalDateTime으로 반환
     */
    fun getExpiration(token: String): LocalDateTime {
        val expiration = getClaims(token).expiration
        return expiration.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
    }

    private fun getClaims(token: String): Claims =
        Jwts
            .parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload

    enum class TokenType {
        ACCESS,
        REFRESH,
    }
}
