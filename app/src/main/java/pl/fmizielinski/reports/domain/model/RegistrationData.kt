package pl.fmizielinski.reports.domain.model

import pl.fmizielinski.reports.data.network.auth.model.RegisterRequestModel

data class RegistrationData(
    val email: String,
    val name: String,
    val surname: String,
    val password: String,
)

fun RegistrationData.toRegisterRequestModel() = RegisterRequestModel(
    email = email,
    name = name,
    surname = surname,
    password = password,
)
