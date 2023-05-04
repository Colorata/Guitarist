package it.vfsfitvnm.vimusic.ui.screens.player

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.C
import androidx.media3.common.Player
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.models.Song
import it.vfsfitvnm.vimusic.models.ui.UiMedia
import it.vfsfitvnm.vimusic.query
import it.vfsfitvnm.vimusic.ui.components.SeekBar
import it.vfsfitvnm.vimusic.ui.components.themed.IconButton
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.ui.styling.favoritesIcon
import it.vfsfitvnm.vimusic.utils.*
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun Controls(
    media: UiMedia, shouldBePlaying: Boolean, position: Long, modifier: Modifier = Modifier
) {
    val (colorPalette, typography) = LocalAppearance.current

    val binder = LocalPlayerServiceBinder.current
    binder?.player ?: return

    var trackLoopEnabled by rememberPreference(trackLoopEnabledKey, defaultValue = false)

    var scrubbingPosition by remember(media.id) {
        mutableStateOf<Long?>(null)
    }

    var likedAt by rememberSaveable {
        mutableStateOf<Long?>(null)
    }

    LaunchedEffect(media.id) {
        Database.likedAt(media.id).distinctUntilChanged().collect { likedAt = it }
    }

    val shouldBePlayingTransition = updateTransition(shouldBePlaying, label = "shouldBePlaying")

    val playPauseRoundness by shouldBePlayingTransition.animateDp(transitionSpec = {
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

        BasicText(
            text = media.title, style = typography.l.bold, maxLines = 1, overflow = TextOverflow.Ellipsis
        )

        BasicText(
            text = media.artist, style = typography.s.semiBold.secondary, maxLines = 1, overflow = TextOverflow.Ellipsis
        )

        Spacer(
            modifier = Modifier.weight(0.5f)
        )

        SeekBar(
            position = scrubbingPosition ?: position,
            range = 0..media.duration,
            onSeekStarted = {
                scrubbingPosition = it
            }, onSeek = { delta ->
                scrubbingPosition = if (media.duration != C.TIME_UNSET) {
                    scrubbingPosition?.plus(delta)?.coerceIn(0, media.duration)
                } else {
                    null
                }
            }, onSeekFinished = {
                scrubbingPosition?.let(binder.player::seekTo)
                scrubbingPosition = null
            }, color = colorPalette.text, isActive = binder.player.isPlaying, backgroundColor = colorPalette.background2, shape = RoundedCornerShape(8.dp)
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            BasicText(
                text = formatAsDuration(scrubbingPosition ?: position),
                style = typography.xxs.semiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            if (media.duration != C.TIME_UNSET) {
                BasicText(
                    text = formatAsDuration(media.duration),
                    style = typography.xxs.semiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Spacer(
            modifier = Modifier.weight(1f)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                icon = if (likedAt == null) R.drawable.heart_outline else R.drawable.heart,
                color = colorPalette.favoritesIcon,
                onClick = {
                    val currentMediaItem = binder.player.currentMediaItem
                    query {
                        if (Database.like(
                                media.id, if (likedAt == null) System.currentTimeMillis() else null
                            ) == 0
                        ) {
                            currentMediaItem?.takeIf { it.mediaId == media.id }?.let {
                                Database.insert(currentMediaItem, Song::toggleLike)
                            }
                        }
                    }
                },
                modifier = Modifier.weight(1f).size(24.dp)
            )

            IconButton(
                icon = R.drawable.play_skip_back,
                color = colorPalette.text,
                onClick = binder.player::forceSeekToPrevious,
                modifier = Modifier.weight(1f).size(24.dp)
            )

            Spacer(
                modifier = Modifier.width(8.dp)
            )

            Box(modifier = Modifier.clip(RoundedCornerShape(playPauseRoundness)).clickable {
                if (shouldBePlaying) {
                    binder.player.pause()
                } else {
                    if (binder.player.playbackState == Player.STATE_IDLE) {
                        binder.player.prepare()
                    }
                    binder.player.play()
                }
            }.background(colorPalette.background2).size(64.dp)) {
                Image(
                    painter = painterResource(if (shouldBePlaying) R.drawable.pause else R.drawable.play),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(colorPalette.text),
                    modifier = Modifier.align(Alignment.Center).size(28.dp)
                )
            }

            Spacer(
                modifier = Modifier.width(8.dp)
            )

            IconButton(
                icon = R.drawable.play_skip_forward,
                color = colorPalette.text,
                onClick = binder.player::forceSeekToNext,
                modifier = Modifier.weight(1f).size(24.dp)
            )

            IconButton(
                icon = R.drawable.infinite,
                color = if (trackLoopEnabled) colorPalette.text else colorPalette.textDisabled,
                onClick = { trackLoopEnabled = !trackLoopEnabled },
                modifier = Modifier.weight(1f).size(24.dp)
            )
        }

        Spacer(
            modifier = Modifier.weight(1f)
        )
    }
}
