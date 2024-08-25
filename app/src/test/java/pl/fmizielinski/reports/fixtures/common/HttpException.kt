package pl.fmizielinski.reports.fixtures.common

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.ResponseBody.Companion.toResponseBody
import pl.fmizielinski.reports.domain.error.NetworkError
import retrofit2.HttpException
import retrofit2.Response

fun <T> httpException(code: Int, error: NetworkError? = null): HttpException {
    val errors = buildList {
        error?.let { add(it) }
    }
    return httpException<T>(code = code, errors = errors)
}

fun <T> httpException(code: Int, errors: List<NetworkError>): HttpException {
    val responseBody = Json.encodeToString(errors).toResponseBody()
    return HttpException(
        Response.error<T>(code, responseBody),
    )
}
