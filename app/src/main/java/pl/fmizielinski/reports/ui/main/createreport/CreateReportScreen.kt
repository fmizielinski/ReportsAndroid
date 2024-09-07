package pl.fmizielinski.reports.ui.main.createreport

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import pl.fmizielinski.reports.BuildConfig
import pl.fmizielinski.reports.R
import pl.fmizielinski.reports.ui.base.BaseScreen
import pl.fmizielinski.reports.ui.common.composable.ReportsTextField
import pl.fmizielinski.reports.ui.main.createreport.CreateReportViewModel.UiEvent
import pl.fmizielinski.reports.ui.main.createreport.CreateReportViewModel.UiState
import pl.fmizielinski.reports.ui.navigation.graph.MainGraph
import pl.fmizielinski.reports.ui.theme.Margin
import pl.fmizielinski.reports.ui.theme.ReportsTheme
import pl.fmizielinski.reports.ui.utils.FileUtils

@Destination<MainGraph>(route = "CreateReport")
@Composable
fun CreateReportScreen() {
    BaseScreen<CreateReportViewModel, UiState, UiEvent> {
        ReportContent(
            uiState = state.value,
            callbacks = CreateReportCallbacks(
                onTitleChanged = {
                    coroutineScope.launch { viewModel.postUiEvent(UiEvent.TitleChanged(it)) }
                },
                onDescriptionChanged = {
                    coroutineScope.launch { viewModel.postUiEvent(UiEvent.DescriptionChanged(it)) }
                },
                onDeleteAttachment = {
                    coroutineScope.launch { viewModel.postUiEvent(UiEvent.DeleteAttachment(it)) }
                },
            ),
        )
    }
}

@Composable
fun ReportContent(
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

    Column(
        modifier = Modifier.fillMaxSize()
            .padding(Margin),
    ) {
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
        )

        Attachements(
            attachments = uiState.attachments,
            onDeleteAttachment = callbacks.onDeleteAttachment,
        )
    }
}

@Composable
fun Attachements(
    attachments: List<Uri>,
    onDeleteAttachment: (Uri) -> Unit,
) {
    val fileUtils = koinInject<FileUtils>()
    val density = LocalDensity.current

    var width by remember { mutableStateOf(ATTACHMENTS_GRID_INITIAL_MIN_COLUMN_WIDTH) }
    val cardMinWidth = remember(width) {
        with(density) {
            (width.toFloat() / ATTACHMENTS_GRID_COLUMNS).toDp()
        }
    }

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(cardMinWidth),
        modifier = Modifier.fillMaxWidth()
            .onGloballyPositioned { coordinates ->
                width = coordinates.size.width
            },
        verticalItemSpacing = 8.dp,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(attachments) { attachment ->
            AttachmentItem(
                attachment = attachment,
                fileUtils = fileUtils,
                onDeleteAttachment = onDeleteAttachment,
            )
        }
    }
}

@Composable
fun AttachmentItem(
    attachment: Uri,
    fileUtils: FileUtils,
    onDeleteAttachment: (Uri) -> Unit,
) {
    val context = LocalContext.current

    val photoBitmap = remember(attachment) {
        fileUtils.getBitmapFromUri(context, attachment)
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Image(
                bitmap = photoBitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth(),
            )
            IconButton(
                onClick = { onDeleteAttachment(attachment) },
                modifier = Modifier.align(Alignment.End),
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

const val ATTACHMENTS_GRID_INITIAL_MIN_COLUMN_WIDTH = 100
const val ATTACHMENTS_GRID_COLUMNS = 3

data class CreateReportCallbacks(
    val onTitleChanged: (String) -> Unit,
    val onDescriptionChanged: (String) -> Unit,
    val onDeleteAttachment: (Uri) -> Unit,
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
)

private val emptyCallbacks = CreateReportCallbacks(
    onTitleChanged = {},
    onDescriptionChanged = {},
    onDeleteAttachment = {},
)
