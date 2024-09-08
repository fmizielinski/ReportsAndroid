package pl.fmizielinski.reports.ui.common.model

import androidx.annotation.StringRes
import pl.fmizielinski.reports.ui.navigation.isStartDestination

data class ReportsTopAppBarUiState(
    @StringRes val title: Int?,
    val isBackVisible: Boolean,
    val actions: List<TopBarAction>,
) {
    constructor(
        @StringRes title: Int? = null,
        destination: String? = null,
        actions: List<TopBarAction> = emptyList(),
    ) : this(
        title = title,
        isBackVisible = destination?.isStartDestination == true,
        actions = actions,
    )
}
