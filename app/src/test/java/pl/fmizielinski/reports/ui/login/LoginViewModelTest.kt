package pl.fmizielinski.reports.ui.login

import io.mockk.mockk
import kotlinx.coroutines.CoroutineDispatcher
import org.junit.Assert.*
import org.junit.Test
import pl.fmizielinski.reports.domain.repository.EventsRepository
import pl.fmizielinski.reports.domain.usecase.auth.LoginUseCase

class LoginViewModelTest {

    private val loginUseCase: LoginUseCase = mockk()
    private val eventsRepository: EventsRepository = mockk()

    private fun viewModel(dispatcher: CoroutineDispatcher) = LoginViewModel(
        dispatcher = dispatcher,
        loginUseCase = loginUseCase,
        eventsRepository = eventsRepository,
    )
}
