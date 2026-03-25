package com.familyfinance.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.familyfinance.ui.dashboard.DashboardScreen
import com.familyfinance.ui.settings.AccountManageScreen
import com.familyfinance.ui.settings.CategoryManageScreen
import com.familyfinance.ui.settings.ProjectManageScreen
import com.familyfinance.ui.settings.SettingsScreen
import com.familyfinance.ui.settings.SettingsViewModel
import com.familyfinance.ui.entry.FastEntrySheet
import com.familyfinance.ui.timeline.TimelineScreen
import com.familyfinance.ui.reconcile.ReconciliationScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToFastEntry = { navController.navigate(Screen.FastEntry.route) },
                onNavigateToTimeline = { navController.navigate(Screen.Timeline.route) },
                onNavigateToReconcile = { id -> navController.navigate(Screen.Reconcile.createRoute(id)) }
            )
        }
        composable(
            route = Screen.Reconcile.route,
            arguments = listOf(
                androidx.navigation.navArgument("accountId") { type = androidx.navigation.NavType.LongType }
            )
        ) {
            ReconciliationScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Timeline.route) {
            TimelineScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.FastEntry.route) {
            FastEntrySheet(
                onBack = { navController.popBackStack() }
            )
        }
        
        // Settings Group
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateToAccounts = { navController.navigate(Screen.SettingsAccounts.route) },
                onNavigateToCategories = { navController.navigate(Screen.SettingsCategories.route) },
                onNavigateToProjects = { navController.navigate(Screen.SettingsProjects.route) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.SettingsAccounts.route) {
            AccountManageScreen(
                viewModel = hiltViewModel(),
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.SettingsCategories.route) {
            CategoryManageScreen(
                viewModel = hiltViewModel(),
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.SettingsProjects.route) {
            ProjectManageScreen(
                viewModel = hiltViewModel(),
                onBack = { navController.popBackStack() }
            )
        }
    }
}
