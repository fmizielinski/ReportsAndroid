package pl.fmizielinski.reports.ui.main.createreport

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
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

        Attachements(uiState.attachments)
    }
}

@Composable
fun Attachements(attachments: List<Uri>) {
    val fileUtils = koinInject<FileUtils>()
    val context = LocalContext.current

    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = Modifier.fillMaxWidth(),
    ) {
        items(attachments) { attachment ->
            val photoBitmap = remember(attachment) {
                fileUtils.getBitmapFromUri(context, attachment)
            }

            Image(
                bitmap = photoBitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth()
                    .padding(4.dp),
            )
        }
    }
}

data class CreateReportCallbacks(
    val onTitleChanged: (String) -> Unit,
    val onDescriptionChanged: (String) -> Unit,
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
    attachments = listOf(
        Uri.parse(
            "android.resource://pl.fmizielinski.reports/mipmap-xxxhdpi/ic_launcher.webp",
        ),
        Uri.parse(
            "android.resource://pl.fmizielinski.reports/mipmap-xxxhdpi/ic_launcher.webp",
        ),
        Uri.parse(
            "android.resource://pl.fmizielinski.reports/mipmap-xxxhdpi/ic_launcher.webp",
        ),
        Uri.parse(
            "android.resource://pl.fmizielinski.reports/mipmap-xxxhdpi/ic_launcher.webp",
        ),
        Uri.parse(
            "android.resource://pl.fmizielinski.reports/mipmap-xxxhdpi/ic_launcher.webp",
        ),
    ),
)

private val emptyCallbacks = CreateReportCallbacks(
    onTitleChanged = {},
    onDescriptionChanged = {},
)
