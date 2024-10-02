package pl.fmizielinski.reports.ui.common.model

import androidx.annotation.StringRes
import pl.fmizielinski.reports.ui.common.model.TopBarNavigationIcon.BACK
import pl.fmizielinski.reports.ui.navigation.isStartDestination

data class ReportsTopAppBarUiState(
    @StringRes val title: Int?,
    val navigationIcon: TopBarNavigationIcon?,
    val actions: List<TopBarAction> = emptyList(),
    val isEnabled: Boolean = true,
) {
    constructor(
        @StringRes title: Int? = null,
        destination: String? = null,
        actions: List<TopBarAction> = emptyList(),
        isEnabled: Boolean = true,
    ) : this(
        title = title,
        navigationIcon = BACK.takeIf { destination?.isStartDestination == true },
        actions = actions,
        isEnabled = isEnabled,
    )
}
