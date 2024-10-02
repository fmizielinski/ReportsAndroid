package pl.fmizielinski.reports.ui.main.attachment

import androidx.compose.runtime.Composable
import com.ramcosta.composedestinations.annotation.Destination
import pl.fmizielinski.reports.ui.base.BaseScreen
import pl.fmizielinski.reports.ui.main.attachment.AttachmentGalleryViewModel.UiEvent
import pl.fmizielinski.reports.ui.main.attachment.AttachmentGalleryViewModel.UiState
import pl.fmizielinski.reports.ui.main.attachment.model.AttachmentGalleryNavArgs
import pl.fmizielinski.reports.ui.navigation.graph.MainGraph

@Destination<MainGraph>(
    route = "AttachmentGallery",
    navArgs = AttachmentGalleryNavArgs::class,
)
@Composable
fun AttachmentGalleryScreen() {
    BaseScreen<AttachmentGalleryViewModel, UiState, UiEvent> {
        AttachmentGalleryContent(
            uiState = state.value,
        )
    }
}

@Composable
fun AttachmentGalleryContent(
    uiState: UiState,
) {

}
