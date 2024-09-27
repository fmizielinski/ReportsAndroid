package pl.fmizielinski.reports.ui.navigation.graph

import com.ramcosta.composedestinations.annotation.NavGraph

@NavGraph<ReportsGraph>(
    route = "auth",
    start = true,
)
annotation class AuthGraph
