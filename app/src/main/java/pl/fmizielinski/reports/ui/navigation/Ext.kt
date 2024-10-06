package pl.fmizielinski.reports.ui.navigation

import com.ramcosta.composedestinations.utils.startDestination
import pl.fmizielinski.reports.R
import pl.fmizielinski.reports.ui.common.model.FabUiState
import pl.fmizielinski.reports.ui.common.model.ReportsTopAppBarUiState
import pl.fmizielinski.reports.ui.common.model.TopBarAction.FILES
import pl.fmizielinski.reports.ui.common.model.TopBarAction.LOGOUT
import pl.fmizielinski.reports.ui.common.model.TopBarAction.PHOTO
import pl.fmizielinski.reports.ui.common.model.TopBarAction.REGISTER
import pl.fmizielinski.reports.ui.common.model.TopBarNavigationIcon.CLOSE
import pl.fmizielinski.reports.ui.destinations.destinations.AttachmentGalleryDestination
import pl.fmizielinski.reports.ui.destinations.destinations.CreateReportDestination
import pl.fmizielinski.reports.ui.destinations.destinations.LoginDestination
import pl.fmizielinski.reports.ui.destinations.destinations.RegisterDestination
import pl.fmizielinski.reports.ui.destinations.destinations.ReportsDestination
import pl.fmizielinski.reports.ui.destinations.navgraphs.ReportsNavGraph

val String.isStartDestination: Boolean
    get() = ReportsNavGraph.nestedNavGraphs.none { graph ->
        graph.startDestination.baseRoute == this
    }

fun String?.getAppBarUiState(isLoading: Boolean) = when (this) {
    LoginDestination.baseRoute -> ReportsTopAppBarUiState(
        destination = this,
        actions = listOf(REGISTER),
        isEnabled = !isLoading,
    )

    RegisterDestination.baseRoute -> ReportsTopAppBarUiState(
        title = R.string.registerScreen_title,
        destination = this,
        isEnabled = !isLoading,
    )

    CreateReportDestination.baseRoute -> ReportsTopAppBarUiState(
        title = R.string.createReportScreen_title,
        destination = this,
        actions = arrayListOf(
            FILES,
            PHOTO,
        ),
        isEnabled = !isLoading,
    )

    ReportsDestination.baseRoute -> ReportsTopAppBarUiState(
        title = R.string.reportsScreen_title,
        destination = this,
        actions = arrayListOf(LOGOUT),
    )

    AttachmentGalleryDestination.baseRoute -> ReportsTopAppBarUiState(
        title = R.string.attachmentGalleryScreen_title,
        navigationIcon = CLOSE,
    )

    else -> ReportsTopAppBarUiState(
        destination = this,
        isEnabled = !isLoading,
    )
}

fun String?.getFabUiState(): FabUiState? {
    return when (this) {
        CreateReportDestination.baseRoute -> FabUiState(
            icon = R.drawable.ic_save_24dp,
            contentDescription = R.string.common_button_saveReport,
        )

        ReportsDestination.baseRoute -> FabUiState(
            icon = R.drawable.ic_add_24dp,
            contentDescription = R.string.common_button_createReport,
        )

        LoginDestination.baseRoute -> FabUiState(
            icon = R.drawable.ic_login_24dp,
            contentDescription = R.string.common_button_login,
        )

        RegisterDestination.baseRoute -> FabUiState(
            icon = R.drawable.ic_person_add_24dp,
            contentDescription = R.string.common_button_register,
        )

        else -> null
    }
}
