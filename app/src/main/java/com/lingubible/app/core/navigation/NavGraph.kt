package com.lingubible.app.core.navigation

import com.lingubible.app.core.theme.AppMotion

import androidx.compose.animation.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.layout.fillMaxSize
import androidx.navigation.NavBackStackEntry
import com.lingubible.app.ui.components.LocalNavAnimatedScope
import com.lingubible.app.ui.components.LocalSharedTransitionScope
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import com.lingubible.app.domain.model.parsePublicDetailLink
import com.lingubible.app.domain.model.parsePasswordResetLink
import com.lingubible.app.ui.viewmodels.PasswordRecoveryViewModel
import org.koin.androidx.compose.koinViewModel
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.util.Consumer
import com.lingubible.app.LocalComponentActivity
import com.lingubible.app.domain.model.parseTimetableShare
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import androidx.navigation.navDeepLink
import com.lingubible.app.ui.screens.*

private const val OPEN_INSTRUCTOR_REVIEWS = "openInstructorReviews"

// Courses/Instructors list <-> detail: crossfade the pages so the card's container transform carries the motion.
private fun AnimatedContentTransitionScope<NavBackStackEntry>.isContainerTransform(): Boolean {
    fun pair(a: NavBackStackEntry, b: NavBackStackEntry) =
        (a.destination.hasRoute<Screen.Courses>() && b.destination.hasRoute<Screen.CourseDetail>()) ||
            (a.destination.hasRoute<Screen.Instructors>() && b.destination.hasRoute<Screen.InstructorDetail>())
    return pair(initialState, targetState) || pair(targetState, initialState)
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun LingUBibleNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val activity = LocalComponentActivity.current
    val recovery: PasswordRecoveryViewModel = koinViewModel()
    fun consumeRecovery(incoming: android.content.Intent): Boolean {
        val link = incoming.dataString.orEmpty()
        if (incoming.action != android.content.Intent.ACTION_VIEW || parsePasswordResetLink(link) == null) return false
        // Never place reset secrets in navigation arguments, saved state or retained Activity intents.
        incoming.data = null
        incoming.replaceExtras(android.os.Bundle())
        recovery.openLink(link)
        navController.navigate(Screen.PasswordRecovery) { launchSingleTop = true }
        return true
    }
    fun consumePublicDetail(incoming: android.content.Intent): Boolean {
        if (incoming.action != android.content.Intent.ACTION_VIEW) return false
        val detail = parsePublicDetailLink(incoming.dataString.orEmpty()) ?: return false
        incoming.data = null
        incoming.replaceExtras(android.os.Bundle())
        val screen: Screen = if (detail.kind == "courses") Screen.CourseDetail(detail.value, detail.tab) else Screen.InstructorDetail(detail.value, detail.tab)
        navController.navigate(screen)
        return true
    }
    LaunchedEffect(activity, navController) { activity?.intent?.let { if (!consumeRecovery(it)) consumePublicDetail(it) } }
    DisposableEffect(activity, navController) {
        val listener = Consumer<android.content.Intent> { incoming ->
            if (consumeRecovery(incoming) || consumePublicDetail(incoming)) return@Consumer
            if (incoming.action == android.content.Intent.ACTION_VIEW && parseTimetableShare(incoming.dataString.orEmpty()).isSuccess) {
                navController.handleDeepLink(incoming)
            }
        }
        activity?.addOnNewIntentListener(listener)
        onDispose { activity?.removeOnNewIntentListener(listener) }
    }
    SharedTransitionLayout(modifier) { CompositionLocalProvider(LocalSharedTransitionScope provides this) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home,
        modifier = Modifier.fillMaxSize(),
        enterTransition = enter@{
            if (isContainerTransform()) return@enter fadeIn(animationSpec = AppMotion.effects())
            val fromIndex = BottomNavDestination.entries.indexOfFirst { initialState.destination.hasRoute(it.screen::class) }
            val toIndex = BottomNavDestination.entries.indexOfFirst { targetState.destination.hasRoute(it.screen::class) }
            if (fromIndex != -1 && toIndex != -1) {
                val direction = if (toIndex >= fromIndex) 1 else -1
                slideInHorizontally(
                    initialOffsetX = { (it * 0.25f * direction).toInt() },
                    animationSpec = AppMotion.page()
                ) + fadeIn(animationSpec = AppMotion.effects())
            } else {
                slideInHorizontally(
                    initialOffsetX = { (it * 0.3f).toInt() },
                    animationSpec = AppMotion.page()
                ) + fadeIn(animationSpec = AppMotion.effects())
            }
        },
        exitTransition = exit@{
            if (isContainerTransform()) return@exit fadeOut(animationSpec = AppMotion.effects())
            val fromIndex = BottomNavDestination.entries.indexOfFirst { initialState.destination.hasRoute(it.screen::class) }
            val toIndex = BottomNavDestination.entries.indexOfFirst { targetState.destination.hasRoute(it.screen::class) }
            if (fromIndex != -1 && toIndex != -1) {
                val direction = if (toIndex >= fromIndex) -1 else 1
                slideOutHorizontally(
                    targetOffsetX = { (it * 0.25f * direction).toInt() },
                    animationSpec = AppMotion.page()
                ) + fadeOut(animationSpec = AppMotion.effects())
            } else {
                slideOutHorizontally(
                    targetOffsetX = { (-it * 0.15f).toInt() },
                    animationSpec = AppMotion.page()
                ) + fadeOut(animationSpec = AppMotion.effects())
            }
        },
        popEnterTransition = {
            if (isContainerTransform()) fadeIn(animationSpec = AppMotion.effects()) else slideInHorizontally(
                initialOffsetX = { (-it * 0.2f).toInt() },
                animationSpec = AppMotion.page()
            ) + fadeIn(animationSpec = AppMotion.effects())
        },
        popExitTransition = {
            if (isContainerTransform()) fadeOut(animationSpec = AppMotion.effects()) else slideOutHorizontally(
                targetOffsetX = { (it * 0.3f).toInt() },
                animationSpec = AppMotion.page()
            ) + fadeOut(animationSpec = AppMotion.effects())
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
                onNavigateToPlanner = { navController.navigate(Screen.AcademicTools(initialTab = 0, fromHome = true)) },
                onNavigateToCalendar = { navController.navigate(Screen.AcademicTools(initialTab = 1, fromHome = true)) },
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

        composable<Screen.ImportTimetable>(deepLinks = listOf(
            navDeepLink { uriPattern = "https://www.lingubible.com/planner?term={term}&sections={sections}" },
            navDeepLink { uriPattern = "https://lingubible.com/planner?term={term}&sections={sections}" }
        )) { entry ->
            val share = entry.toRoute<Screen.ImportTimetable>()
            PlannerScreen(initialShareUrl = "https://www.lingubible.com/planner?term=${android.net.Uri.encode(share.term)}&sections=${android.net.Uri.encode(share.sections)}")
        }

        composable<Screen.Calendar> {
            AcademicToolsScreen(initialTab = 1)
        }

        composable<Screen.GpaHons> {
            AcademicToolsScreen(initialTab = 2)
        }

        composable<Screen.Courses> {
            CompositionLocalProvider(LocalNavAnimatedScope provides this) {
            CoursesScreen(
                onNavigateToCourse = { code -> navController.navigate(Screen.CourseDetail(code)) }
            ) }
        }

        composable<Screen.CourseDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<Screen.CourseDetail>()
            CompositionLocalProvider(LocalNavAnimatedScope provides this) {
            CourseDetailScreen(
                courseCode = route.courseCode,
                initialTab = route.initialTab,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToWriteReview = { navController.navigate(Screen.WriteReview(route.courseCode)) },
                onNavigateToInstructor = { name ->
                    navController.navigate(Screen.InstructorDetail(name))
                    navController.currentBackStackEntry?.savedStateHandle?.set(OPEN_INSTRUCTOR_REVIEWS, true)
                },
                onNavigateToAuth = { navController.navigate(Screen.Auth("login")) }
            ) }
        }

        composable<Screen.Instructors> {
            CompositionLocalProvider(LocalNavAnimatedScope provides this) {
            InstructorsScreen(
                onNavigateToInstructor = { name -> navController.navigate(Screen.InstructorDetail(name)) }
            ) }
        }

        composable<Screen.InstructorDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<Screen.InstructorDetail>()
            // Keep the existing route identity; save the requested section with this entry.
            val showReviews by backStackEntry.savedStateHandle.getStateFlow(OPEN_INSTRUCTOR_REVIEWS, false).collectAsStateWithLifecycle()
            CompositionLocalProvider(LocalNavAnimatedScope provides this) {
            InstructorDetailScreen(
                instructorName = route.name,
                initialTab = route.initialTab,
                onCourse = { navController.navigate(Screen.CourseDetail(it)) },
                showReviews = showReviews,
                onNavigateBack = { navController.popBackStack() }
            ) }
        }

        composable<Screen.Reviews> {
            ReviewsScreen(
                onNavigateToCourse = { code -> navController.navigate(Screen.CourseDetail(code)) },
                onNavigateToWriteReview = { code -> navController.navigate(Screen.WriteReview(code)) }
            )
        }

        composable<Screen.Favorites> {
            FavoritesScreen(
                onCourse = { navController.navigate(Screen.CourseDetail(it)) },
                onInstructor = { navController.navigate(Screen.InstructorDetail(it)) },
                onSignIn = { navController.navigate(Screen.Auth("login")) }
            )
        }

        composable<Screen.MyReviews> {
            ReviewsScreen(
                myReviewsOnly = true,
                onNavigateToCourse = { code -> navController.navigate(Screen.CourseDetail(code)) },
                onNavigateToWriteReview = { code -> navController.navigate(Screen.WriteReview(code)) },
                onNavigateToAuth = { navController.navigate(Screen.Auth("login")) }
            )
        }

        composable<Screen.WriteReview> { backStackEntry ->
            val route = backStackEntry.toRoute<Screen.WriteReview>()
            WriteReviewScreen(
                initialCourseCode = route.courseCode ?: "",
                onNavigateToAuth = { navController.navigate(Screen.Auth("login")) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Screen.Auth> { backStackEntry ->
            val route = backStackEntry.toRoute<Screen.Auth>()
            AuthScreen(
                initialMode = route.mode,
                onForgotPassword = { recovery.newRequest(); navController.navigate(Screen.PasswordRecovery) },
                onAuthSuccess = { navController.popBackStack() },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Screen.Profile> {
            ProfileScreen(
                onNavigateToAuth = { navController.navigate(Screen.Auth("login")) },
                onNavigateToReviews = { navController.navigate(Screen.MyReviews) },
                onNavigateToFavorites = { navController.navigate(Screen.Favorites) },
                onNavigateToSettings = { navController.navigate(Screen.Settings) },
                onNavigateToGuide = { navController.navigate(Screen.Guide) },
                onNavigateToInformation = { navController.navigate(Screen.Information(it)) }
            )
        }

        composable<Screen.PasswordRecovery> {
            PasswordRecoveryScreen(recovery, onSignIn = {
                if (!navController.popBackStack<Screen.Auth>(inclusive = false)) {
                    navController.navigate(Screen.Auth("login")) { popUpTo<Screen.PasswordRecovery> { inclusive = true }; launchSingleTop = true }
                }
            }, onInfo = { navController.navigate(Screen.Information(it)) })
        }

        composable<Screen.Information> { entry ->
            InformationScreen(entry.toRoute<Screen.Information>().page, onNavigate = { navController.navigate(Screen.Information(it)) })
        }

        composable<Screen.Guide> {
            GuideScreen(onClose = { navController.popBackStack() }, onNavigate = { navController.navigate(it) })
        }

        composable<Screen.Settings> {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToGuide = { navController.navigate(Screen.Guide) },
                onNavigateToInformation = { navController.navigate(Screen.Information(it)) }
            )
        }
    }
    } }
}
