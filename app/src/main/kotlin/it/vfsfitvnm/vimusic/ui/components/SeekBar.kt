package it.vfsfitvnm.vimusic.ui.components

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.updateTransition
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.roundToLong


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
    barHeight: Dp = 3.dp,
    scrubberColor: Color = color,
    scrubberRadius: Dp = 6.dp,
    shape: Shape = RectangleShape,
    drawSteps: Boolean = false,
) {
    val minimumValue = range.start
    val maximumValue = range.endInclusive
    val isDragging = remember {
        MutableTransitionState(false)
    }

    val transition = updateTransition(transitionState = isDragging, label = null)

    val currentBarHeight by transition.animateDp(label = "") { if (it) scrubberRadius else barHeight }
    val currentScrubberRadius by transition.animateDp(label = "") { if (it) 0.dp else scrubberRadius }

    Box(modifier = modifier.pointerInput(minimumValue, maximumValue) {
        if (maximumValue < minimumValue) return@pointerInput

        var acc = 0f

        detectHorizontalDragGestures(onDragStart = {
            isDragging.targetState = true
        }, onHorizontalDrag = { _, delta ->
            acc += delta / size.width * (maximumValue - minimumValue)

            if (acc !in -1f..1f) {
                onSeek(position + acc.toLong())
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
    }.pointerInput(minimumValue, maximumValue) {
        if (maximumValue < minimumValue) return@pointerInput

        detectTapGestures(onPress = { offset ->
            val updatedOffset = (offset.x / size.width * (maximumValue - minimumValue) + minimumValue).roundToLong()

            onSeekStarted(updatedOffset)
        }, onTap = {
            onSeekFinished()
        })
    }.padding(horizontal = scrubberRadius).drawWithContent {
        drawContent()

        val scrubberPosition = if (maximumValue < minimumValue) {
            0f
        } else {
            (position.toFloat() - minimumValue) / (maximumValue - minimumValue) * size.width
        }

        drawCircle(
            color = scrubberColor,
            radius = currentScrubberRadius.toPx(),
            center = center.copy(x = scrubberPosition)
        )

        if (drawSteps) {
            for (i in position + 1..maximumValue) {
                val stepPosition = (i.toFloat() - minimumValue) / (maximumValue - minimumValue) * size.width
                drawCircle(
                    color = scrubberColor,
                    radius = scrubberRadius.toPx() / 2,
                    center = center.copy(x = stepPosition),
                )
            }
        }
    }.height(scrubberRadius)) {
        Spacer(
            modifier = Modifier.height(currentBarHeight).fillMaxWidth()
                .background(color = backgroundColor, shape = shape).align(Alignment.Center)
        )

        Spacer(
            modifier = Modifier.height(currentBarHeight)
                .fillMaxWidth((position.toFloat() - minimumValue) / (maximumValue - minimumValue))
                .background(color = color, shape = shape).align(Alignment.CenterStart)
        )
    }
}