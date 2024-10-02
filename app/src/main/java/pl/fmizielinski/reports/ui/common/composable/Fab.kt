package pl.fmizielinski.reports.ui.common.composable

import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import pl.fmizielinski.reports.ui.common.model.FabUiState

@Composable
fun Fab(
    uiState: FabUiState?,
    onFabClicked: () -> Unit,
) {
    FloatingActionButton(
        onClick = onFabClicked,
        content = {
            if (uiState != null) {
                Icon(
                    imageVector = ImageVector.vectorResource(uiState.icon),
                    contentDescription = stringResource(uiState.contentDescription),
                )
            }
        },
    )
}
