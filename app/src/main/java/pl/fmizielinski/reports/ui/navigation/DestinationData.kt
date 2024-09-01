package pl.fmizielinski.reports.ui.navigation

import androidx.navigation.NavOptions
import com.ramcosta.composedestinations.generated.navgraphs.ReportsNavGraph
import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.utils.startDestination

data class DestinationData(
    val destination: DestinationSpec,
) {
    val navOptions: NavOptions? = buildNavOptions(destination)

    private fun buildNavOptions(destination: DestinationSpec): NavOptions? {
        val isStartDestination = ReportsNavGraph.nestedNavGraphs.any { graph ->
            graph.startDestination.baseRoute == destination.route
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

fun DestinationSpec.toDestinationData(): DestinationData = DestinationData(this)
