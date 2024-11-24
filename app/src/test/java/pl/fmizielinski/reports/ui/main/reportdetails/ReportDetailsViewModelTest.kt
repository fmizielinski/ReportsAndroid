package pl.fmizielinski.reports.ui.main.reportdetails

import androidx.lifecycle.SavedStateHandle
import androidx.paging.PagingData
import app.cash.turbine.testIn
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestDispatcher
import org.junit.jupiter.api.Test
import pl.fmizielinski.reports.base.BaseViewModelTest
import pl.fmizielinski.reports.domain.error.toSnackBarData
import pl.fmizielinski.reports.domain.report.usecase.AddCommentUseCase
import pl.fmizielinski.reports.domain.report.usecase.GetCommentsUseCase
import pl.fmizielinski.reports.domain.report.usecase.GetReportDetailsAttachmentGalleryNavArgsUseCase
import pl.fmizielinski.reports.domain.report.usecase.GetReportDetailsUseCase
import pl.fmizielinski.reports.domain.repository.EventsRepository
import pl.fmizielinski.reports.fixtures.domain.comment
import pl.fmizielinski.reports.fixtures.domain.reportDetails
import pl.fmizielinski.reports.fixtures.domain.reportDetailsAttachment
import pl.fmizielinski.reports.fixtures.domain.simpleErrorException
import pl.fmizielinski.reports.fixtures.ui.attachmentGalleryNavArgs
import pl.fmizielinski.reports.ui.destinations.destinations.AttachmentGalleryDestination
import pl.fmizielinski.reports.ui.main.reportdetails.ReportDetailsViewModel.UiEvent
import pl.fmizielinski.reports.ui.main.reportdetails.ReportDetailsViewModel.UiState.Tab
import pl.fmizielinski.reports.ui.navigation.DestinationData
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
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
    private val getCommentsUseCase: GetCommentsUseCase = mockk()
    private val addCommentUseCase: AddCommentUseCase = mockk()

    override fun createViewModel(dispatcher: TestDispatcher): ReportDetailsViewModel {
        return ReportDetailsViewModel(
            dispatcher = dispatcher,
            handle = handle,
            eventsRepository = eventsRepository,
            getReportDetailsUseCase = getReportDetailsUseCase,
            getAttachmentGalleryNavArgsUseCase = getAttachmentGalleryNavArgsUseCase,
            getCommentsUseCase = getCommentsUseCase,
            addCommentUseCase = addCommentUseCase,
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
        coEvery { getCommentsUseCase.data } returns flowOf(
            PagingData.from(listOf(comment())),
        )

        val uiState = viewModel.uiState.testIn(context)
        val pagingContent = viewModel.pagingContent.testIn(context)

        context.launch { viewModel.onStart() }
        scheduler.advanceUntilIdle()

        val result = uiState.expectMostRecentItem()
        expectThat(result) {
            get { report }.isNotNull()
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
            get { comments.scrollToFirst }.isFalse()
            get { selectedTabIndex } isEqualTo 0
        }

        pagingContent.skipItems(2)
        pagingContent.ensureAllEventsConsumed()

        pagingContent.cancel()
        uiState.cancelAndIgnoreRemainingEvents()
    }

    @Test
    fun `GIVEN details loading error WHEN start THEN post snack bar event AND post nav up event`() = runTurbineTest {
        val exception = simpleErrorException()
        coEvery { getReportDetailsUseCase(id) } throws exception
        coEvery { getCommentsUseCase.data } returns flowOf(PagingData.empty())

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
        coEvery { getCommentsUseCase.data } returns flowOf(PagingData.empty())

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

    @Test
    fun `GIVEN comment typed WHEN Send clicked THEN refresh comments list`() = runTurbineTest {
        val commentsFlow = MutableStateFlow(
            PagingData.from(listOf(comment())),
        )
        val reportDetails = reportDetails()
        coEvery { getReportDetailsUseCase(id) } returns reportDetails
        coEvery { getCommentsUseCase.data } returns commentsFlow
        coEvery { getCommentsUseCase() } coAnswers {
            val data = PagingData.from(listOf(comment(), comment()))
            commentsFlow.emit(data)
        }
        coJustRun { addCommentUseCase(any(), any()) }

        val uiState = viewModel.uiState.testIn(context, name = "uiState")
        val pagingContent = viewModel.pagingContent.testIn(context)

        context.launch { viewModel.onStart() }
        scheduler.advanceUntilIdle()
        postUiEvent(UiEvent.TabClicked(Tab.COMMENTS))
        postUiEvent(UiEvent.CommentChanged("comment"))
        postUiEvent(UiEvent.SendClicked)
        scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { getCommentsUseCase() }

        pagingContent.cancelAndIgnoreRemainingEvents()
        uiState.cancelAndIgnoreRemainingEvents()
    }

    @Test
    fun `GIVEN add comment error visible WHEN Comment clicked THEN retry adding comment`() = runTurbineTest {
        val reportDetails = reportDetails()
        coEvery { getReportDetailsUseCase(id) } returns reportDetails
        coEvery { getCommentsUseCase.data } returns flowOf(PagingData.empty())
        coEvery { addCommentUseCase(any(), any()) } throws simpleErrorException()

        val uiState = viewModel.uiState.testIn(context, name = "uiState")

        context.launch { viewModel.onStart() }
        scheduler.advanceUntilIdle()
        postUiEvent(UiEvent.TabClicked(Tab.COMMENTS))
        postUiEvent(UiEvent.CommentChanged("comment"))
        postUiEvent(UiEvent.SendClicked)
        scheduler.advanceUntilIdle()
        postUiEvent(UiEvent.CommentClicked(id = null))
        scheduler.advanceUntilIdle()

        coVerify(exactly = 2) { addCommentUseCase(any(), any()) }

        uiState.cancelAndIgnoreRemainingEvents()
    }
}
