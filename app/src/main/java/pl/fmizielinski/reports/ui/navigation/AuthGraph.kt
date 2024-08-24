package pl.fmizielinski.reports.ui.navigation

import com.ramcosta.composedestinations.annotation.NavGraph
import com.ramcosta.composedestinations.annotation.RootGraph

@NavGraph<RootGraph>(
    route = "auth",
    start = true,
)
annotation class AuthGraph
