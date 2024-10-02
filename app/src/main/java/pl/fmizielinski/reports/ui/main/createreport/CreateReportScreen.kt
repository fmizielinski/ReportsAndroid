package pl.fmizielinski.reports.ui.main.createreport

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.ramcosta.composedestinations.annotation.Destination
import pl.fmizielinski.reports.BuildConfig
import pl.fmizielinski.reports.R
import pl.fmizielinski.reports.ui.base.BaseScreen
import pl.fmizielinski.reports.ui.common.composable.ReportsTextField
import pl.fmizielinski.reports.ui.main.createreport.CreateReportViewModel.UiEvent
import pl.fmizielinski.reports.ui.main.createreport.CreateReportViewModel.UiState
import pl.fmizielinski.reports.ui.navigation.graph.MainGraph
import pl.fmizielinski.reports.ui.theme.AttachmentProgressSize
import pl.fmizielinski.reports.ui.theme.Margin
import pl.fmizielinski.reports.ui.theme.ReportsTheme

@Destination<MainGraph>(route = "CreateReport")
@Composable
fun CreateReportScreen() {
    BaseScreen<CreateReportViewModel, UiState, UiEvent> {
        ReportContent(
            uiState = state.value,
            callbacks = CreateReportCallbacks(
                onTitleChanged = { postUiEvent(UiEvent.TitleChanged(it)) },
                onDescriptionChanged = { postUiEvent(UiEvent.DescriptionChanged(it)) },
                onDeleteAttachment = { postUiEvent(UiEvent.DeleteAttachment(it)) },
                onListScrolled = { firstItemIndex ->
                    postUiEvent(UiEvent.ListScrolled(firstItemIndex))
                },
                onAttachmentClicked = { postUiEvent(UiEvent.PreviewAttachment(it)) },
            ),
        )
    }
}

@Composable
fun ReportContent(
    uiState: UiState,
    callbacks: CreateReportCallbacks,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        if (uiState.isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(Margin),
        ) {
            ReportDetails(
                uiState = uiState,
                callbacks = callbacks,
            )
            Attachements(
                attachments = uiState.attachments,
                onDeleteAttachment = callbacks.onDeleteAttachment,
                onAttachmentClicked = callbacks.onAttachmentClicked,
                onListScrolled = callbacks.onListScrolled,
                enabled = !uiState.isLoading,
            )
        }
    }
}

@Composable
fun ReportDetails(
    uiState: UiState,
    callbacks: CreateReportCallbacks,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val descriptionConfirmationFocusRequester = remember {
        FocusRequester()
    }

    val titleSupportingText = stringResource(
        R.string.common_label_characterCounter,
        uiState.titleLength,
        BuildConfig.REPORT_TITLE_LENGTH,
    )

    val descriptionSupportingText = stringResource(
        R.string.common_label_characterCounter,
        uiState.descriptionLength,
        BuildConfig.REPORT_DESCRIPTION_LENGTH,
    )

    ReportsTextField(
        onValueChange = callbacks.onTitleChanged,
        modifier = Modifier.fillMaxWidth()
            .padding(bottom = 16.dp),
        singleLine = true,
        labelResId = R.string.createReportScreen_label_title,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Next,
        ),
        keyboardActions = KeyboardActions {
            descriptionConfirmationFocusRequester.requestFocus()
        },
        limit = BuildConfig.REPORT_TITLE_LENGTH,
        supportingText = titleSupportingText,
        error = uiState.titleVerificationError?.let { stringResource(it) },
        enabled = !uiState.isLoading,
    )
    ReportsTextField(
        onValueChange = callbacks.onDescriptionChanged,
        modifier = Modifier.fillMaxWidth()
            .padding(bottom = 16.dp)
            .focusRequester(descriptionConfirmationFocusRequester),
        labelResId = R.string.createReportScreen_label_description,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Done,
        ),
        keyboardActions = KeyboardActions { keyboardController?.hide() },
        limit = BuildConfig.REPORT_DESCRIPTION_LENGTH,
        supportingText = descriptionSupportingText,
        error = uiState.descriptionVerificationError?.let { stringResource(it) },
        enabled = !uiState.isLoading,
    )
}

@Composable
fun Attachements(
    attachments: List<UiState.Attachment>,
    enabled: Boolean,
    onDeleteAttachment: (Int) -> Unit,
    onAttachmentClicked: (Int) -> Unit,
    onListScrolled: (Int) -> Unit,
) {
    val gridState = rememberLazyGridState()
    val arrangement = Arrangement.spacedBy(12.dp)

    LaunchedEffect(gridState) {
        snapshotFlow { gridState.firstVisibleItemIndex }
            .collect(onListScrolled)
    }

    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(ATTACHMENTS_GRID_COLUMNS),
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = arrangement,
        horizontalArrangement = arrangement,
    ) {
        items(attachments) { attachment ->
            AttachmentItem(
                attachment = attachment,
                enabled = enabled,
                onDeleteAttachment = onDeleteAttachment,
                onAttachmentClicked = onAttachmentClicked,
            )
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun AttachmentItem(
    attachment: UiState.Attachment,
    enabled: Boolean,
    onDeleteAttachment: (Int) -> Unit,
    onAttachmentClicked: (Int) -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box {
                GlideImage(
                    model = attachment.file,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                        .aspectRatio(1f)
                        .clickable { onAttachmentClicked(attachment.localId) },
                    contentScale = ContentScale.Crop,
                )
                if (attachment.isUploading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(AttachmentProgressSize)
                            .align(Alignment.Center),
                        progress = { attachment.progress },
                    )
                } else if (attachment.isUploaded || attachment.uploadFailed) {
                    val (iconResId, tint) = if (attachment.uploadFailed) {
                        R.drawable.ic_error_24dp to MaterialTheme.colorScheme.error
                    } else {
                        R.drawable.ic_check_circle_24dp to MaterialTheme.colorScheme.primary
                    }
                    Icon(
                        imageVector = ImageVector.vectorResource(iconResId),
                        contentDescription = null,
                        modifier = Modifier.size(AttachmentProgressSize)
                            .align(Alignment.Center),
                        tint = tint,
                    )
                }
            }
            IconButton(
                onClick = { onDeleteAttachment(attachment.localId) },
                modifier = Modifier.align(Alignment.End),
                enabled = enabled,
            ) {
                Image(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_delete_24dp),
                    contentDescription = stringResource(
                        R.string.createReportScreen_button_deleteAttachment,
                    ),
                )
            }
        }
    }
}

const val ATTACHMENTS_GRID_COLUMNS = 2

data class CreateReportCallbacks(
    val onTitleChanged: (String) -> Unit,
    val onDescriptionChanged: (String) -> Unit,
    val onDeleteAttachment: (Int) -> Unit,
    val onListScrolled: (firstItemIndex: Int) -> Unit,
    val onAttachmentClicked: (Int) -> Unit,
)

@Preview(showBackground = true, device = Devices.PIXEL_4)
@Composable
fun CreateReportScreenPreview() {
    ReportsTheme {
        ReportContent(
            uiState = previewUiState,
            callbacks = emptyCallbacks,
        )
    }
}

private val previewUiState = UiState(
    titleLength = 12,
    descriptionLength = 120,
    titleVerificationError = null,
    descriptionVerificationError = null,
    attachments = emptyList(),
    isLoading = false,
)

private val emptyCallbacks = CreateReportCallbacks(
    onTitleChanged = {},
    onDescriptionChanged = {},
    onDeleteAttachment = {},
    onListScrolled = {},
    onAttachmentClicked = {},
)
