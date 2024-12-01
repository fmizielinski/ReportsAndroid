package pl.fmizielinski.reports.ui.base

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.ParametersDefinition
import pl.fmizielinski.reports.ui.common.collectUiStateAsState

@Composable
inline fun <
        reified ViewModel : BaseViewModel<*, *, UiState, UiEvent>,
        reified UiState,
        reified UiEvent,
        > BaseScreen(
    noinline parameters: ParametersDefinition? = null,
    content: @Composable ScreenScope<ViewModel, UiState, UiEvent>.() -> Unit,
) {

    val viewModel: ViewModel = koinViewModel(parameters = parameters)
    val coroutineScope = rememberCoroutineScope()

    val state = viewModel.collectUiStateAsState()

    val scope = ScreenScope(
        viewModel = viewModel,
        coroutineScope = coroutineScope,
        state = state,
    )

    scope.content()

    DisposableEffect(key1 = viewModel) {
        coroutineScope.launch { viewModel.onStart() }
        onDispose {
            coroutineScope.launch { viewModel.onStop() }
        }
    }
}

data class ScreenScope<ViewModel : BaseViewModel<*, *, UiState, UiEvent>, UiState, UiEvent>(
    val viewModel: ViewModel,
    val coroutineScope: CoroutineScope,
    val state: State<UiState>,
) {

    fun postUiEvent(event: UiEvent) {
        coroutineScope.launch { viewModel.postUiEvent(event) }
    }
}
