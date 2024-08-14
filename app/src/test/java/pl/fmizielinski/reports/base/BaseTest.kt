package pl.fmizielinski.reports.base

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.TurbineContext
import app.cash.turbine.turbineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration

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
