package pl.fmizielinski.reports.data.network.auth

import pl.fmizielinski.reports.data.network.auth.model.LoginResponseModel
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers

interface AuthService {

    @GET("/auth/login")
    @Headers("Content-Type: application/json")
    suspend fun login(
        @Header("Authorization") credentials: String,
    ): LoginResponseModel
}
