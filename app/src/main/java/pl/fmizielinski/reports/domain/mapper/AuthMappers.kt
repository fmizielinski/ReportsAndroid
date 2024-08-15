package pl.fmizielinski.reports.domain.mapper

import pl.fmizielinski.reports.data.db.model.TokenModel
import pl.fmizielinski.reports.data.network.auth.model.LoginResponseModel

fun LoginResponseModel.toTokenModel() = TokenModel(token = token)
