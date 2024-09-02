package pl.fmizielinski.reports.ui.main.createreport

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.ramcosta.composedestinations.annotation.Destination
import kotlinx.coroutines.launch
import pl.fmizielinski.reports.BuildConfig
import pl.fmizielinski.reports.R
import pl.fmizielinski.reports.ui.base.BaseScreen
import pl.fmizielinski.reports.ui.common.composable.ReportsTextField
import pl.fmizielinski.reports.ui.main.createreport.CreateReportViewModel.UiEvent
import pl.fmizielinski.reports.ui.main.createreport.CreateReportViewModel.UiState
import pl.fmizielinski.reports.ui.navigation.graph.MainGraph
import pl.fmizielinski.reports.ui.theme.Margin
import pl.fmizielinski.reports.ui.theme.ReportsTheme

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
    Column(
        modifier = Modifier.fillMaxSize()
            .padding(Margin),
    ) {
        ReportsTextField(
            onValueChange = callbacks.onTitleChanged,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            labelResId = R.string.createReportScreen_label_title,
            limit = BuildConfig.REPORT_TITLE_LENGTH,
        )
        ReportsTextField(
            onValueChange = callbacks.onDescriptionChanged,
            modifier = Modifier.fillMaxWidth(),
            labelResId = R.string.createReportScreen_label_description,
            limit = BuildConfig.REPORT_DESCRIPTION_LENGTH,
        )
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

private val previewUiState = UiState()

private val emptyCallbacks = CreateReportCallbacks(
    onTitleChanged = {},
    onDescriptionChanged = {},
)
