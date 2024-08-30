package pl.fmizielinski.reports.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Report(
    val id: Int,
    val title: String,
    val description: String,
    val reportDate: String,
    val comments: Int,
)
