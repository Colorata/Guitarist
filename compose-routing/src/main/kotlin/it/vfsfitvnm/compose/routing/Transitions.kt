package it.vfsfitvnm.compose.routing

import androidx.compose.animation.*

@ExperimentalAnimationApi
val defaultStacking = ContentTransform(
    initialContentExit = scaleOut(targetScale = 0.9f) + fadeOut(),
    targetContentEnter = fadeIn(),
    targetContentZIndex = 1f
)

@ExperimentalAnimationApi
val defaultUnstacking = ContentTransform(
    initialContentExit = fadeOut(),
    targetContentEnter = EnterTransition.None,
    targetContentZIndex = 0f
)

@ExperimentalAnimationApi
val defaultStill = ContentTransform(
    initialContentExit = scaleOut(targetScale = 0.9f) + fadeOut(),
    targetContentEnter = fadeIn(),
    targetContentZIndex = 1f
)

@ExperimentalAnimationApi
val AnimatedContentTransitionScope<RouteHandlerScope>.isStacking: Boolean
    get() = initialState.route == null && targetState.route != null

@ExperimentalAnimationApi
val AnimatedContentTransitionScope<RouteHandlerScope>.isUnstacking: Boolean
    get() = initialState.route != null && targetState.route == null

@ExperimentalAnimationApi
val AnimatedContentTransitionScope<RouteHandlerScope>.isStill: Boolean
    get() = initialState.route == null && targetState.route == null

@ExperimentalAnimationApi
val AnimatedContentTransitionScope<RouteHandlerScope>.isUnknown: Boolean
    get() = initialState.route != null && targetState.route != null
