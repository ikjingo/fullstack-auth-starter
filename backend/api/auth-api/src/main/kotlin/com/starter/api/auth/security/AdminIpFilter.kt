package com.starter.api.auth.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.starter.api.auth.config.AdminIpProperties
import com.starter.core.api.support.error.ErrorType
import com.starter.core.api.support.response.ApiResponse
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher
import org.springframework.web.filter.OncePerRequestFilter
import java.net.InetAddress

/**
 * Admin API에 대한 IP 기반 접근 제어 필터
 *
 * 설정된 IP 화이트리스트에 있는 IP만 Admin API에 접근 가능
 * CIDR 표기법 지원 (예: 10.0.0.0/8, 192.168.1.0/24)
 */
@Component
@ConditionalOnProperty(
    prefix = "admin.ip-whitelist",
    name = ["enabled"],
    havingValue = "true",
)
class AdminIpFilter(
    private val adminIpProperties: AdminIpProperties,
    private val objectMapper: ObjectMapper,
) : OncePerRequestFilter() {
    private val log = LoggerFactory.getLogger(javaClass)
    private val pathMatcher = AntPathMatcher()
    private val parsedCidrs: List<CidrRange> by lazy {
        adminIpProperties.allowedIps.mapNotNull { parseCidr(it) }
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val requestPath = request.requestURI
        val isAdminPath =
            adminIpProperties.pathPatterns.any { pattern ->
                pathMatcher.match(pattern, requestPath)
            }

        if (!isAdminPath) {
            filterChain.doFilter(request, response)
            return
        }

        val clientIp = getClientIp(request)

        if (!isIpAllowed(clientIp)) {
            log.warn("Admin API 접근 거부 - IP: {}, Path: {}", clientIp, requestPath)
            sendForbiddenResponse(response, clientIp)
            return
        }

        log.debug("Admin API 접근 허용 - IP: {}, Path: {}", clientIp, requestPath)
        filterChain.doFilter(request, response)
    }

    private fun isIpAllowed(clientIp: String): Boolean =
        try {
            val clientAddress = InetAddress.getByName(clientIp)
            parsedCidrs.any { cidr -> cidr.contains(clientAddress) }
        } catch (e: Exception) {
            log.error("IP 주소 파싱 실패: {}", clientIp, e)
            false
        }

    private fun getClientIp(request: HttpServletRequest): String {
        val xForwardedFor = request.getHeader("X-Forwarded-For")
        if (!xForwardedFor.isNullOrBlank()) {
            return xForwardedFor.split(",").first().trim()
        }

        val xRealIp = request.getHeader("X-Real-IP")
        if (!xRealIp.isNullOrBlank()) {
            return xRealIp
        }

        return request.remoteAddr ?: "unknown"
    }

    private fun sendForbiddenResponse(
        response: HttpServletResponse,
        clientIp: String,
    ) {
        response.status = HttpServletResponse.SC_FORBIDDEN
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = "UTF-8"

        val errorResponse =
            ApiResponse.error<Unit>(
                error = ErrorType.FORBIDDEN,
                message = "IP 주소 $clientIp 에서의 접근이 허용되지 않습니다",
            )

        response.writer.write(objectMapper.writeValueAsString(errorResponse))
    }

    private fun parseCidr(cidrNotation: String): CidrRange? =
        try {
            val parts = cidrNotation.split("/")
            val address = InetAddress.getByName(parts[0])
            val prefixLength =
                if (parts.size > 1) {
                    parts[1].toInt()
                } else {
                    if (address.address.size == 4) 32 else 128
                }
            CidrRange(address, prefixLength)
        } catch (e: Exception) {
            log.warn("CIDR 파싱 실패: {}", cidrNotation, e)
            null
        }

    /**
     * CIDR 범위를 나타내는 클래스
     */
    private class CidrRange(
        private val networkAddress: InetAddress,
        private val prefixLength: Int,
    ) {
        private val networkBytes = networkAddress.address
        private val maskBytes = createMask()

        private fun createMask(): ByteArray {
            val mask = ByteArray(networkBytes.size)
            var remainingBits = prefixLength

            for (i in mask.indices) {
                if (remainingBits >= 8) {
                    mask[i] = 0xFF.toByte()
                    remainingBits -= 8
                } else if (remainingBits > 0) {
                    mask[i] = (0xFF shl (8 - remainingBits)).toByte()
                    remainingBits = 0
                } else {
                    mask[i] = 0
                }
            }
            return mask
        }

        fun contains(address: InetAddress): Boolean {
            val addressBytes = address.address

            // IPv4와 IPv6 타입이 다르면 불일치
            if (addressBytes.size != networkBytes.size) {
                return false
            }

            for (i in networkBytes.indices) {
                val networkPart = (networkBytes[i].toInt() and maskBytes[i].toInt())
                val addressPart = (addressBytes[i].toInt() and maskBytes[i].toInt())
                if (networkPart != addressPart) {
                    return false
                }
            }
            return true
        }
    }
}
