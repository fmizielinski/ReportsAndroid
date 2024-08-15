package pl.fmizielinski.reports.ui.common.composable

import androidx.annotation.StringRes
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation

@Composable
fun ReportsTextField(
    value: String,
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
) {
    var fieldRange by remember { mutableStateOf(TextRange.Zero) }
    val fieldValue = TextFieldValue(
        text = value,
        selection = fieldRange,
    )

    TextField(
        value = fieldValue,
        onValueChange = {
            if (it.text.length <= limit) {
                fieldRange = it.selection
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
        supportingText = { error?.let { Text(it) } },
    )
}
