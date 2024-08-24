package pl.fmizielinski.reports.ui.navigation.graph

import com.ramcosta.composedestinations.animations.defaults.DefaultFadingTransitions
import com.ramcosta.composedestinations.annotation.NavHostGraph

@NavHostGraph(
    route = "reports",
    defaultTransitions = DefaultFadingTransitions::class,
)
annotation class ReportsGraph
