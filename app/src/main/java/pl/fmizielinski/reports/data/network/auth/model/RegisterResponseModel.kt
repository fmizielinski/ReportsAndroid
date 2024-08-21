package pl.fmizielinski.reports.data.network.auth.model

import kotlinx.serialization.Serializable

@Serializable
data class RegisterResponseModel(
    val id: Int,
    val token: String,
)
