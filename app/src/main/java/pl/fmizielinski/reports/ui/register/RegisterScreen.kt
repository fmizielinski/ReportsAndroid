package pl.fmizielinski.reports.ui.register

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.ColorFilter
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
import com.ramcosta.composedestinations.annotation.RootGraph
import pl.fmizielinski.reports.R
import pl.fmizielinski.reports.ui.base.BaseScreen
import pl.fmizielinski.reports.ui.common.composable.ReportsTextField
import pl.fmizielinski.reports.ui.register.RegisterViewModel.UiEvent
import pl.fmizielinski.reports.ui.register.RegisterViewModel.UiState
import pl.fmizielinski.reports.ui.theme.ReportsTheme

@Destination<RootGraph>
@Composable
fun RegisterScreen() {
    BaseScreen<RegisterViewModel, UiState, UiEvent> {
        RegisterForm(
            uiState = state.value,
            callbacks = RegisterCallbacks(
                loginDataCallbacks = RegisterCallbacks.LoginDataCallbacks(
                    onEmailChanged = {
                        TODO()
                    },
                    onPasswordChanged = {
                        TODO()
                    },
                    onPasswordConfirmationChanged = {
                        TODO()
                    },
                    onShowPasswordClicked = {
                        TODO()
                    },
                ),
                userDataCallbacks = RegisterCallbacks.UserDataCallbacks(
                    onNameChanged = {
                        TODO()
                    },
                    onSurnameChanged = {
                        TODO()
                    },
                ),
                onRegisterClicked = {
                    TODO()
                },
            ),
        )
    }
}

@Composable
fun RegisterForm(
    uiState: UiState,
    callbacks: RegisterCallbacks,
) {
    Box(
        modifier = Modifier.fillMaxSize()
            .padding(32.dp),
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
        ) {
            val keyboardController = LocalSoftwareKeyboardController.current

            LoginData(
                uiState = uiState.loginData,
                callbacks = callbacks.loginDataCallbacks,
            )
            UserData(
                uiState = uiState.userData,
                callbacks = callbacks.userDataCallbacks,
                onRegisterClicked = callbacks.onRegisterClicked,
            )
            Button(
                enabled = uiState.isRegisterButtonEnabled,
                onClick = {
                    callbacks.onRegisterClicked()
                    keyboardController?.hide()
                },
                modifier = Modifier.padding(vertical = 8.dp)
                    .align(Alignment.CenterHorizontally),
            ) {
                Text(stringResource(R.string.registerScreen_button_register))
            }
        }
    }
}

@Composable
fun LoginData(
    uiState: UiState.LoginData,
    callbacks: RegisterCallbacks.LoginDataCallbacks,
) {
    val focusRequester = remember(key1 = PASSWORD_FOCUS_REQUESTER) { FocusRequester() }

    ReportsTextField(
        value = uiState.email,
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
    PasswordTextField(
        labelResId = R.string.registerScreen_label_password,
        password = uiState.password,
        showPassword = uiState.showPassword,
        onPasswordChanged = callbacks.onPasswordChanged,
        onShowPasswordClicked = callbacks.onShowPasswordClicked,
        focusRequesterKey = PASSWORD_FOCUS_REQUESTER,
        nextFocusRequesterKey = PASSWORD_CONFIRMATION_FOCUS_REQUESTER,
    )
    PasswordTextField(
        labelResId = R.string.registerScreen_label_passwordConfirmation,
        password = uiState.password,
        showPassword = uiState.showPassword,
        onPasswordChanged = callbacks.onPasswordChanged,
        onShowPasswordClicked = callbacks.onShowPasswordClicked,
        focusRequesterKey = PASSWORD_CONFIRMATION_FOCUS_REQUESTER,
        nextFocusRequesterKey = NAME_FOCUS_REQUESTER,
    )
}

@Composable
fun PasswordTextField(
    @StringRes labelResId: Int,
    focusRequesterKey: String,
    nextFocusRequesterKey: String,
    password: String,
    showPassword: Boolean,
    onPasswordChanged: (String) -> Unit,
    onShowPasswordClicked: () -> Unit,
) {
    val passwordVisualTransformation = if (showPassword) {
        VisualTransformation.None
    } else {
        PasswordVisualTransformation()
    }
    val focusRequester = remember(key1 = focusRequesterKey) { FocusRequester() }
    val nextFocusRequester = remember(key1 = nextFocusRequesterKey) { FocusRequester() }

    ReportsTextField(
        value = password,
        onValueChange = onPasswordChanged,
        modifier = Modifier.padding(vertical = 4.dp)
            .fillMaxWidth()
            .focusRequester(focusRequester),
        labelResId = labelResId,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Next,
        ),
        keyboardActions = KeyboardActions { nextFocusRequester.requestFocus() },
        singleLine = true,
        visualTransformation = passwordVisualTransformation,
        limit = 64,
        trailingIcon = {
            ShowPasswordButton(
                showPassword,
                onShowPasswordClicked,
            )
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
            R.string.registerScreen_button_hidePassword
        } else {
            R.string.registerScreen_button_showPassword
        }
    Image(
        imageVector = ImageVector.vectorResource(drawableResId),
        contentDescription = stringResource(contentDescriptionResId),
        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
        modifier = Modifier.clickable { onShowPasswordClicked() },
    )
}

@Composable
fun UserData(
    uiState: UiState.UserData,
    callbacks: RegisterCallbacks.UserDataCallbacks,
    onRegisterClicked: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val nameFocusRequester = remember(key1 = NAME_FOCUS_REQUESTER) { FocusRequester() }
    val surnameFocusRequester = remember(key1 = SURNAME_FOCUS_REQUESTER) { FocusRequester() }

    ReportsTextField(
        value = uiState.name,
        onValueChange = callbacks.onNameChanged,
        modifier = Modifier.padding(vertical = 4.dp)
            .fillMaxWidth()
            .focusRequester(nameFocusRequester),
        labelResId = R.string.registerScreen_label_name,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next,
        ),
        keyboardActions = KeyboardActions { surnameFocusRequester.requestFocus() },
        singleLine = true,
        limit = 254,
    )
    ReportsTextField(
        value = uiState.surname,
        onValueChange = callbacks.onSurnameChanged,
        modifier = Modifier.padding(vertical = 4.dp)
            .fillMaxWidth()
            .focusRequester(surnameFocusRequester),
        labelResId = R.string.registerScreen_label_surname,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next,
        ),
        keyboardActions = KeyboardActions {
            onRegisterClicked()
            keyboardController?.hide()
        },
        singleLine = true,
        limit = 254,
    )
}

data class RegisterCallbacks(
    val loginDataCallbacks: LoginDataCallbacks,
    val userDataCallbacks: UserDataCallbacks,
    val onRegisterClicked: () -> Unit,
) {

    data class LoginDataCallbacks(
        val onEmailChanged: (String) -> Unit,
        val onPasswordChanged: (String) -> Unit,
        val onPasswordConfirmationChanged: (String) -> Unit,
        val onShowPasswordClicked: () -> Unit = {},
    )

    data class UserDataCallbacks(
        val onNameChanged: (String) -> Unit,
        val onSurnameChanged: (String) -> Unit,
    )
}

private const val PASSWORD_FOCUS_REQUESTER = "passwordFocusRequester"
private const val PASSWORD_CONFIRMATION_FOCUS_REQUESTER = "passwordConfirmationFocusRequester"
private const val NAME_FOCUS_REQUESTER = "nameFocusRequester"
private const val SURNAME_FOCUS_REQUESTER = "surnameFocusRequester"

@Preview(showBackground = true, showSystemUi = true, device = Devices.PIXEL_4)
@Composable
fun RegisterScreenPreview() {
    ReportsTheme {
        RegisterForm(
            uiState = previewUiState,
            callbacks = emptyCallbacks,
        )
    }
}

private val previewUiState = UiState(
    loginData = UiState.LoginData(
        email = "test@test.com",
        password = "password",
        passwordConfirmation = "password",
        showPassword = false,
    ),
    userData = UiState.UserData(
        name = "John",
        surname = "Doe",
    ),
    isRegisterButtonEnabled = true,
)

private val emptyCallbacks = RegisterCallbacks(
    loginDataCallbacks = RegisterCallbacks.LoginDataCallbacks(
        onEmailChanged = {},
        onPasswordChanged = {},
        onPasswordConfirmationChanged = {},
        onShowPasswordClicked = {},
    ),
    userDataCallbacks = RegisterCallbacks.UserDataCallbacks(
        onNameChanged = {},
        onSurnameChanged = {},
    ),
    onRegisterClicked = {},
)