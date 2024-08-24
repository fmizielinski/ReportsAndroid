package pl.fmizielinski.reports.ui.common

import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.spec.Direction
import pl.fmizielinski.reports.ui.navigation.DestinationData

fun DestinationsNavigator.consumeNavEvent(destinationData: DestinationData?) {
    if (destinationData == null) {
        navigateUp()
    } else {
        navigate(
            direction = Direction(destinationData.destination.route),
            navOptions = destinationData.navOptions,
        )
    }
}
