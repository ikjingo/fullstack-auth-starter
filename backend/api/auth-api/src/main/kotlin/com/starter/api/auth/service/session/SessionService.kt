package com.starter.api.auth.service.session

import com.starter.api.auth.config.SessionProperties
import com.starter.core.api.support.error.CoreApiException
import com.starter.core.api.support.error.ErrorType
import com.starter.storage.db.user.RefreshTokenRepository
import com.starter.storage.db.user.UserEntity
import com.starter.storage.db.user.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 세션 관리 서비스
 *
 * - 동시 세션 제한 (사용자당 최대 N개)
 * - 세션 목록 조회
 * - 세션 강제 종료
 */
@Service
@Transactional(readOnly = true)
class SessionService(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val userRepository: UserRepository,
    private val sessionProperties: SessionProperties,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * 새 세션 생성 전 세션 수 제한 확인
     * 최대 세션 수를 초과하면 가장 오래된 세션을 만료시킴
     */
    @Transactional
    fun enforceSessionLimit(user: UserEntity) {
        val activeSessionCount = refreshTokenRepository.countActiveSessionsByUser(user)

        if (activeSessionCount >= sessionProperties.maxSessionsPerUser) {
            val sessionsToRevoke = (activeSessionCount - sessionProperties.maxSessionsPerUser + 1).toInt()

            repeat(sessionsToRevoke) {
                val oldestSession = refreshTokenRepository.findOldestActiveSessionByUser(user)
                oldestSession?.let {
                    it.revoke()
                    log.info("Session limit exceeded. Revoked oldest session for user: ${user.email}")
                }
            }
        }
    }

    /**
     * 사용자의 활성 세션 목록 조회
     */
    fun getActiveSessions(userId: Long): List<SessionResponse> {
        val user = findUserByIdOrThrow(userId)
        val sessions = refreshTokenRepository.findActiveSessionsByUser(user)

        return sessions.map { session ->
            SessionResponse(
                id = session.id,
                createdAt = session.createdAt.toString(),
                expiresAt = session.expiresAt.toString(),
                userAgent = session.userAgent,
                ipAddress = session.ipAddress,
                deviceInfo = session.deviceInfo ?: parseDeviceInfo(session.userAgent),
            )
        }
    }

    /**
     * 특정 세션 강제 종료
     */
    @Transactional
    fun revokeSession(
        userId: Long,
        sessionId: Long,
    ): Boolean {
        val user = findUserByIdOrThrow(userId)
        val session =
            refreshTokenRepository.findByIdAndUser(sessionId, user)
                ?: throw CoreApiException(ErrorType.NOT_FOUND)

        if (session.revoked) {
            throw CoreApiException(ErrorType.NOT_FOUND)
        }

        session.revoke()
        log.info("Session revoked - userId: {}, sessionId: {}", userId, sessionId)

        return true
    }

    /**
     * 현재 세션을 제외한 모든 세션 종료
     */
    @Transactional
    fun revokeAllOtherSessions(
        userId: Long,
        currentToken: String,
    ): Int {
        val user = findUserByIdOrThrow(userId)
        val sessions = refreshTokenRepository.findActiveSessionsByUser(user)

        var revokedCount = 0
        sessions.forEach { session ->
            if (session.token != currentToken && !session.revoked) {
                session.revoke()
                revokedCount++
            }
        }

        log.info("Revoked {} other sessions for user: {}", revokedCount, user.email)
        return revokedCount
    }

    /**
     * User-Agent에서 디바이스 정보 파싱
     */
    private fun parseDeviceInfo(userAgent: String?): String {
        if (userAgent.isNullOrBlank()) {
            return "Unknown Device"
        }

        return when {
            userAgent.contains("iPhone") -> "iPhone"
            userAgent.contains("iPad") -> "iPad"
            userAgent.contains("Android") -> "Android Device"
            userAgent.contains("Windows") -> "Windows PC"
            userAgent.contains("Mac OS") || userAgent.contains("Macintosh") -> "Mac"
            userAgent.contains("Linux") -> "Linux PC"
            else -> "Unknown Device"
        }
    }

    private fun findUserByIdOrThrow(userId: Long): UserEntity =
        userRepository.findById(userId).orElseThrow {
            CoreApiException(ErrorType.USER_NOT_FOUND)
        }
}

/**
 * 세션 응답 DTO
 */
data class SessionResponse(
    val id: Long,
    val createdAt: String,
    val expiresAt: String,
    val userAgent: String?,
    val ipAddress: String?,
    val deviceInfo: String?,
)
