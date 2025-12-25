package com.starter.api.auth.service.auth

import com.starter.api.auth.controller.request.RefreshTokenRequest
import com.starter.api.auth.controller.request.SignInRequest
import com.starter.api.auth.controller.request.SignUpRequest
import com.starter.api.auth.controller.response.AuthResponse
import com.starter.api.auth.controller.response.TokenResponse
import com.starter.api.auth.controller.response.UserResponse
import com.starter.api.auth.event.AuthEventPublisher
import com.starter.api.auth.security.jwt.JwtProperties
import com.starter.api.auth.security.jwt.JwtTokenProvider
import com.starter.api.auth.service.audit.AuditLogService
import com.starter.api.auth.service.metrics.AuthMetricsService
import com.starter.api.auth.service.session.SessionService
import com.starter.api.auth.service.token.TokenBlacklistService
import com.starter.core.api.support.error.CoreApiException
import com.starter.core.api.support.error.ErrorType
import com.starter.storage.db.user.RefreshTokenEntity
import com.starter.storage.db.user.RefreshTokenRepository
import com.starter.storage.db.user.UserEntity
import com.starter.storage.db.user.UserRepository
import com.starter.storage.db.user.UserStatus
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * 핵심 인증 서비스
 * - 회원가입/로그인
 * - 토큰 갱신/로그아웃
 * - 사용자 정보 조회
 */
@Service
@Transactional(readOnly = true)
class AuthenticationService(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    private val jwtProperties: JwtProperties,
    private val passwordEncoder: PasswordEncoder,
    private val tokenBlacklistService: TokenBlacklistService,
    private val loginAttemptService: LoginAttemptService,
    private val auditLogService: AuditLogService,
    private val authMetricsService: AuthMetricsService,
    private val sessionService: SessionService,
    private val eventPublisher: AuthEventPublisher,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun signUp(request: SignUpRequest): AuthResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw CoreApiException(ErrorType.DUPLICATE_EMAIL)
        }

        val user =
            UserEntity(
                email = request.email,
                password = passwordEncoder.encode(request.password),
                nickname = request.nickname,
            )

        val savedUser = userRepository.save(user)
        val tokenResponse = generateTokens(savedUser)

        log.info("New user registered: ${savedUser.email}")
        auditLogService.logSignUp(savedUser.id, savedUser.email)
        authMetricsService.recordSignUp()
        eventPublisher.publishUserRegistered(savedUser.id, savedUser.email, savedUser.nickname)

        return AuthResponse.from(
            user = savedUser,
            token = tokenResponse.accessToken,
            refreshToken = tokenResponse.refreshToken,
        )
    }

    @Transactional
    fun signIn(request: SignInRequest): AuthResponse {
        val user =
            userRepository.findByEmailAndStatus(request.email, UserStatus.ACTIVE)
                ?: throw CoreApiException(ErrorType.INVALID_CREDENTIALS)

        // 계정 잠금 여부 확인
        loginAttemptService.checkAccountLocked(user)

        // 비밀번호 미설정 계정은 로그인 불가
        if (user.password == null) {
            throw CoreApiException(ErrorType.INVALID_CREDENTIALS)
        }

        if (!passwordEncoder.matches(request.password, user.password)) {
            // 비밀번호 불일치 시 실패 기록
            loginAttemptService.recordFailedAttempt(user)
            auditLogService.logLoginFailure(request.email, "invalid_password")
            authMetricsService.recordLoginFailure("invalid_password")
            eventPublisher.publishLoginFailure(request.email, "invalid_password")
            throw CoreApiException(ErrorType.INVALID_CREDENTIALS)
        }

        // 로그인 성공 시 실패 카운터 초기화
        loginAttemptService.resetFailedAttempts(user)
        auditLogService.logLoginSuccess(user.id, user.email)
        authMetricsService.recordLoginSuccess()
        eventPublisher.publishLoginSuccess(user.id, user.email)

        val tokenResponse = generateTokens(user)
        return AuthResponse.from(
            user = user,
            token = tokenResponse.accessToken,
            refreshToken = tokenResponse.refreshToken,
        )
    }

    @Transactional
    fun refresh(request: RefreshTokenRequest): TokenResponse {
        val token = request.refreshToken

        if (!jwtTokenProvider.validateToken(token)) {
            throw CoreApiException(ErrorType.INVALID_TOKEN)
        }

        if (!jwtTokenProvider.isRefreshToken(token)) {
            throw CoreApiException(ErrorType.INVALID_TOKEN)
        }

        val refreshTokenEntity =
            refreshTokenRepository.findByToken(token)
                ?: throw CoreApiException(ErrorType.INVALID_TOKEN)

        if (!refreshTokenEntity.isValid()) {
            throw CoreApiException(ErrorType.INVALID_TOKEN)
        }

        val user = refreshTokenEntity.user

        // 기존 토큰 삭제 (새 토큰과 unique 제약 충돌 방지)
        refreshTokenRepository.delete(refreshTokenEntity)
        refreshTokenRepository.flush()

        auditLogService.logTokenRefresh(user.id)
        authMetricsService.recordTokenRefresh()
        eventPublisher.publishTokenRefreshed(user.id, user.email)
        return generateTokens(user)
    }

    @Transactional
    fun signOut(
        userId: Long,
        accessToken: String?,
    ) {
        val user = findUserByIdOrThrow(userId)
        refreshTokenRepository.revokeAllByUser(user)

        // Access Token을 블랙리스트에 추가 (즉시 무효화)
        if (accessToken != null && jwtTokenProvider.validateToken(accessToken)) {
            val expiresAt = jwtTokenProvider.getExpiration(accessToken)
            tokenBlacklistService.blacklistToken(accessToken, expiresAt)
            log.info("User signed out and token blacklisted: ${user.email}")
        }

        auditLogService.logLogout(userId)
        authMetricsService.recordLogout()
        eventPublisher.publishLogout(userId, user.email)
    }

    fun getMe(userId: Long): UserResponse {
        val user = findUserByIdOrThrow(userId)
        return UserResponse.from(user)
    }

    @Transactional
    fun updateNickname(
        userId: Long,
        nickname: String,
    ): AuthResponse {
        val user = findUserByIdOrThrow(userId)

        user.nickname = nickname
        log.info("Nickname updated for user: ${user.email}")

        val tokenResponse = generateTokens(user)
        return AuthResponse.from(
            user = user,
            token = tokenResponse.accessToken,
            refreshToken = tokenResponse.refreshToken,
        )
    }

    fun generateTokens(
        user: UserEntity,
        userAgent: String? = null,
        ipAddress: String? = null,
    ): TokenResponse {
        // 세션 제한 확인 및 초과 시 가장 오래된 세션 만료
        sessionService.enforceSessionLimit(user)

        val roles = listOf(user.role.name)

        val accessToken = jwtTokenProvider.createAccessToken(user.id, user.email, roles)
        val refreshToken = jwtTokenProvider.createRefreshToken(user.id, user.email)

        val expiresAt = LocalDateTime.now().plusSeconds(jwtProperties.refreshTokenExpiration / 1000)
        val refreshTokenEntity =
            RefreshTokenEntity(
                user = user,
                token = refreshToken,
                expiresAt = expiresAt,
                userAgent = userAgent,
                ipAddress = ipAddress,
                deviceInfo = parseDeviceInfo(userAgent),
            )
        refreshTokenRepository.save(refreshTokenEntity)

        return TokenResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresIn = jwtProperties.accessTokenExpiration / 1000,
        )
    }

    private fun parseDeviceInfo(userAgent: String?): String? {
        if (userAgent.isNullOrBlank()) return null

        return when {
            userAgent.contains("iPhone") -> "iPhone"
            userAgent.contains("iPad") -> "iPad"
            userAgent.contains("Android") -> "Android"
            userAgent.contains("Windows") -> "Windows"
            userAgent.contains("Mac OS") || userAgent.contains("Macintosh") -> "Mac"
            userAgent.contains("Linux") -> "Linux"
            else -> null
        }
    }

    fun findUserByIdOrThrow(userId: Long): UserEntity =
        userRepository.findById(userId).orElseThrow {
            CoreApiException(ErrorType.USER_NOT_FOUND)
        }
}
