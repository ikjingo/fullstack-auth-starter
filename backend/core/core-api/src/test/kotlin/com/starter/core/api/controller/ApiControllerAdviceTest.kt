package com.starter.core.api.controller

import com.starter.core.api.support.error.CoreApiException
import com.starter.core.api.support.error.ErrorCode
import com.starter.core.api.support.error.ErrorType
import com.starter.core.api.support.response.ResultType
import io.mockk.every
import io.mockk.mockk
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ConstraintViolation
import jakarta.validation.ConstraintViolationException
import jakarta.validation.Path
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException

@DisplayName("ApiControllerAdvice")
class ApiControllerAdviceTest {
    private lateinit var advice: ApiControllerAdvice
    private lateinit var mockRequest: HttpServletRequest

    @BeforeEach
    fun setUp() {
        advice = ApiControllerAdvice()
        mockRequest = mockk<HttpServletRequest>()
        every { mockRequest.requestURI } returns "/api/test"
    }

    @Nested
    @DisplayName("handleCoreApiException")
    inner class HandleCoreApiExceptionTest {
        @Test
        fun `CoreApiException 발생 시 해당 ErrorType의 상태 코드를 반환해야 한다`() {
            // Given
            val exception = CoreApiException(ErrorType.USER_NOT_FOUND)

            // When
            val response = advice.handleCoreApiException(exception, mockRequest)

            // Then
            assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
            assertThat(response.body?.result).isEqualTo(ResultType.ERROR)
            assertThat(response.body?.error?.code).isEqualTo(ErrorCode.E404.name)
            assertThat(response.body?.error?.message).isEqualTo("사용자를 찾을 수 없습니다.")
            assertThat(response.body?.error?.path).isEqualTo("/api/test")
            assertThat(response.body?.error?.timestamp).isNotBlank()
        }

        @Test
        fun `INVALID_CREDENTIALS 예외 발생 시 401 상태 코드를 반환해야 한다`() {
            // Given
            val exception = CoreApiException(ErrorType.INVALID_CREDENTIALS)

            // When
            val response = advice.handleCoreApiException(exception, mockRequest)

            // Then
            assertThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
            assertThat(response.body?.error?.code).isEqualTo(ErrorCode.E401.name)
        }

        @Test
        fun `DUPLICATE_EMAIL 예외 발생 시 400 상태 코드를 반환해야 한다`() {
            // Given
            val exception = CoreApiException(ErrorType.DUPLICATE_EMAIL)

            // When
            val response = advice.handleCoreApiException(exception, mockRequest)

            // Then
            assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
            assertThat(response.body?.error?.code).isEqualTo(ErrorCode.E400.name)
        }

        @Test
        fun `FORBIDDEN 예외 발생 시 403 상태 코드를 반환해야 한다`() {
            // Given
            val exception = CoreApiException(ErrorType.FORBIDDEN)

            // When
            val response = advice.handleCoreApiException(exception, mockRequest)

            // Then
            assertThat(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
            assertThat(response.body?.error?.code).isEqualTo(ErrorCode.E403.name)
        }

        @Test
        fun `TOO_MANY_REQUESTS 예외 발생 시 429 상태 코드를 반환해야 한다`() {
            // Given
            val exception = CoreApiException(ErrorType.TOO_MANY_REQUESTS)

            // When
            val response = advice.handleCoreApiException(exception, mockRequest)

            // Then
            assertThat(response.statusCode).isEqualTo(HttpStatus.TOO_MANY_REQUESTS)
            assertThat(response.body?.error?.code).isEqualTo(ErrorCode.E429.name)
        }

        @Test
        fun `ACCOUNT_LOCKED 예외 발생 시 423 상태 코드를 반환해야 한다`() {
            // Given
            val exception = CoreApiException(ErrorType.ACCOUNT_LOCKED)

            // When
            val response = advice.handleCoreApiException(exception, mockRequest)

            // Then
            assertThat(response.statusCode).isEqualTo(HttpStatus.LOCKED)
            assertThat(response.body?.error?.code).isEqualTo(ErrorCode.E423.name)
        }

        @Test
        fun `DEFAULT_ERROR 예외 발생 시 500 상태 코드를 반환해야 한다`() {
            // Given
            val exception = CoreApiException(ErrorType.DEFAULT_ERROR)

            // When
            val response = advice.handleCoreApiException(exception, mockRequest)

            // Then
            assertThat(response.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
            assertThat(response.body?.error?.code).isEqualTo(ErrorCode.E500.name)
        }

        @Test
        fun `예외에 추가 데이터가 있으면 응답에 포함되어야 한다`() {
            // Given
            val additionalData = mapOf("field" to "email", "reason" to "중복")
            val exception = CoreApiException(ErrorType.DUPLICATE_EMAIL, additionalData)

            // When
            val response = advice.handleCoreApiException(exception, mockRequest)

            // Then
            assertThat(response.body?.error?.data).isEqualTo(additionalData)
        }
    }

    @Nested
    @DisplayName("handleMethodArgumentNotValid")
    inner class HandleMethodArgumentNotValidTest {
        @Test
        fun `유효성 검사 실패 시 400 상태 코드와 필드별 에러 메시지를 반환해야 한다`() {
            // Given
            val fieldErrors =
                listOf(
                    FieldError("request", "email", "유효한 이메일 형식이 아닙니다"),
                    FieldError("request", "password", "비밀번호는 8자 이상이어야 합니다"),
                )

            val bindingResult = mockk<BindingResult>()
            every { bindingResult.fieldErrors } returns fieldErrors

            val exception = mockk<MethodArgumentNotValidException>()
            every { exception.bindingResult } returns bindingResult

            // When
            val response = advice.handleMethodArgumentNotValid(exception, mockRequest)

            // Then
            assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
            assertThat(response.body?.result).isEqualTo(ResultType.ERROR)
            assertThat(response.body?.error?.code).isEqualTo(ErrorCode.E400.name)
            assertThat(response.body?.error?.path).isEqualTo("/api/test")

            @Suppress("UNCHECKED_CAST")
            val errors = response.body?.error?.data as? Map<String, String>
            assertThat(errors).containsEntry("email", "유효한 이메일 형식이 아닙니다")
            assertThat(errors).containsEntry("password", "비밀번호는 8자 이상이어야 합니다")
        }
    }

    @Nested
    @DisplayName("handleConstraintViolation")
    inner class HandleConstraintViolationTest {
        @Test
        fun `ConstraintViolation 발생 시 400 상태 코드와 필드별 에러를 반환해야 한다`() {
            // Given
            val violation1 = createMockViolation("updateProfile.nickname", "닉네임은 필수입니다")
            val violation2 = createMockViolation("updateProfile.email", "이메일은 필수입니다")
            val exception = ConstraintViolationException(setOf(violation1, violation2))

            // When
            val response = advice.handleConstraintViolation(exception, mockRequest)

            // Then
            assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
            assertThat(response.body?.result).isEqualTo(ResultType.ERROR)
            assertThat(response.body?.error?.path).isEqualTo("/api/test")

            @Suppress("UNCHECKED_CAST")
            val errors = response.body?.error?.data as? Map<String, String>
            assertThat(errors).containsEntry("nickname", "닉네임은 필수입니다")
            assertThat(errors).containsEntry("email", "이메일은 필수입니다")
        }

        private fun createMockViolation(
            propertyPath: String,
            message: String,
        ): ConstraintViolation<*> =
            object : ConstraintViolation<Any> {
                override fun getMessage(): String = message

                override fun getMessageTemplate(): String = message

                override fun getRootBean(): Any? = null

                override fun getRootBeanClass(): Class<Any>? = null

                override fun getLeafBean(): Any? = null

                override fun getExecutableParameters(): Array<Any>? = null

                override fun getExecutableReturnValue(): Any? = null

                override fun getPropertyPath(): Path =
                    object : Path {
                        override fun iterator(): MutableIterator<Path.Node> = mutableListOf<Path.Node>().iterator()

                        override fun toString(): String = propertyPath
                    }

                override fun getInvalidValue(): Any? = null

                override fun getConstraintDescriptor(): jakarta.validation.metadata.ConstraintDescriptor<*>? = null

                override fun <U : Any?> unwrap(type: Class<U>?): U? = null
            }
    }

    @Nested
    @DisplayName("handleException")
    inner class HandleExceptionTest {
        @Test
        fun `예상치 못한 예외 발생 시 500 상태 코드를 반환해야 한다`() {
            // Given
            val exception = RuntimeException("예상치 못한 오류")

            // When
            val response = advice.handleException(exception, mockRequest)

            // Then
            assertThat(response.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
            assertThat(response.body?.result).isEqualTo(ResultType.ERROR)
            assertThat(response.body?.error?.code).isEqualTo(ErrorCode.E500.name)
            assertThat(response.body?.error?.message).isEqualTo("서버 오류가 발생했습니다.")
            assertThat(response.body?.error?.path).isEqualTo("/api/test")
        }

        @Test
        fun `NullPointerException 발생 시 500 상태 코드를 반환해야 한다`() {
            // Given
            val exception = NullPointerException("null 참조")

            // When
            val response = advice.handleException(exception, mockRequest)

            // Then
            assertThat(response.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
            assertThat(response.body?.error?.code).isEqualTo(ErrorCode.E500.name)
        }

        @Test
        fun `IllegalStateException 발생 시 500 상태 코드를 반환해야 한다`() {
            // Given
            val exception = IllegalStateException("잘못된 상태")

            // When
            val response = advice.handleException(exception, mockRequest)

            // Then
            assertThat(response.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
            assertThat(response.body?.error?.code).isEqualTo(ErrorCode.E500.name)
        }
    }

    @Nested
    @DisplayName("모든 ErrorType 테스트")
    inner class AllErrorTypesTest {
        @Test
        fun `모든 ErrorType에 대해 올바른 상태 코드를 반환해야 한다`() {
            ErrorType.entries.forEach { errorType ->
                // Given
                val exception = CoreApiException(errorType)

                // When
                val response = advice.handleCoreApiException(exception, mockRequest)

                // Then
                assertThat(response.statusCode)
                    .withFailMessage("ErrorType.$errorType should return ${errorType.status}")
                    .isEqualTo(errorType.status)
                assertThat(response.body?.error?.code).isEqualTo(errorType.code.name)
                assertThat(response.body?.error?.message).isEqualTo(errorType.message)
            }
        }
    }
}
