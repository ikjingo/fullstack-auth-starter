package com.starter.api.auth.test.support

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler
import org.springframework.restdocs.operation.preprocess.Preprocessors.modifyHeaders
import org.springframework.restdocs.operation.preprocess.Preprocessors.modifyUris
import org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest
import org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse
import org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint

@TestConfiguration
class RestDocsConfiguration {
    @Bean
    fun restDocumentationResultHandler(): RestDocumentationResultHandler =
        MockMvcRestDocumentation.document(
            "{class-name}/{method-name}",
            preprocessRequest(
                modifyUris()
                    .scheme("https")
                    .host("api.zenless.com")
                    .removePort(),
                modifyHeaders()
                    .remove("X-CSRF-TOKEN")
                    .remove("Host"),
                prettyPrint(),
            ),
            preprocessResponse(
                modifyHeaders()
                    .remove("X-Content-Type-Options")
                    .remove("X-XSS-Protection")
                    .remove("Cache-Control")
                    .remove("Pragma")
                    .remove("Expires")
                    .remove("X-Frame-Options")
                    .remove("Vary"),
                prettyPrint(),
            ),
        )
}
