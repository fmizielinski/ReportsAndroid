package pl.fmizielinski.reports.domain.model

import androidx.annotation.StringRes

data class SnackBarData(
    @StringRes val messageResId: Int?,
    val secondsAlive: Long = DURATION_LONG,
) {
    val isValid: Boolean = messageResId != null

    companion object {
        const val DURATION_LONG = 3L
        const val DURATION_SHORT = 1L

        fun empty() = SnackBarData(messageResId = null, secondsAlive = 0)
    }
}
