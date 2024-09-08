package pl.fmizielinski.reports.domain.error

object ErrorReasons {

    object Auth {

        object Register {
            const val INVALID_CREDENTIALS = "auth.register.invalid-credentials"
            const val USER_ALREADY_EXISTS = "auth.register.user-already-exists"
            const val EMAIL_NOT_VALID = "auth.register.validation.email-not-valid"
            const val NAME_EMPTY = "auth.register.validation.name-empty"
            const val SURNAME_EMPTY = "auth.register.validation.surname-empty"
            const val PASSWORD_EMPTY = "auth.register.validation.password-empty"
        }
    }

    object Report {

        const val ACCESS_DENIED = "report.access-denied"

        object Create {
            const val INVALID_DATA = "report.create.invalid-data"
            const val TITLE_EMPTY = "report.create.validation.title-empty"
            const val DESCRIPTION_EMPTY = "report.create.validation.description-empty"
            const val UPLOAD_FAILED = "report.create.upload-failed"
        }
    }
}
