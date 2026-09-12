package com.lingubible.app.core.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.lingubible.app.ui.screens.*

@Composable
fun LingUBibleNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home,
        modifier = modifier,
        enterTransition = {
            val fromIndex = BottomNavDestination.entries.indexOfFirst { initialState.destination.hasRoute(it.screen::class) }
            val toIndex = BottomNavDestination.entries.indexOfFirst { targetState.destination.hasRoute(it.screen::class) }
            if (fromIndex != -1 && toIndex != -1) {
                val direction = if (toIndex >= fromIndex) 1 else -1
                slideInHorizontally(
                    initialOffsetX = { (it * 0.25f * direction).toInt() },
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                ) + fadeIn(animationSpec = tween(240))
            } else {
                slideInHorizontally(
                    initialOffsetX = { (it * 0.3f).toInt() },
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                ) + fadeIn(animationSpec = tween(240))
            }
        },
        exitTransition = {
            val fromIndex = BottomNavDestination.entries.indexOfFirst { initialState.destination.hasRoute(it.screen::class) }
            val toIndex = BottomNavDestination.entries.indexOfFirst { targetState.destination.hasRoute(it.screen::class) }
            if (fromIndex != -1 && toIndex != -1) {
                val direction = if (toIndex >= fromIndex) -1 else 1
                slideOutHorizontally(
                    targetOffsetX = { (it * 0.25f * direction).toInt() },
                    animationSpec = tween(190, easing = FastOutLinearInEasing)
                ) + fadeOut(animationSpec = tween(170))
            } else {
                slideOutHorizontally(
                    targetOffsetX = { (-it * 0.15f).toInt() },
                    animationSpec = tween(190, easing = FastOutLinearInEasing)
                ) + fadeOut(animationSpec = tween(170))
            }
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { (-it * 0.2f).toInt() },
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
            ) + fadeIn(animationSpec = tween(240))
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { (it * 0.3f).toInt() },
                animationSpec = tween(190, easing = FastOutLinearInEasing)
            ) + fadeOut(animationSpec = tween(170))
        }
    ) {
        composable<Screen.Home> {
            HomeScreen(
                onNavigateToCourse = { code -> 
                    if (code.isNotBlank()) navController.navigate(Screen.CourseDetail(code))
                    else navController.navigate(Screen.Courses)
                },
                onNavigateToInstructor = { name -> 
                    if (name.isNotBlank()) navController.navigate(Screen.InstructorDetail(name))
                    else navController.navigate(Screen.Instructors)
                },
                onNavigateToWriteReview = { code -> navController.navigate(Screen.WriteReview(code)) },
                onNavigateToPlanner = { navController.navigate(Screen.AcademicTools(0)) },
                onNavigateToReviews = { navController.navigate(Screen.Reviews) }
            )
        }

        composable<Screen.AcademicTools> { backStackEntry ->
            val route = backStackEntry.toRoute<Screen.AcademicTools>()
            AcademicToolsScreen(initialTab = route.initialTab)
        }

        composable<Screen.Planner> {
            AcademicToolsScreen(initialTab = 0)
        }

        composable<Screen.Calendar> {
            AcademicToolsScreen(initialTab = 1)
        }

        composable<Screen.GpaHons> {
            AcademicToolsScreen(initialTab = 2)
        }

        composable<Screen.Courses> {
            CoursesScreen(
                onNavigateToCourse = { code -> navController.navigate(Screen.CourseDetail(code)) }
            )
        }

        composable<Screen.CourseDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<Screen.CourseDetail>()
            CourseDetailScreen(
                courseCode = route.courseCode,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToWriteReview = { navController.navigate(Screen.WriteReview(route.courseCode)) }
            )
        }

        composable<Screen.Instructors> {
            InstructorsScreen(
                onNavigateToInstructor = { name -> navController.navigate(Screen.InstructorDetail(name)) }
            )
        }

        composable<Screen.InstructorDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<Screen.InstructorDetail>()
            InstructorDetailScreen(
                instructorName = route.name,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Screen.Reviews> {
            ReviewsScreen(
                onNavigateToCourse = { code -> navController.navigate(Screen.CourseDetail(code)) },
                onNavigateToWriteReview = { code -> navController.navigate(Screen.WriteReview(code)) }
            )
        }

        composable<Screen.WriteReview> { backStackEntry ->
            val route = backStackEntry.toRoute<Screen.WriteReview>()
            WriteReviewScreen(
                initialCourseCode = route.courseCode ?: "",
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Screen.Auth> { backStackEntry ->
            val route = backStackEntry.toRoute<Screen.Auth>()
            AuthScreen(
                initialMode = route.mode,
                onAuthSuccess = { navController.popBackStack() },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Screen.Profile> {
            ProfileScreen(
                onNavigateToAuth = { navController.navigate(Screen.Auth("login")) },
                onNavigateToReviews = { navController.navigate(Screen.Reviews) },
                onNavigateToSettings = { navController.navigate(Screen.Settings) }
            )
        }

        composable<Screen.Settings> {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
