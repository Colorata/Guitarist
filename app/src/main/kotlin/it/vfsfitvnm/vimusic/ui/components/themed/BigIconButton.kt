package it.vfsfitvnm.vimusic.ui.components.themed

import androidx.annotation.DrawableRes
import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun BigIconButton(
    @DrawableRes id: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = LocalAppearance.current.colorPalette.background2,
    contentColor: Color = LocalAppearance.current.colorPalette.text
) {
    var isPressed by remember { mutableStateOf(false) }

    val icon = remember {
        movableContentOf {
            Image(
                painter = painterResource(id),
                contentDescription = null,
                Modifier.size(24.dp),
                colorFilter = ColorFilter.tint(contentColor),
            )
        }
    }

    AnimatedContent(isPressed, modifier.height(64.dp), transitionSpec = {
        val duration = if (isPressed) 0 else 300
        val animationSpec = tween<Float>(duration)
        fadeIn(animationSpec) with fadeOut(animationSpec)
    }) { pressed ->
        val roundness by animateDpAsState(if (pressed) 8.dp else 32.dp)
        Box(
            Modifier
                .clip(RoundedCornerShape(roundness))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            println("Tap: start")
                            isPressed = true
                            awaitRelease()
                        }
                    )
                }
                .clickable {
                    isPressed = false
                    onClick()
                }
                .background(backgroundColor)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
    }
}