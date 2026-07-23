package com.example.mindquest.ui.navigation

sealed class Destination(val route: String) {
    data object Landing : Destination("landing")
    data object Activity : Destination("activity?tab={tab}") {
        const val ARG_TAB = "tab"
        const val BASE_ROUTE = "activity"
    }
    data object Statistics : Destination("statistics")
    data object Settings : Destination("settings")
}

fun activityRoute(tab: String? = null): String =
    if (tab != null) "${Destination.Activity.BASE_ROUTE}?tab=$tab" else Destination.Activity.BASE_ROUTE
