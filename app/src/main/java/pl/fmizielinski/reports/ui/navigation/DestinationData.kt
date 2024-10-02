package pl.fmizielinski.reports.ui.navigation

import androidx.navigation.NavOptions
import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.spec.Direction
import com.ramcosta.composedestinations.utils.startDestination
import pl.fmizielinski.reports.ui.destinations.navgraphs.ReportsNavGraph

data class DestinationData(
    val direction: Direction,
) {
    val navOptions: NavOptions? = buildNavOptions(direction)

    private fun buildNavOptions(direction: Direction): NavOptions? {
        val isStartDestination = ReportsNavGraph.nestedNavGraphs.any { graph ->
            graph.startDestination.baseRoute == direction.route
        }
        return if (isStartDestination) {
            NavOptions.Builder()
                .setPopUpTo(ReportsNavGraph.route, inclusive = true)
                .build()
        } else {
            null
        }
    }
}

fun DestinationSpec.toDestinationData(): DestinationData = DestinationData(
    direction = Direction(this.route),
)
