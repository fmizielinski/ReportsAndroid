package pl.fmizielinski.reports.domain.error

import kotlinx.serialization.Serializable

@Serializable
data class NetworkError(val code: String, val message: String)
