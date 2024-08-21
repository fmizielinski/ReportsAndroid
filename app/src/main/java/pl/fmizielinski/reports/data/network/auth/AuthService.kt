package pl.fmizielinski.reports.data.network.auth

import pl.fmizielinski.reports.data.network.auth.model.LoginResponseModel
import pl.fmizielinski.reports.data.network.auth.model.RegisterRequestModel
import pl.fmizielinski.reports.data.network.auth.model.RegisterResponseModel
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface AuthService {

    @GET("/auth/login")
    @Headers("Content-Type: application/json")
    suspend fun login(
        @Header("Authorization") credentials: String,
    ): LoginResponseModel

    @POST("/auth/register")
    @Headers("Accept: application/json")
    suspend fun register(@Body request: RegisterRequestModel): RegisterResponseModel
}
