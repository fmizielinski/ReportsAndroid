package pl.fmizielinski.reports.ui.common.composable

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import pl.fmizielinski.reports.ui.theme.ReportsTheme

@ExperimentalPermissionsApi
@ExperimentalMaterial3Api
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
            if (uiState.isBackVisible) {
                IconButton(
                    onClick = callbacks.onBackClicked,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = stringResource(R.string.common_button_back),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        },
        actions = {
            uiState.actions.forEach { action ->
                if (action.requirePermission != null) {
                    requestPermission(
                        permission = action.requirePermission,
                        onGrantedCallback = { callbacks.onActionClicked(action) },
                        onShouldShowRationale = {
                            callbacks.onShouldShowPermissionRationale(action.requirePermission)
                        },
                    ) { onClick ->
                        Action(
                            action = action,
                            onActionClicked = { onClick() },
                        )
                    }
                } else {
                    Action(
                        action = action,
                        onActionClicked = callbacks.onActionClicked,
                    )
                }
            }
        },
    )
}

@ExperimentalPermissionsApi
@Composable
fun Action(
    action: TopBarAction,
    onActionClicked: ((TopBarAction) -> Unit)? = null,
) {
    IconButton(
        onClick = { onActionClicked?.invoke(action) },
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
    val onShouldShowPermissionRationale: (String) -> Unit,
)

@ExperimentalMaterial3Api
@ExperimentalPermissionsApi
@Preview(device = Devices.PIXEL_4)
@Composable
fun ReportsTopAppBarPreview() {
    ReportsTheme {
        ReportsTopAppBar(
            uiState = previewTopAppBarUiState,
            callbacks = emptyTopAppBarCallbacks,
        )
    }
}

val previewTopAppBarUiState = ReportsTopAppBarUiState(
    title = R.string.common_label_permission,
    isBackVisible = true,
    actions = listOf(TopBarAction.REGISTER),
)

val emptyTopAppBarCallbacks = ReportsTopAppBarCallbacks(
    onBackClicked = {},
    onActionClicked = {},
    onShouldShowPermissionRationale = {},
)
