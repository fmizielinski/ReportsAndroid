package pl.fmizielinski.reports.fixtures.common

import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response

fun <T> httpException(code: Int) = HttpException(
    Response.error<T>(code, "".toResponseBody()),
)
