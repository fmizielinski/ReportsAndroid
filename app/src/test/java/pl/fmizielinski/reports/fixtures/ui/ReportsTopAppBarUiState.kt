package pl.fmizielinski.reports.fixtures.ui

import pl.fmizielinski.reports.ui.common.model.ReportsTopAppBarUiState
import pl.fmizielinski.reports.ui.common.model.TopBarAction

fun reportsTopAppBarUiState(
    title: Int? = 0,
    isBackVisible: Boolean = false,
    actions: List<TopBarAction> = emptyList(),
    isEnabled: Boolean = true,
) = ReportsTopAppBarUiState(
    title = title,
    isBackVisible = isBackVisible,
    actions = actions,
    isEnabled = isEnabled,
)
