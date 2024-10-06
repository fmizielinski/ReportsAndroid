package pl.fmizielinski.reports.ui.main.attachment

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.ramcosta.composedestinations.annotation.Destination
import pl.fmizielinski.reports.ui.base.BaseScreen
import pl.fmizielinski.reports.ui.main.attachment.AttachmentGalleryViewModel.UiEvent
import pl.fmizielinski.reports.ui.main.attachment.AttachmentGalleryViewModel.UiState
import pl.fmizielinski.reports.ui.main.attachment.model.AttachmentGalleryNavArgs
import pl.fmizielinski.reports.ui.navigation.graph.MainGraph
import pl.fmizielinski.reports.ui.theme.ReportsTheme
import kotlin.math.absoluteValue

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

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun AttachmentGalleryContent(
    uiState: UiState,
) {
    val pagerState = rememberPagerState(initialPage = uiState.initialIndex) {
        uiState.attachments.size
    }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(32.dp),
    ) { page ->
        GlideImage(
            model = uiState.attachments[page],
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
                .graphicsLayer {
                    val pageOffset = with(pagerState) {
                        ((currentPage - page) + currentPageOffsetFraction).absoluteValue
                    }
                    val fraction = 1f - pageOffset.coerceIn(0f, 1f)
                    alpha = lerp(
                        start = 0.3f,
                        stop = 1f,
                        fraction = fraction,
                    )
                    scaleX = lerp(
                        start = 0.9f,
                        stop = 1f,
                        fraction = fraction,
                    )
                    scaleY = lerp(
                        start = 0.7f,
                        stop = 1f,
                        fraction = fraction,
                    )
                },
            contentScale = ContentScale.Inside,
        )
    }
}

@Preview(showBackground = true, device = Devices.PIXEL_4)
@Composable
fun AttachmentGalleryScreenPreview() {
    ReportsTheme {
        AttachmentGalleryContent(previewUiState)
    }
}

private val previewUiState = UiState(
    initialIndex = 1,
    attachments = emptyList(),
)
