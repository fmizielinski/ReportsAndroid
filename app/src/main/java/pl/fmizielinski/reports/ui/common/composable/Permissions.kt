package pl.fmizielinski.reports.ui.common.composable

import androidx.compose.runtime.Composable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@ExperimentalPermissionsApi
@Composable
fun requestPermission(
    permission: String,
    onGrantedCallback: Callback,
    onShouldShowRationale: Callback? = null,
    content: @Composable (onClick: Callback) -> Unit,
) {
    val permissionState = rememberPermissionState(permission)
    when {
        permissionState.status.isGranted -> {
            content(onGrantedCallback)
        }

        permissionState.status.shouldShowRationale -> {
            if (onShouldShowRationale != null) {
                content(onShouldShowRationale)
            } else {
                content { /* NOP */ }
            }
        }

        else -> {
            content { permissionState.launchPermissionRequest() }
        }
    }
}

typealias Callback = () -> Unit
