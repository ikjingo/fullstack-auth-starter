package com.starter.storage.db.user

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RefreshTokenRepository :
    JpaRepository<RefreshTokenEntity, Long>,
    RefreshTokenRepositoryCustom
