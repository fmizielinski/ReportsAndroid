package pl.fmizielinski.reports.ui.common

import androidx.navigation.NavHostController
import com.ramcosta.composedestinations.spec.DestinationSpec

fun NavHostController.consumeNavEvent(destination: DestinationSpec?) {
    if (destination == null) {
        navigateUp()
    } else {
        navigate(destination.route)
    }
}
