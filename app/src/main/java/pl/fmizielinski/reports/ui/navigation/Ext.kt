package pl.fmizielinski.reports.ui.navigation

import com.ramcosta.composedestinations.utils.startDestination
import pl.fmizielinski.reports.ui.destinations.navgraphs.ReportsNavGraph

val String.isStartDestination: Boolean
    get() = ReportsNavGraph.nestedNavGraphs.none { graph ->
        graph.startDestination.baseRoute == this
    }
