package com.lingubible.app.di

import android.content.Context
import com.lingubible.app.data.remote.AppwriteClientProvider
import com.lingubible.app.data.repository.*
import com.lingubible.app.domain.repository.*
import com.lingubible.app.ui.common.MainViewModel
import com.lingubible.app.ui.viewmodels.*
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

data class AppConfig(
    val endpoint: String = "https://appwrite.lingubible.com/v1",
    val projectId: String = "6a1097400037a55f6472",
    val databaseId: String = "lingubible"
)

val appModule = module {
    single { AppConfig() }
    single { AppwriteClientProvider(context = androidContext(), appConfig = get()) }

    single { SupportRepository(get()) }
    viewModel { PasswordRecoveryViewModel(get()) }
    viewModel { ContactViewModel(get()) }

    single<AuthRepository> { AppwriteAuthRepository(clientProvider = get()) }
    single { AppwriteFavoritesRepository(get(), get()) }
    single<AvatarRepository> { AppwriteAvatarRepository(context = androidContext(), clientProvider = get()) }
    single<CourseRepository> { AppwriteCourseRepository(clientProvider = get()) }
    single<InstructorRepository> { AppwriteInstructorRepository(clientProvider = get()) }
    single<ReviewRepository> { AppwriteReviewRepository(clientProvider = get(), authRepository = get(), courseRepository = get()) }
    single<MaterialRepository> { AppwriteMaterialRepository(clientProvider = get()) }
    single<StatsRepository> { AppwriteStatsRepository(clientProvider = get()) }
    single { TimetableRepository(androidContext()) }
    single { AcademicCalendarRepository(androidContext()) }
    single { com.lingubible.app.core.settings.AppSettingsManager(androidContext()) }

    viewModel { MainViewModel(get()) }
    viewModel { HomeViewModel(get()) }
    viewModel { AuthViewModel(get(), get()) }
    viewModel { CoursesViewModel(get(), get(), get()) }
    viewModel { CourseDetailViewModel(get(), get(), get(), get(), get()) }
    viewModel { InstructorsViewModel(get(), get()) }
    viewModel { params -> ReviewsViewModel(get(), get(), params.getOrNull<Boolean>() ?: false, params.getOrNull<Int>()) }
    single { ReviewDraftStore(androidContext().getSharedPreferences("review_drafts", Context.MODE_PRIVATE)) }
    viewModel { WriteReviewViewModel(get(), get(), get(), get()) }
    viewModel { TimetableViewModel(get(), get()) }
    viewModel { AcademicCalendarViewModel(get()) }
    viewModel { GpaHonsViewModel(androidContext(), get()) }
}
