package pl.fmizielinski.reports.base

import app.cash.turbine.TurbineContext
import app.cash.turbine.turbineScope
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import pl.fmizielinski.reports.ui.base.BaseViewModel
import kotlin.time.Duration.Companion.seconds

abstract class BaseViewModelTest<T : BaseViewModel<*, *, *, *>> {

    abstract fun createViewModel(dispatcher: TestDispatcher): T

    protected fun runTurbineTest(body: suspend TestContext<T>.() -> Unit) {
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

    protected data class TestContext<T>(
        val context: TurbineContext,
        val scheduler: TestCoroutineScheduler,
        val viewModel: T,
    )
}
