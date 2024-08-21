package pl.fmizielinski.reports.fixtures.data

import pl.fmizielinski.reports.data.network.auth.model.RegisterResponseModel

fun registerResponseModel(
    id: Int = 0,
    token: String,
) = RegisterResponseModel(
    id = id,
    token = token,
)
