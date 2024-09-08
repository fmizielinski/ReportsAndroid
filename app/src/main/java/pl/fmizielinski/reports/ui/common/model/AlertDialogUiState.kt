package pl.fmizielinski.reports.ui.common.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class AlertDialogUiState(
    @DrawableRes val iconResId: Int,
    @StringRes val titleResId: Int,
    @StringRes val messageResId: Int,
    @StringRes val positiveButtonResId: Int,
    @StringRes val negativeButtonResId: Int,
)
