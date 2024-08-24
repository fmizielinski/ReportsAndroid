package pl.fmizielinski.reports.ui.navigation.graph

import com.ramcosta.composedestinations.annotation.NavGraph
import com.ramcosta.composedestinations.annotation.RootGraph

@NavGraph<ReportsGraph>(
    route = "auth",
    start = true,
)
annotation class AuthGraph
