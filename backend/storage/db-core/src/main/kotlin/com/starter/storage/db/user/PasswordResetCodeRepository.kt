package com.starter.storage.db.user

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PasswordResetCodeRepository :
    JpaRepository<PasswordResetCodeEntity, Long>,
    PasswordResetCodeRepositoryCustom
