package pl.fmizielinski.reports.fixtures.domain

import pl.fmizielinski.reports.domain.model.RegistrationData

fun registrationData(
    email: String = "email",
    name: String = "name",
    surname: String = "surname",
    password: String = "password",
) = RegistrationData(
    email = email,
    name = name,
    surname = surname,
    password = password,
)
