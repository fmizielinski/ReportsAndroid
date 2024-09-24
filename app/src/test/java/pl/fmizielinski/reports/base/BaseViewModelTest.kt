package pl.fmizielinski.reports.base

import app.cash.turbine.TurbineContext
import app.cash.turbine.turbineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import pl.fmizielinski.reports.ui.base.BaseViewModel
import kotlin.time.Duration.Companion.seconds

abstract class BaseViewModelTest<ViewModel : BaseViewModel<*, *, *, UiEvent>, UiEvent> {

    abstract fun createViewModel(dispatcher: TestDispatcher): ViewModel

    protected fun runTurbineTest(body: suspend TestContext<ViewModel, UiEvent>.() -> Unit) {
        runTest {
            turbineScope(timeout = 1.seconds) {
                val scheduler = requireNotNull(coroutineContext[TestCoroutineScheduler.Key])
                TestContext(
                    context = this,
                    scheduler = scheduler,
                    viewModel = createViewModel(StandardTestDispatcher(scheduler)),
                ).body()
            }
        }
    }

    protected data class TestContext<ViewModel : BaseViewModel<*, *, *, UiEvent>, UiEvent>(
        val context: TurbineContext,
        val scheduler: TestCoroutineScheduler,
        val viewModel: ViewModel,
    ) {

        fun postUiEvent(event: UiEvent) {
            context.launch { viewModel.postUiEvent(event) }
        }
    }
}
