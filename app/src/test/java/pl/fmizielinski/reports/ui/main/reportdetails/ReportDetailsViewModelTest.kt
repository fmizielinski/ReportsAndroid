package pl.fmizielinski.reports.ui.main.reportdetails

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.testIn
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestDispatcher
import org.junit.jupiter.api.Test
import pl.fmizielinski.reports.base.BaseViewModelTest
import pl.fmizielinski.reports.domain.error.toSnackBarData
import pl.fmizielinski.reports.domain.report.usecase.GetReportDetailsAttachmentGalleryNavArgsUseCase
import pl.fmizielinski.reports.domain.report.usecase.GetReportDetailsUseCase
import pl.fmizielinski.reports.domain.repository.EventsRepository
import pl.fmizielinski.reports.fixtures.domain.reportDetails
import pl.fmizielinski.reports.fixtures.domain.reportDetailsAttachment
import pl.fmizielinski.reports.fixtures.domain.simpleErrorException
import pl.fmizielinski.reports.fixtures.ui.attachmentGalleryNavArgs
import pl.fmizielinski.reports.ui.destinations.destinations.AttachmentGalleryDestination
import pl.fmizielinski.reports.ui.main.reportdetails.ReportDetailsViewModel.UiEvent
import pl.fmizielinski.reports.ui.navigation.DestinationData
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.withFirst

class ReportDetailsViewModelTest : BaseViewModelTest<ReportDetailsViewModel, UiEvent>() {

    private val id = 1
    private val handle = SavedStateHandle(
        mapOf("id" to id),
    )

    private val eventsRepository = spyk(EventsRepository())
    private val getReportDetailsUseCase: GetReportDetailsUseCase = mockk()
    private val getAttachmentGalleryNavArgsUseCase: GetReportDetailsAttachmentGalleryNavArgsUseCase = mockk()

    override fun createViewModel(dispatcher: TestDispatcher): ReportDetailsViewModel {
        return ReportDetailsViewModel(
            dispatcher = dispatcher,
            handle = handle,
            eventsRepository = eventsRepository,
            getReportDetailsUseCase = getReportDetailsUseCase,
            getAttachmentGalleryNavArgsUseCase = getAttachmentGalleryNavArgsUseCase,
        )
    }

    @Test
    fun `WHEN start THEN show report details`() = runTurbineTest {
        val expectedId = id
        val expectedTitle = "title"
        val expectedDescription = "description"
        val expectedReportDate = "12 Jun 2021, 12:00"
        val expectedAttachmentId = 2
        val expectedPath = "path"
        val attachments = listOf(
            reportDetailsAttachment(
                id = expectedAttachmentId,
                path = expectedPath,
            ),
        )
        val reportDetails = reportDetails(
            id = expectedId,
            title = expectedTitle,
            description = expectedDescription,
            reportDate = expectedReportDate,
            attachments = attachments,
        )
        coEvery { getReportDetailsUseCase(expectedId) } returns reportDetails

        val uiState = viewModel.uiState.testIn(context)

        context.launch { viewModel.onStart() }
        scheduler.advanceUntilIdle()

        val result = uiState.expectMostRecentItem()
        expectThat(result.report).isNotNull()
            .and {
                get { this.id } isEqualTo expectedId
                get { title } isEqualTo expectedTitle
                get { description } isEqualTo expectedDescription
                get { reportDate } isEqualTo expectedReportDate
                get { attachments }.hasSize(1)
                    .withFirst {
                        get { id } isEqualTo expectedAttachmentId
                        get { path } isEqualTo expectedPath
                    }
            }

        uiState.cancel()
    }

    @Test
    fun `GIVEN loading error WHEN start THEN post snack bar event AND post nav up event`() = runTurbineTest {
        val exception = simpleErrorException()
        coEvery { getReportDetailsUseCase(id) } throws exception

        val uiState = viewModel.uiState.testIn(context)

        context.launch { viewModel.onStart() }
        scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { eventsRepository.postNavUpEvent() }
        coVerify(exactly = 1) { eventsRepository.postSnackBarEvent(exception.toSnackBarData()) }

        uiState.cancelAndIgnoreRemainingEvents()
    }

    @Test
    fun `GIVEN has attachments WHEN PreviewAttachment event posted THEH post AttachmentGallery navigation event`() = runTurbineTest {
        val path = "path"
        val attachmentGalleryNavArgs = attachmentGalleryNavArgs(
            initialIndex = 1,
            attachments = arrayListOf(path),
        )
        val attachmentId = 2
        val reportDetails = reportDetails(
            attachments = listOf(reportDetailsAttachment(id = attachmentId)),
        )
        every { getAttachmentGalleryNavArgsUseCase(any(), any()) } returns attachmentGalleryNavArgs
        coEvery { getReportDetailsUseCase(id) } returns reportDetails

        val uiState = viewModel.uiState.testIn(context, name = "uiState")

        context.launch { viewModel.onStart() }
        scheduler.advanceUntilIdle()

        postUiEvent(UiEvent.PreviewAttachment(attachmentId))
        scheduler.advanceUntilIdle()
        val expectedDirection = AttachmentGalleryDestination(attachmentGalleryNavArgs)
        val directionSlot = slot<DestinationData>()
        coVerify(exactly = 1) { eventsRepository.postNavEvent(capture(directionSlot)) }
        expectThat(directionSlot.captured.direction.route) isEqualTo expectedDirection.route

        uiState.cancelAndIgnoreRemainingEvents()
    }
}
