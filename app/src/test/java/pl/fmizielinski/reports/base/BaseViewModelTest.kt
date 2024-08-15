package pl.fmizielinski.reports.base

import app.cash.turbine.TurbineContext
import app.cash.turbine.turbineScope
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.runTest

abstract class BaseViewModelTest {
    protected fun runTurbineTest(body: suspend TestContext.() -> Unit) {
        runTest {
            turbineScope {
                val scheduler = requireNotNull(coroutineContext[TestCoroutineScheduler.Key])
                TestContext(
                    context = this,
                    scheduler = scheduler,
                ).body()
            }
        }
    }

    protected data class TestContext(
        val context: TurbineContext,
        val scheduler: TestCoroutineScheduler,
    )
}
