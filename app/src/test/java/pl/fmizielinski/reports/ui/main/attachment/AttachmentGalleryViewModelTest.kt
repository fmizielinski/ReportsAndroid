package pl.fmizielinski.reports.ui.main.attachment

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.testIn
import kotlinx.coroutines.test.TestDispatcher
import org.junit.jupiter.api.Test
import pl.fmizielinski.reports.base.BaseViewModelTest
import pl.fmizielinski.reports.ui.main.attachment.AttachmentGalleryViewModel.UiEvent
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo

class AttachmentGalleryViewModelTest : BaseViewModelTest<AttachmentGalleryViewModel, UiEvent>() {

    private val initialIndex = 0
    private val attachments = listOf("attachment1", "attachment2")
    private val handle = SavedStateHandle(
        mapOf(
            "initialIndex" to initialIndex,
            "attachments" to arrayListOf("attachment1", "attachment2"),
        ),
    )

    override fun createViewModel(dispatcher: TestDispatcher): AttachmentGalleryViewModel {
        return AttachmentGalleryViewModel(dispatcher, handle)
    }

    @Test
    fun `WHEN view model created THEN display attachments`() = runTurbineTest {
        val uiState = viewModel.uiState.testIn(context)
        scheduler.advanceUntilIdle()

        expectThat(uiState.expectMostRecentItem()) {
            get { initialIndex } isEqualTo initialIndex
            get { this.attachments }.hasSize(2)
                .contains(attachments)
        }

        uiState.cancelAndIgnoreRemainingEvents()
    }
}
