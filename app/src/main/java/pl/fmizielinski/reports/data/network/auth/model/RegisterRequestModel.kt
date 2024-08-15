package pl.fmizielinski.reports.data.network.auth.model

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequestModel(
    val email: String,
    val name: String,
    val surname: String,
    val password: String,
)
