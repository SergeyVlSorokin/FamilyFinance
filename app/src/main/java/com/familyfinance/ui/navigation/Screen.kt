package com.familyfinance.ui.navigation

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Timeline : Screen("timeline")
    object FastEntry : Screen("fast_entry")
    object Settings : Screen("settings")
    object SettingsAccounts : Screen("settings_accounts")
    object SettingsCategories : Screen("settings_categories")
    object SettingsProjects : Screen("settings_projects")
    object Reconcile : Screen("reconcile/{accountId}") {
        fun createRoute(accountId: Long) = "reconcile/$accountId"
    }
}
