package pl.fmizielinski.reports.ui.common.composable

import androidx.compose.runtime.Composable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@ExperimentalPermissionsApi
@Composable
fun requestPermissions(
    permissions: List<String>,
    onGrantedCallback: Callback,
    onShouldShowRationale: Callback? = null,
    content: @Composable (onClick: Callback) -> Unit,
) {
    val permissionState = rememberMultiplePermissionsState(permissions)
    when {
        permissionState.allPermissionsGranted -> {
            content(onGrantedCallback)
        }

        permissionState.shouldShowRationale -> {
            if (onShouldShowRationale != null) {
                content(onShouldShowRationale)
            } else {
                content { /* NOP */ }
            }
        }

        else -> {
            content { permissionState.launchMultiplePermissionRequest() }
        }
    }
}

typealias Callback = () -> Unit
