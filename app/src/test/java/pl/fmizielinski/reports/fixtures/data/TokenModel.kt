package pl.fmizielinski.reports.fixtures.data

import pl.fmizielinski.reports.data.db.model.TokenModel

fun tokenModel(
    token: String = "token",
) = TokenModel(
    token = token,
)
