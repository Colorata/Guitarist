package it.vfsfitvnm.vimusic.ui.screens.player

import androidx.annotation.DrawableRes
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.media3.common.C
import androidx.media3.common.Player
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.models.ui.UiMedia
import it.vfsfitvnm.vimusic.ui.components.SeekBar
import it.vfsfitvnm.vimusic.ui.components.themed.BigIconButton
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.utils.*
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

private const val FORWARD_BACKWARD_OFFSET = 16f

@Composable
fun Controls(
    media: UiMedia, shouldBePlaying: Boolean, position: Long, modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val colorPalette = LocalAppearance.current.colorPalette

    val binder = LocalPlayerServiceBinder.current
    binder?.player ?: return

    val compositionLaunched = isCompositionLaunched()
    var trackLoopEnabled by rememberPreference(trackLoopEnabledKey, defaultValue = false)

    val animatedPosition = remember { Animatable(position.toFloat()) }
    var isSeeking by remember { mutableStateOf(false) }

    LaunchedEffect(media) {
        if (compositionLaunched) animatedPosition.animateTo(0f)
    }
    LaunchedEffect(position) {
        if (!isSeeking && !animatedPosition.isRunning)
            animatedPosition.animateTo(position.toFloat())
    }
    val durationVisible by remember(isSeeking) { derivedStateOf { isSeeking } }

    var likedAt by rememberSaveable {
        mutableStateOf<Long?>(null)
    }

    LaunchedEffect(media.id) {
        Database.likedAt(media.id).distinctUntilChanged().collect { likedAt = it }
    }

    val shouldBePlayingTransition = updateTransition(shouldBePlaying, label = "shouldBePlaying")

    val controlHeight = 64.dp

    val playButtonRadius by shouldBePlayingTransition.animateDp(transitionSpec = {
        tween(
            durationMillis = 100,
            easing = LinearEasing
        )
    },
        label = "playPauseRoundness",
        targetValueByState = { if (it) 32.dp else 16.dp })

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth().padding(horizontal = 32.dp)
    ) {
        Spacer(
            modifier = Modifier.weight(1f)
        )

        MediaInfo(media)

        Spacer(
            modifier = Modifier.weight(1f)
        )

        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PlayButton(
                { playButtonRadius },
                shouldBePlaying,
                Modifier.height(controlHeight).weight(4f)
            )
            SkipButton(
                R.drawable.play_skip_forward,
                onClick = binder.player::forceSeekToNext,
                Modifier.weight(1f)
            )
        }
        Spacer(Modifier.weight(1f))
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            SkipButton(
                R.drawable.play_skip_back,
                onClick = binder.player::forceSeekToPrevious,
                Modifier.weight(1f),
                offsetOnPress = -FORWARD_BACKWARD_OFFSET
            )


            Column(Modifier.weight(4f)) {
                SeekBar(
                    position = { animatedPosition.value },
                    range = 0f..media.duration.toFloat(),
                    onSeekStarted = {
                        isSeeking = true
                        scope.launch {
                            animatedPosition.animateTo(it)
                        }
                    },
                    onSeek = { delta ->
                        if (media.duration != C.TIME_UNSET) {
                            isSeeking = true
                            scope.launch {
                                animatedPosition.snapTo(
                                    animatedPosition.value.plus(delta).coerceIn(0f, media.duration.toFloat())
                                )
                            }
                        }
                    },
                    onSeekFinished = {
                        isSeeking = false
                        animatedPosition.let {
                            binder.player.seekTo(it.targetValue.toLong())
                        }
                    },
                    color = colorPalette.text,
                    isActive = binder.player.isPlaying,
                    backgroundColor = colorPalette.background2,
                    shape = RoundedCornerShape(8.dp)
                )
                AnimatedVisibility(
                    durationVisible,
                    enter = fadeIn() + expandVertically { -it },
                    exit = fadeOut() + shrinkVertically { -it }) {
                    Column {
                        Spacer(Modifier.height(8.dp))
                        Duration(animatedPosition.value, media.duration)
                    }
                }
            }

//            BigIconButton(
//                if (likedAt == null) R.drawable.heart_outline else R.drawable.heart,
//                onClick = {
//                    val currentMediaItem = binder.player.currentMediaItem
//                    query {
//                        if (Database.like(
//                                media.id, if (likedAt == null) System.currentTimeMillis() else null
//                            ) == 0
//                        ) {
//                            currentMediaItem?.takeIf { it.mediaId == media.id }?.let {
//                                Database.insert(currentMediaItem, Song::toggleLike)
//                            }
//                        }
//                    }
//                },
//                Modifier.weight(1f),
//                backgroundColor = if (likedAt == null) colorPalette.background2 else colorPalette.accent
//            )
//
//            BigIconButton(
//                R.drawable.infinite,
//                onClick = { trackLoopEnabled = !trackLoopEnabled },
//                Modifier.weight(1f),
//                contentColor = if (trackLoopEnabled) colorPalette.text else colorPalette.textDisabled
//            )
        }

        Spacer(
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SkipButton(
    @DrawableRes id: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    offsetOnPress: Float = FORWARD_BACKWARD_OFFSET
) {
    val scope = rememberCoroutineScope()
    val offsetDp = remember { Animatable(0f) }
    val density = LocalDensity.current
    BigIconButton(
        id,
        onClick = {
            onClick()
            scope.launch {
                offsetDp.animateTo(offsetOnPress)
            }
        },
        modifier.graphicsLayer {
            with(density) {
                translationX = offsetDp.value.dp.toPx()
            }
        },
        onPress = {
            scope.launch {
                offsetDp.animateTo(offsetOnPress)
            }
        },
        onCancel = {
            scope.launch {
                offsetDp.animateTo(0f)
            }
        }
    )
}

@Composable
private fun PlayButton(
    playButtonRadius: () -> Dp,
    shouldBePlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val colorPalette = LocalAppearance.current.colorPalette
    val binder = LocalPlayerServiceBinder.current
    Box(modifier = modifier.clip(RoundedCornerShape(playButtonRadius())).clickable {
        if (shouldBePlaying) {
            binder?.player?.pause()
        } else {
            if (binder?.player?.playbackState == Player.STATE_IDLE) {
                binder.player.prepare()
            }
            binder?.player?.play()
        }
    }.background(colorPalette.accent)) {
        Image(
            painter = painterResource(if (shouldBePlaying) R.drawable.pause else R.drawable.play),
            contentDescription = null,
            colorFilter = ColorFilter.tint(colorPalette.text),
            modifier = Modifier.align(Alignment.Center).size(28.dp)
        )
    }
}

@Composable
private fun Duration(
    position: Float,
    duration: Long,
) {
    val typography = LocalAppearance.current.typography
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        BasicText(
            text = formatAsDuration(position.toLong()),
            style = typography.xxs.semiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        if (duration != C.TIME_UNSET) {
            BasicText(
                text = formatAsDuration(duration),
                style = typography.xxs.semiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun MediaInfo(
    media: UiMedia
) {
    val typography = LocalAppearance.current.typography
    BasicText(
        text = media.title, style = typography.l.bold, maxLines = 1, overflow = TextOverflow.Ellipsis
    )

    BasicText(
        text = media.artist, style = typography.s.semiBold.secondary, maxLines = 1, overflow = TextOverflow.Ellipsis
    )
}
