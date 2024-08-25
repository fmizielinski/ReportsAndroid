package pl.fmizielinski.reports.domain.mapper

import kotlinx.serialization.json.Json
import pl.fmizielinski.reports.domain.error.NetworkError
import retrofit2.HttpException

fun HttpException.parseErrorBody(): List<NetworkError> {
    val errorBody = response()?.errorBody()
    return errorBody?.let { body ->
        Json.decodeFromString<List<NetworkError>>(body.string())
    } ?: emptyList()
}
