package pl.fmizielinski.reports.ui.common.composable

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation

@Composable
fun ReportsTextField(
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    singleLine: Boolean = false,
    @StringRes labelResId: Int,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    limit: Int = Int.MAX_VALUE,
    trailingIcon: @Composable (() -> Unit)? = null,
    error: String? = null,
    supportingText: String? = null,
    enabled: Boolean = true,
) {
    var fieldValue by remember { mutableStateOf(TextFieldValue()) }

    TextField(
        value = fieldValue,
        onValueChange = {
            if (it.text.length <= limit) {
                fieldValue = it
                onValueChange(it.text)
            }
        },
        singleLine = singleLine,
        modifier = modifier,
        label = { Text(stringResource(labelResId)) },
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation,
        trailingIcon = trailingIcon,
        isError = error != null,
        supportingText = {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                supportingText?.let {
                    Text(
                        text = it,
                        modifier = Modifier.align(Alignment.End),
                    )
                }
                error?.let { Text(it) }
            }
        },
        enabled = enabled,
    )
}

@Composable
fun OutlinedReportsTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    singleLine: Boolean = false,
    @StringRes labelResId: Int? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    limit: Int = Int.MAX_VALUE,
    trailingIcon: @Composable (() -> Unit)? = null,
    error: String? = null,
    supportingText: String? = null,
    enabled: Boolean = true,
) {

    OutlinedTextField(
        value = value,
        onValueChange = {
            if (it.length <= limit) {
                onValueChange(it)
            }
        },
        singleLine = singleLine,
        modifier = modifier,
        label = {
            if (labelResId != null) {
                Text(stringResource(labelResId))
            }
        },
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation,
        trailingIcon = trailingIcon,
        isError = error != null,
        supportingText = {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                supportingText?.let {
                    Text(
                        text = it,
                        modifier = Modifier.align(Alignment.End),
                    )
                }
                error?.let { Text(it) }
            }
        },
        enabled = enabled,
    )
}
