package pl.fmizielinski.reports.fixtures.data

import pl.fmizielinski.reports.data.network.auth.model.LoginResponseModel

fun loginResponseModel(
    id: Int = 0,
    token: String = "token",
) = LoginResponseModel(
    id = id,
    token = token,
)
