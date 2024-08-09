package pl.fmizielinski.reports.domain.mapper

import pl.fmizielinski.reports.data.db.model.UserModel
import pl.fmizielinski.reports.data.network.auth.model.LoginResponseModel

fun LoginResponseModel.toUserModel() = UserModel(
    email = email,
    username = username,
    token = token,
)
