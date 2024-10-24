package pl.fmizielinski.reports.ui.common.composable

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import pl.fmizielinski.reports.R
import pl.fmizielinski.reports.ui.common.model.ReportsTopAppBarUiState
import pl.fmizielinski.reports.ui.common.model.TopBarAction
import pl.fmizielinski.reports.ui.common.model.TopBarNavigationIcon
import pl.fmizielinski.reports.ui.theme.ReportsTheme

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ReportsTopAppBar(
    uiState: ReportsTopAppBarUiState,
    callbacks: ReportsTopAppBarCallbacks,
) {
    CenterAlignedTopAppBar(
        title = {
            if (uiState.title != null) {
                Text(text = stringResource(uiState.title))
            }
        },
        navigationIcon = {
            if (uiState.navigationIcon != null) {
                IconButton(
                    onClick = callbacks.onBackClicked,
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(uiState.navigationIcon.iconResId),
                        contentDescription = stringResource(uiState.navigationIcon.nameResId),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        },
        actions = {
            uiState.actions.forEach { action ->
                if (action.requirePermissions.isNotEmpty()) {
                    requestPermissions(
                        permissions = action.requirePermissions,
                        onGrantedCallback = { callbacks.onActionClicked(action) },
                        onShouldShowRationale = {
                            callbacks.onShouldShowPermissionRationale(action)
                        },
                    ) { onClick ->
                        Action(
                            action = action,
                            isEnabled = uiState.isEnabled,
                            onActionClicked = { onClick() },
                        )
                    }
                } else {
                    Action(
                        action = action,
                        isEnabled = uiState.isEnabled,
                        onActionClicked = callbacks.onActionClicked,
                    )
                }
            }
        },
    )
}

@Composable
fun Action(
    action: TopBarAction,
    isEnabled: Boolean,
    onActionClicked: ((TopBarAction) -> Unit)? = null,
) {
    IconButton(
        onClick = { onActionClicked?.invoke(action) },
        enabled = isEnabled,
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(action.iconResId),
            contentDescription = stringResource(action.nameResId),
            tint = MaterialTheme.colorScheme.onSurface,
        )
    }
}

data class ReportsTopAppBarCallbacks(
    val onBackClicked: () -> Unit,
    val onActionClicked: (TopBarAction) -> Unit,
    val onShouldShowPermissionRationale: (TopBarAction) -> Unit,
)

@Preview(device = Devices.PIXEL_4)
@Composable
private fun ReportsTopAppBarPreview() {
    ReportsTheme {
        ReportsTopAppBar(
            uiState = previewTopAppBarUiState,
            callbacks = emptyTopAppBarCallbacks,
        )
    }
}

val previewTopAppBarUiState = ReportsTopAppBarUiState(
    title = R.string.common_label_permission,
    navigationIcon = TopBarNavigationIcon.BACK,
    actions = listOf(TopBarAction.REGISTER),
    isEnabled = true,
)

val emptyTopAppBarCallbacks = ReportsTopAppBarCallbacks(
    onBackClicked = {},
    onActionClicked = {},
    onShouldShowPermissionRationale = {},
)
