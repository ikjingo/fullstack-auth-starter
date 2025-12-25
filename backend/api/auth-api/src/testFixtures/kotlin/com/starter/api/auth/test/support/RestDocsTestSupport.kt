package com.starter.api.auth.test.support

import com.epages.restdocs.apispec.ResourceSnippetParameters
import com.epages.restdocs.apispec.Schema
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import com.starter.api.auth.security.AuthUser
import com.starter.api.auth.security.jwt.JwtProperties
import com.starter.api.auth.security.jwt.JwtTokenProvider
import com.starter.api.auth.service.ratelimit.RateLimitService
import com.starter.api.auth.service.token.TokenBlacklistService
import com.starter.api.auth.test.fixture.AuthUserFixture
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.context.annotation.Import
import org.springframework.restdocs.RestDocumentationContextProvider
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

/**
 * RestDocs 테스트를 위한 기본 지원 클래스
 *
 * @WebMvcTest와 함께 사용하며, RestDocs 문서화를 위한 기본 설정을 제공합니다.
 */
@ExtendWith(RestDocumentationExtension::class)
@AutoConfigureRestDocs
@Import(RestDocsConfiguration::class)
abstract class RestDocsTestSupport {
    @Autowired
    protected lateinit var objectMapper: ObjectMapper

    @Autowired
    protected lateinit var restDocs: RestDocumentationResultHandler

    protected lateinit var mockMvc: MockMvc

    @MockkBean
    protected lateinit var jwtTokenProvider: JwtTokenProvider

    @MockkBean
    protected lateinit var jwtProperties: JwtProperties

    @MockkBean
    protected lateinit var tokenBlacklistService: TokenBlacklistService

    @MockkBean
    protected lateinit var rateLimitService: RateLimitService

    protected val testUserId = 1L
    protected val authUser: AuthUser = AuthUserFixture.create(userId = testUserId)

    @BeforeEach
    fun setUp(
        webApplicationContext: WebApplicationContext,
        restDocumentation: RestDocumentationContextProvider,
    ) {
        mockMvc =
            MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply<DefaultMockMvcBuilder>(MockMvcRestDocumentation.documentationConfiguration(restDocumentation))
                .alwaysDo<DefaultMockMvcBuilder>(MockMvcResultHandlers.print())
                .alwaysDo<DefaultMockMvcBuilder>(restDocs)
                .build()

        // Security Context 설정
        val authentication = UsernamePasswordAuthenticationToken(authUser, null, authUser.authorities)
        SecurityContextHolder.getContext().authentication = authentication
    }

    // === 공통 Response Field Descriptors ===

    protected fun apiResponseFields(dataFields: List<FieldDescriptor>): List<FieldDescriptor> =
        listOf(
            fieldWithPath("result").description("API 결과 상태 (SUCCESS/ERROR)"),
            fieldWithPath("data").description("응답 데이터").optional(),
            fieldWithPath("error").description("에러 정보").optional(),
        ) + dataFields.map { it.withPrefix("data.") }

    protected fun apiErrorResponseFields(): List<FieldDescriptor> =
        listOf(
            fieldWithPath("result").description("API 결과 상태 (ERROR)"),
            fieldWithPath("data").description("응답 데이터 (null)").optional(),
            fieldWithPath("error").description("에러 정보"),
            fieldWithPath("error.code").description("에러 코드"),
            fieldWithPath("error.message").description("에러 메시지"),
        )

    // Field Descriptor prefix 확장 함수
    private fun FieldDescriptor.withPrefix(prefix: String): FieldDescriptor =
        fieldWithPath(prefix + this.path).description(this.description).apply {
            if (this@withPrefix.isOptional) {
                optional()
            }
        }

    // === OpenAPI Resource Snippets Helper ===

    protected fun resourceSnippet(
        summary: String,
        description: String = "",
        tag: String,
        requestSchema: Schema? = null,
        responseSchema: Schema? = null,
    ): ResourceSnippetParameters =
        ResourceSnippetParameters
            .builder()
            .tag(tag)
            .summary(summary)
            .description(description)
            .apply {
                requestSchema?.let { requestSchema(it) }
                responseSchema?.let { responseSchema(it) }
            }.build()
}
