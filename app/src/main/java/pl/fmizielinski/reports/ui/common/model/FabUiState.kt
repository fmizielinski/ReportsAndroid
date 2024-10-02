package pl.fmizielinski.reports.ui.common.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class FabUiState(
    @DrawableRes val icon: Int,
    @StringRes val contentDescription: Int,
)
