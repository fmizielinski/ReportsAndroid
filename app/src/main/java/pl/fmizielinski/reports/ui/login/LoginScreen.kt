package pl.fmizielinski.reports.ui.login

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import kotlinx.coroutines.launch
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import pl.fmizielinski.reports.R
import pl.fmizielinski.reports.ui.base.BaseScreen
import pl.fmizielinski.reports.ui.common.composable.ReportsTextField
import pl.fmizielinski.reports.ui.login.LoginViewModel.UiEvent
import pl.fmizielinski.reports.ui.login.LoginViewModel.UiState
import pl.fmizielinski.reports.ui.theme.ReportsTheme
import java.io.IOException

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
            .padding(32.dp),
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
        ) {
            val keyboardController = LocalSoftwareKeyboardController.current

            ReportsTextField(
                value = uiState.email,
                onValueChange = callbacks.onEmailChanged,
                modifier = Modifier.padding(vertical = 4.dp)
                    .fillMaxWidth(),
                labelResId = R.string.loginScreen_label_email,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                limit = 254,
            )
            ReportsTextField(
                value = uiState.password,
                onValueChange = callbacks.onPasswordChanged,
                modifier = Modifier.padding(vertical = 4.dp)
                    .fillMaxWidth(),
                labelResId = R.string.loginScreen_label_password,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                limit = 64,
            )
            Button(
                enabled = uiState.isLoginButtonEnabled,
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
    isLoginButtonEnabled = true,
)

private val emptyCallbacks = LoginCallbacks(
    onEmailChanged = {},
    onPasswordChanged = {},
    onLoginClicked = {},
)
