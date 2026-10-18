package com.lingubible.app.core.theme

import androidx.compose.material3.MotionScheme

/** The same Expressive spring tokens drive native components and custom transitions. */
object AppMotion {
    val scheme = MotionScheme.expressive()

    fun <T> page() = scheme.slowSpatialSpec<T>()
    fun <T> spatial() = scheme.defaultSpatialSpec<T>()
    fun <T> press() = scheme.fastSpatialSpec<T>()
    fun <T> effects() = scheme.defaultEffectsSpec<T>()
}
