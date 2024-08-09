package pl.fmizielinski.reports.ui.common

import androidx.navigation.NavHostController
import com.ramcosta.composedestinations.spec.Direction

fun NavHostController.consumeNavEvent(direction: Direction?) {
    if (direction == null) {
        navigateUp()
    } else {
        navigate(direction.route)
    }
}
