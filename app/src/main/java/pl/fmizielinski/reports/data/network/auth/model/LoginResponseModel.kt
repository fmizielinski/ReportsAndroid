package pl.fmizielinski.reports.data.network.auth.model

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponseModel(
    val id: Int,
    val email: String,
    val username: String,
    val token: String,
)
