package pl.fmizielinski.reports.ui.login

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import kotlinx.coroutines.launch
import pl.fmizielinski.reports.R
import pl.fmizielinski.reports.ui.base.BaseScreen
import pl.fmizielinski.reports.ui.login.LoginViewModel.UiEvent
import pl.fmizielinski.reports.ui.login.LoginViewModel.UiState
import pl.fmizielinski.reports.ui.theme.ReportsTheme

@Destination<RootGraph>(start = true)
@Composable
fun LoginScreen() {
    BaseScreen<LoginViewModel, UiState, UiEvent> {
        LoginForm(
            uiState = state.value,
            callbacks = LoginCallbacks(
                onEmailChanged = {
                    coroutineScope.launch { viewModel.postUiEvent(UiEvent.EmailChanged(it)) }
                },
                onPasswordChanged = {
                    coroutineScope.launch { viewModel.postUiEvent(UiEvent.PasswordChanged(it)) }
                },
                onLoginClicked = {
                    coroutineScope.launch { viewModel.postUiEvent(UiEvent.LoginClicked) }
                },
            ),
        )
    }
}

@Composable
fun LoginForm(
    uiState: UiState,
    callbacks: LoginCallbacks,
) {
    Box(
        modifier = Modifier.fillMaxSize()
            .padding(20.dp),
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
        ) {
            val keyboardController = LocalSoftwareKeyboardController.current
            var usernameRange by remember { mutableStateOf(TextRange.Zero) }
            val usernameValue = TextFieldValue(
                text = uiState.email,
                selection = usernameRange,
            )
            var passwordRange by remember { mutableStateOf(TextRange.Zero) }
            val passwordValue = TextFieldValue(
                text = uiState.password,
                selection = passwordRange,
            )

            TextField(
                value = usernameValue,
                onValueChange = {
                    usernameRange = it.selection
                    callbacks.onEmailChanged(it.text)
                },
                modifier = Modifier.padding(vertical = 4.dp),
                label = { Text(stringResource(R.string.loginScreen_label_email)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            )
            TextField(
                value = passwordValue,
                onValueChange = {
                    passwordRange = it.selection
                    callbacks.onPasswordChanged(it.text)
                },

                modifier = Modifier.padding(vertical = 4.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = PasswordVisualTransformation(),
                label = { Text(stringResource(R.string.loginScreen_label_password)) },
            )
            Button(
                onClick = {
                    callbacks.onLoginClicked()
                    keyboardController?.hide()
                },
                modifier = Modifier.padding(vertical = 8.dp)
                    .align(Alignment.CenterHorizontally),
            ) {
                Text(stringResource(R.string.loginScreen_button_login))
            }
        }
    }
}

data class LoginCallbacks(
    val onEmailChanged: (String) -> Unit,
    val onPasswordChanged: (String) -> Unit,
    val onLoginClicked: () -> Unit,
)

@Preview(showBackground = true, showSystemUi = true, device = Devices.PIXEL_4)
@Composable
fun LoginScreenPreview() {
    ReportsTheme {
        LoginForm(
            uiState = uiState,
            callbacks = emptyCallbacks,
        )
    }
}

private val uiState = UiState(
    email = "test@test.com",
    password = "password",
)

private val emptyCallbacks = LoginCallbacks(
    onEmailChanged = {},
    onPasswordChanged = {},
    onLoginClicked = {},
)
