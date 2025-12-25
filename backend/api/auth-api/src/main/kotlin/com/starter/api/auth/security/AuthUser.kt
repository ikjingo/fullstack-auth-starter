package com.starter.api.auth.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class AuthUser(
    val userId: Long,
    val email: String,
    private val roles: List<String>,
) : UserDetails {
    override fun getAuthorities(): Collection<GrantedAuthority> = roles.map { SimpleGrantedAuthority("ROLE_$it") }

    override fun getPassword(): String? = null

    override fun getUsername(): String = email

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = true

    /**
     * 특정 역할을 가지고 있는지 확인
     */
    fun hasRole(role: String): Boolean {
        val roleWithPrefix = if (role.startsWith("ROLE_")) role else "ROLE_$role"
        return authorities.any { it.authority == roleWithPrefix }
    }

    /**
     * 관리자 권한을 가지고 있는지 확인
     */
    fun isAdmin(): Boolean = hasRole("ADMIN")
}
