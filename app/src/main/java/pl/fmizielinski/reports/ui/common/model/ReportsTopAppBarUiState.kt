package pl.fmizielinski.reports.ui.common.model

import androidx.annotation.StringRes

data class ReportsTopAppBarUiState(
    @StringRes val title: Int?,
    val isBackVisible: Boolean,
    val actions: List<TopBarAction>,
)
