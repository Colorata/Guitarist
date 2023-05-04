package it.vfsfitvnm.vimusic.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.roundToLong
import kotlin.math.sin


@Composable
fun SeekBar(
    position: Long,
    onSeek: (updated: Long) -> Unit,
    modifier: Modifier = Modifier,
    onSeekStarted: (updated: Long) -> Unit = {},
    onSeekFinished: () -> Unit = {},
    color: Color,
    backgroundColor: Color = Color.Transparent,
    range: ClosedRange<Long> = 0..100L,
    isActive: Boolean = true,
    scrubberRadius: Dp = 6.dp,
    shape: Shape = RectangleShape,
) {
    val minimumValue = range.start
    val maximumValue = range.endInclusive
    val isDragging = remember {
        MutableTransitionState(false)
    }

    val transition = updateTransition(transitionState = isDragging, label = null)

    val currentAmplitude by transition.animateDp(label = "") { if (it || !isActive) 0.dp else 2.dp }
    println(currentAmplitude)
    val currentScrubberRadius by transition.animateDp(label = "") { if (it || isActive) 0.dp else scrubberRadius }

    Box(modifier = modifier.pointerInput(minimumValue, maximumValue) {
        if (maximumValue < minimumValue) return@pointerInput

        detectDrags(isDragging, maximumValue, minimumValue, onSeek, position, onSeekFinished)
    }.pointerInput(minimumValue, maximumValue) {
        detectTaps(maximumValue, minimumValue, onSeekStarted, onSeekFinished)
    }.padding(horizontal = scrubberRadius).drawWithContent {
        drawContent()
        drawScrubber(range, position, color, currentScrubberRadius)
    }
    ) {
        SeekBarContent(
            backgroundColor, amplitude = { currentAmplitude }, position, minimumValue, maximumValue, shape, color
        )
    }
}

private suspend fun PointerInputScope.detectDrags(
    isDragging: MutableTransitionState<Boolean>,
    maximumValue: Long,
    minimumValue: Long,
    onSeek: (delta: Long) -> Unit,
    position: Long,
    onSeekFinished: () -> Unit
) {
    var acc = 0f

    detectHorizontalDragGestures(onDragStart = {
        isDragging.targetState = true
    }, onHorizontalDrag = { _, delta ->
        acc += delta / size.width * (maximumValue - minimumValue)

        if (acc !in -1f..1f) {
            onSeek(acc.toLong())
            acc -= acc.toLong()
        }
    }, onDragEnd = {
        isDragging.targetState = false
        acc = 0f
        onSeekFinished()
    }, onDragCancel = {
        isDragging.targetState = false
        acc = 0f

        onSeekFinished()
    })
}

private suspend fun PointerInputScope.detectTaps(
    maximumValue: Long, minimumValue: Long, onSeekStarted: (updated: Long) -> Unit, onSeekFinished: () -> Unit
) {
    if (maximumValue < minimumValue) return

    detectTapGestures(onPress = { offset ->
        val updatedOffset = (offset.x / size.width * (maximumValue - minimumValue) + minimumValue).roundToLong()

        onSeekStarted(updatedOffset)
    }, onTap = {
        onSeekFinished()
    })
}

private fun ContentDrawScope.drawScrubber(
    range: ClosedRange<Long>, position: Long, color: Color, radius: Dp
) {
    val minimumValue = range.start
    val maximumValue = range.endInclusive
    val scrubberPosition = if (maximumValue < minimumValue) {
        0f
    } else {
        (position.toFloat() - minimumValue) / (maximumValue - minimumValue) * size.width
    }

    drawRoundRect(
        color, topLeft = Offset(scrubberPosition - 5f, (size.height - 50f) / 2),
        size = Size(10f, 50f),
        cornerRadius = CornerRadius(radius.toPx())
    )
}


@Composable
private fun SeekBarContent(
    backgroundColor: Color,
    amplitude: () -> Dp,
    position: Long,
    minimumValue: Long,
    maximumValue: Long,
    shape: Shape,
    color: Color
) {
    val fraction = (position.toFloat() - minimumValue) / (maximumValue - minimumValue)
    val progress by rememberInfiniteTransition().animateFloat(
        0f,
        1f,
        animationSpec = infiniteRepeatable(
            tween(2000, easing = LinearEasing)
        )
    )
    Box(Modifier.fillMaxWidth().height(6.dp)) {
        Spacer(
            modifier = Modifier.fillMaxHeight().fillMaxWidth(1f - fraction)
                .background(color = backgroundColor, shape = shape)
                .align(Alignment.CenterEnd)
        )

        Canvas(Modifier.fillMaxWidth(fraction).height(amplitude()).align(Alignment.CenterStart)) {
            drawPath(
                wavePath(size.copy(height = size.height * 2), progress),
                color,
                style = Stroke(width = 5f)
            )
        }
    }
}

private fun wavePath(size: Size, progress: Float): Path {
    fun yFromX(x: Float) = (sin(x / 15f + progress * 2 * PI.toFloat()) + 1) * size.height / 2
    return Path().apply {
        moveTo(0f, yFromX(0f))
        var currentX = 0f
        while (currentX < size.width) {
            lineTo(currentX, yFromX(currentX))
            currentX += 1
        }
    }
}