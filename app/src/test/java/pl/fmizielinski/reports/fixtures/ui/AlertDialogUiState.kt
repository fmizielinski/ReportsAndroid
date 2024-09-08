package pl.fmizielinski.reports.fixtures.ui

import pl.fmizielinski.reports.ui.common.model.AlertDialogUiState

fun alertDialogUiState(
    iconResId: Int = 1,
    titleResId: Int = 2,
    messageResId: Int = 3,
    positiveButtonResId: Int = 4,
    negativeButtonResId: Int = 5,
) = AlertDialogUiState(
    iconResId = iconResId,
    titleResId = titleResId,
    messageResId = messageResId,
    positiveButtonResId = positiveButtonResId,
    negativeButtonResId = negativeButtonResId,
)
