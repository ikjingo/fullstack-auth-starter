package com.starter.core.api.support.response

import com.starter.core.api.support.error.ErrorMessage
import com.starter.core.api.support.error.ErrorType

data class ApiResponse<T> private constructor(
    val result: ResultType,
    val data: T? = null,
    val error: ErrorMessage? = null,
) {
    companion object {
        fun success(): ApiResponse<Any> = ApiResponse(ResultType.SUCCESS, null, null)

        fun <S> success(data: S): ApiResponse<S> = ApiResponse(ResultType.SUCCESS, data, null)

        fun <S> error(
            error: ErrorType,
            data: Any? = null,
            message: String? = null,
            path: String? = null,
        ): ApiResponse<S> = ApiResponse(ResultType.ERROR, null, ErrorMessage.of(error, data, message, path))
    }
}
