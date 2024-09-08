package pl.fmizielinski.reports.ui.navigation

import com.ramcosta.composedestinations.generated.navgraphs.ReportsNavGraph
import com.ramcosta.composedestinations.utils.startDestination

val String.isStartDestination: Boolean
    get() = ReportsNavGraph.nestedNavGraphs.none { graph ->
        graph.startDestination.baseRoute == this
    }
