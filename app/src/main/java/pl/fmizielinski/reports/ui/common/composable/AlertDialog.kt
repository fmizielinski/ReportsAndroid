package pl.fmizielinski.reports.ui.common.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.fmizielinski.reports.R
import pl.fmizielinski.reports.ui.common.model.AlertDialogUiState
import pl.fmizielinski.reports.ui.theme.ReportsTheme
import androidx.compose.material3.AlertDialog as MaterialAlertDialog

@Composable
fun AlertDialog(
    uiState: AlertDialogUiState,
    callbacks: AlertDialogCallbacks,
) {
    MaterialAlertDialog(
        icon = {
            Icon(
                imageVector = ImageVector.vectorResource(uiState.iconResId),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
            )
        },
        title = {
            Text(stringResource(uiState.titleResId))
        },
        text = {
            Text(stringResource(uiState.messageResId))
        },
        confirmButton = {
            Text(
                text = stringResource(uiState.positiveButtonResId).uppercase(),
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(6.dp)
                    .clickable(onClick = callbacks.onPositiveClick),
            )
        },
        dismissButton = {
            Text(
                text = stringResource(uiState.negativeButtonResId).uppercase(),
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(6.dp)
                    .clickable(onClick = callbacks.onNegativeClick),
            )
        },
        onDismissRequest = callbacks.onDismissRequest,
    )
}

data class AlertDialogCallbacks(
    val onDismissRequest: () -> Unit,
    val onNegativeClick: () -> Unit,
    val onPositiveClick: () -> Unit,
)

@Preview(device = Devices.PIXEL_4)
@Composable
private fun AlertDialogPreview() {
    ReportsTheme {
        AlertDialog(
            uiState = previewAlertDialogUIiState,
            callbacks = emptyAlertDialogCallbacks,
        )
    }
}

private val previewAlertDialogUIiState = AlertDialogUiState(
    iconResId = R.drawable.ic_info_24dp,
    titleResId = R.string.common_label_permission,
    messageResId = R.string.common_label_cameraPermissionRationale,
    positiveButtonResId = R.string.common_label_settings,
    negativeButtonResId = R.string.common_label_cancel,
)

val emptyAlertDialogCallbacks = AlertDialogCallbacks(
    onDismissRequest = {},
    onNegativeClick = {},
    onPositiveClick = {},
)
