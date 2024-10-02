package pl.fmizielinski.reports.fixtures.ui

import pl.fmizielinski.reports.ui.common.model.ReportsTopAppBarUiState
import pl.fmizielinski.reports.ui.common.model.TopBarAction
import pl.fmizielinski.reports.ui.common.model.TopBarNavigationIcon

fun reportsTopAppBarUiState(
    title: Int? = 0,
    navigationIcon: TopBarNavigationIcon? = null,
    actions: List<TopBarAction> = emptyList(),
    isEnabled: Boolean = true,
) = ReportsTopAppBarUiState(
    title = title,
    navigationIcon = navigationIcon,
    actions = actions,
    isEnabled = isEnabled,
)
