package com.lingubible.app.ui.screens

import com.lingubible.app.ui.components.AppButtonDefaults
import com.lingubible.app.ui.components.FadingLazyColumn
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lingubible.app.core.navigation.screenContentPadding
import com.lingubible.app.data.repository.AppwriteFavoritesRepository
import com.lingubible.app.domain.model.Course
import com.lingubible.app.domain.model.Instructor
import com.lingubible.app.domain.repository.CourseRepository
import com.lingubible.app.domain.repository.InstructorRepository
import com.lingubible.app.ui.common.hapticClick
import com.lingubible.app.ui.components.PopularCourseCard
import com.lingubible.app.ui.components.PopularInstructorCard
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun FavoritesScreen(onCourse: (String) -> Unit, onInstructor: (String) -> Unit, onSignIn: () -> Unit) {
    val repository = koinInject<AppwriteFavoritesRepository>()
    val courseRepository = koinInject<CourseRepository>()
    val instructorRepository = koinInject<InstructorRepository>()
    val state by repository.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var courses by remember { mutableStateOf(emptyList<Course>()) }
    var instructors by remember { mutableStateOf(emptyList<Instructor>()) }
    var unavailable by remember { mutableStateOf(emptySet<com.lingubible.app.data.repository.FavoriteKey>()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var refresh by remember { mutableIntStateOf(0) }
    LaunchedEffect(state.userId, state.items, refresh) {
        courses = emptyList(); instructors = emptyList(); unavailable = emptySet(); error = null
        if (state.userId == null) return@LaunchedEffect
        loading = true
        try {
            courses = state.items.keys.filter { it.type == "course" }.mapNotNull { key ->
                courseRepository.getCourseByCode(key.itemId).onFailure { unavailable = unavailable + key }.getOrNull()
            }.sortedBy { it.code }
            instructors = state.items.keys.filter { it.type == "instructor" }.mapNotNull { key ->
                instructorRepository.getInstructorByName(key.itemId).onFailure { unavailable = unavailable + key }.getOrNull()
            }.sortedBy { it.name }
        } catch (e: kotlinx.coroutines.CancellationException) { throw e
        } catch (e: Exception) { error = e.localizedMessage
        } finally { loading = false }
    }
    FadingLazyColumn(contentPadding = screenContentPadding(start = 16.dp, end = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            if (state.userId == null) {
                Text("登入後即可儲存與同步收藏 · Sign in to save and sync favourites")
                Button(shapes = AppButtonDefaults.shapes(), onClick = hapticClick(onSignIn)) { Text("登入 Sign in") }
            } else {
                TextButton(shapes = AppButtonDefaults.shapes(), onClick = hapticClick { scope.launch { repository.refresh(); refresh++ } }, enabled = !state.loading && !loading) {
                    Text("重新整理 Refresh")
                }
            }
        }
        if (state.loading || loading) item { CircularProgressIndicator() }
        if (error != null || state.error != null) item { Text(error ?: state.error.orEmpty(), color = MaterialTheme.colorScheme.error) }
        if (state.userId != null && !loading && !state.loading && state.items.isEmpty() && state.error == null) item {
            Text("尚未收藏課程或講師 · No saved courses or instructors")
        }
        itemsIndexed(unavailable.toList(), key = { _, it -> "unavailable:${it.type}:${it.itemId}" }) { _, key ->
            Card(shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(12.dp)) {
                    Text("${key.itemId} · 暫時無法載入 Unavailable")
                    TextButton(shapes = AppButtonDefaults.shapes(), onClick = hapticClick { scope.launch {
                        repository.toggle(key.type, key.itemId).onFailure { error = it.localizedMessage }
                    } }) { Text("移除收藏 Remove favourite") }
                }
            }
        }
        if (courses.isNotEmpty()) item { Text("課程 Courses", style = MaterialTheme.typography.titleMedium) }
        itemsIndexed(courses, key = { _, it -> "course:${it.code}" }) { index, course ->
            PopularCourseCard(course, onClick = { onCourse(course.code) }, groupIndex = index, groupSize = courses.size)
        }
        if (instructors.isNotEmpty()) item { Text("講師 Instructors", style = MaterialTheme.typography.titleMedium) }
        itemsIndexed(instructors, key = { _, it -> "instructor:${it.name}" }) { index, instructor ->
            PopularInstructorCard(instructor, onClick = { onInstructor(instructor.name) }, groupIndex = index, groupSize = instructors.size)
        }
    }
}
