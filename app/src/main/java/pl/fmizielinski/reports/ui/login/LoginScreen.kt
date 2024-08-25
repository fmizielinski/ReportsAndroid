package pl.fmizielinski.reports.ui.login

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import kotlinx.coroutines.launch
import pl.fmizielinski.reports.R
import pl.fmizielinski.reports.ui.base.BaseScreen
import pl.fmizielinski.reports.ui.common.composable.ReportsTextField
import pl.fmizielinski.reports.ui.login.LoginViewModel.UiEvent
import pl.fmizielinski.reports.ui.login.LoginViewModel.UiState
import pl.fmizielinski.reports.ui.navigation.graph.AuthGraph
import pl.fmizielinski.reports.ui.theme.ReportsTheme

@Destination<AuthGraph>(route = "Login", start = true)
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
                onShowPasswordClicked = {
                    coroutineScope.launch { viewModel.postUiEvent(UiEvent.ShowPasswordClicked) }
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
            Credentials(
                email = uiState.email,
                password = uiState.password,
                showPassword = uiState.showPassword,
                callbacks = callbacks,
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

@Composable
fun Credentials(
    email: String,
    password: String,
    showPassword: Boolean,
    callbacks: LoginCallbacks,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val passwordVisualTransformation = if (showPassword) {
        VisualTransformation.None
    } else {
        PasswordVisualTransformation()
    }
    val focusRequester = remember { FocusRequester() }

    ReportsTextField(
        value = email,
        onValueChange = callbacks.onEmailChanged,
        modifier = Modifier.padding(vertical = 4.dp)
            .fillMaxWidth(),
        labelResId = R.string.loginScreen_label_email,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next,
        ),
        keyboardActions = KeyboardActions { focusRequester.requestFocus() },
        singleLine = true,
        limit = 254,
    )
    ReportsTextField(
        value = password,
        onValueChange = callbacks.onPasswordChanged,
        modifier = Modifier.padding(vertical = 4.dp)
            .fillMaxWidth()
            .focusRequester(focusRequester),
        labelResId = R.string.loginScreen_label_password,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
        ),
        keyboardActions = KeyboardActions {
            callbacks.onLoginClicked()
            keyboardController?.hide()
        },
        singleLine = true,
        visualTransformation = passwordVisualTransformation,
        limit = 64,
        trailingIcon = {
            ShowPasswordButton(showPassword, callbacks.onShowPasswordClicked)
        },
    )
}

@Composable
fun ShowPasswordButton(
    showPassword: Boolean,
    onShowPasswordClicked: () -> Unit,
) {
    val drawableResId =
        if (showPassword) {
            R.drawable.ic_visibility_off_24dp
        } else {
            R.drawable.ic_visibility_24dp
        }
    val contentDescriptionResId =
        if (showPassword) {
            R.string.loginScreen_button_hidePassword
        } else {
            R.string.loginScreen_button_showPassword
        }
    IconButton(
        onClick = onShowPasswordClicked,
        content = {
            Icon(
                imageVector = ImageVector.vectorResource(drawableResId),
                contentDescription = stringResource(contentDescriptionResId),
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
    )
}

data class LoginCallbacks(
    val onEmailChanged: (String) -> Unit,
    val onPasswordChanged: (String) -> Unit,
    val onLoginClicked: () -> Unit,
    val onShowPasswordClicked: () -> Unit = {},
)

@Preview(showBackground = true, showSystemUi = true, device = Devices.PIXEL_4)
@Composable
fun LoginScreenPreview() {
    ReportsTheme {
        LoginForm(
            uiState = previewUiState,
            callbacks = emptyCallbacks,
        )
    }
}

private val previewUiState =
    UiState(
        email = "test@test.com",
        password = "password",
        isLoginButtonEnabled = true,
        showPassword = false,
    )

private val emptyCallbacks =
    LoginCallbacks(
        onEmailChanged = {},
        onPasswordChanged = {},
        onLoginClicked = {},
        onShowPasswordClicked = {},
    )
