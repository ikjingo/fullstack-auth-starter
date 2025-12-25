package com.starter.core.api.controller

import com.starter.core.api.support.error.CoreApiException
import com.starter.core.api.support.error.ErrorType
import com.starter.core.api.support.response.ApiResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ConstraintViolationException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.logging.LogLevel
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ApiControllerAdvice {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(CoreApiException::class)
    fun handleCoreApiException(
        e: CoreApiException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiResponse<Any>> {
        when (e.errorType.logLevel) {
            LogLevel.ERROR -> log.error("CoreApiException : {}", e.message, e)
            LogLevel.WARN -> log.warn("CoreApiException : {}", e.message, e)
            else -> log.info("CoreApiException : {}", e.message, e)
        }
        return ResponseEntity(
            ApiResponse.error(e.errorType, e.data, path = request.requestURI),
            e.errorType.status,
        )
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValid(
        e: MethodArgumentNotValidException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiResponse<Any>> {
        val errors =
            e.bindingResult.fieldErrors.associate { fieldError ->
                fieldError.field to (fieldError.defaultMessage ?: "유효하지 않은 값입니다")
            }
        log.warn("MethodArgumentNotValidException : {}", errors)
        return ResponseEntity(
            ApiResponse.error(ErrorType.INVALID_REQUEST, errors, path = request.requestURI),
            ErrorType.INVALID_REQUEST.status,
        )
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(
        e: ConstraintViolationException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiResponse<Any>> {
        val errors =
            e.constraintViolations.associate { violation ->
                val propertyPath = violation.propertyPath.toString()
                val fieldName = propertyPath.substringAfterLast('.')
                fieldName to violation.message
            }
        log.warn("ConstraintViolationException : {}", errors)
        return ResponseEntity(
            ApiResponse.error(ErrorType.INVALID_REQUEST, errors, path = request.requestURI),
            ErrorType.INVALID_REQUEST.status,
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleException(
        e: Exception,
        request: HttpServletRequest,
    ): ResponseEntity<ApiResponse<Any>> {
        log.error("Exception : {}", e.message, e)
        return ResponseEntity(
            ApiResponse.error(ErrorType.DEFAULT_ERROR, path = request.requestURI),
            ErrorType.DEFAULT_ERROR.status,
        )
    }
}
