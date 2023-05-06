package it.vfsfitvnm.vimusic.ui.components.themed

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import it.vfsfitvnm.vimusic.ui.modifiers.pressable
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun BigIconButton(
    @DrawableRes id: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onPress: () -> Unit = {},
    onCancel: () -> Unit = {},
    backgroundColor: Color = LocalAppearance.current.colorPalette.background2,
    contentColor: Color = LocalAppearance.current.colorPalette.text,
    shape: Shape = RoundedCornerShape(32.dp)
) {
    Box(
        modifier
            .clip(shape)
            .pressable(onPress = {
                onPress()
            }, onCancel = {
                onCancel()
            }) {
                onClick()
            }
            .background(backgroundColor)
            .height(64.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id),
            contentDescription = null,
            Modifier.size(24.dp),
            colorFilter = ColorFilter.tint(contentColor),
        )
    }
}


/**
 * Button with fancy animated corner radius.
 * **WIP**, dirty solution.
 */
@Composable
private fun BigIconButton(
    @DrawableRes id: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = LocalAppearance.current.colorPalette.accent,
    contentColor: Color = LocalAppearance.current.colorPalette.text
) {
    val scope = rememberCoroutineScope()
    var isPressed by remember { mutableStateOf(false) }
    var finishedAnimating by remember { mutableStateOf(true) }
    AnimatedContent(isPressed, modifier.height(64.dp)
        .pressable(onPress = {
            isPressed = true
        }, onCancel = {
            scope.launch(Dispatchers.IO) {
                while (!finishedAnimating) continue
                isPressed = false
            }
        }) {
            scope.launch(Dispatchers.IO) {
                while (!finishedAnimating) continue
                isPressed = false
            }
            onClick()
        }, transitionSpec = {
        val duration = if (targetState) 0 else 500
        fadeIn(tween(0, duration)) togetherWith fadeOut(tween(duration))
    }) { pressed ->
        val animatedRoundness = remember { androidx.compose.animation.core.Animatable(32f) }
        LaunchedEffect(Unit) {
            if (pressed) {
                finishedAnimating = false
                animatedRoundness.animateTo(16f) {
                    if (abs(value - targetValue) < 4f) {
                        finishedAnimating = true
                    }
                }
                finishedAnimating = true
            }
        }
        val roundness = if (pressed) animatedRoundness.value else 32f
        Box(
            Modifier
                .clip(RoundedCornerShape(roundness.dp))
                .background(backgroundColor)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id),
                contentDescription = null,
                Modifier.size(24.dp),
                colorFilter = ColorFilter.tint(contentColor),
            )
        }
    }
}