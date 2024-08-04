package pl.fmizielinski.reports.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import pl.fmizielinski.reports.ui.base.BaseViewModel

@Composable
fun <S, E, US, UE : E> BaseViewModel<S, E, US, UE>.collectUiStateAsState() = uiState.collectAsState(initialUiState)
