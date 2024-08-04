package ru.ilyasekunov.videoplayerexample.ui.player

import androidx.annotation.DrawableRes
import androidx.annotation.IntRange
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import ru.ilyasekunov.videoplayerexample.R
import ru.ilyasekunov.videoplayerexample.util.formatMillis

data class Video(
    val url: String,
    val title: String
)

fun List<Video>.toMediaItems(): List<MediaItem> =
    map { it.toMediaItem() }

fun Video.toMediaItem(): MediaItem =
    MediaItem.Builder()
        .setUri(url)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setDisplayTitle(title)
                .build()
        )
        .build()

@Stable
class VideoControlsState(
    visible: Boolean = false,
    isPlaying: Boolean = false,
    isPaused: Boolean = false,
    isLoading: Boolean = false,
    isEnded: Boolean = false,
    playbackState: Int = 0,
    title: String = "",
    currentTimeMs: Long = 0,
    totalDurationMs: Long = 0,
    bufferedPercentage: Int = 0,
    hasPreviousMediaItem: Boolean = false,
    hasNextMediaItem: Boolean = false,
    currentMediaItemIndex: Int = 0,
) {
    var visible by mutableStateOf(visible)
    var isPlaying by mutableStateOf(isPlaying)
    var isPaused by mutableStateOf(isPaused)
    var isLoading by mutableStateOf(isLoading)
    var isEnded by mutableStateOf(isEnded)
    var playbackState by mutableIntStateOf(playbackState)
    var title by mutableStateOf(title)
    var currentTimeMs by mutableLongStateOf(currentTimeMs)
    var totalDurationMs by mutableLongStateOf(totalDurationMs)
    var bufferedPercentage by mutableIntStateOf(bufferedPercentage)
    var hasPreviousMediaItem by mutableStateOf(hasPreviousMediaItem)
    var hasNextMediaItem by mutableStateOf(hasNextMediaItem)
    var currentMediaItemIndex by mutableIntStateOf(currentMediaItemIndex)

    companion object {
        val Saver = mapSaver(
            save = {
                mapOf(
                    "visible" to it.visible,
                    "isPlaying" to it.isPlaying,
                    "isPaused" to it.isPaused,
                    "isLoading" to it.isLoading,
                    "isEnded" to it.isEnded,
                    "playbackState" to it.playbackState,
                    "title" to it.title,
                    "currentTimeMs" to it.currentTimeMs,
                    "totalDurationMs" to it.totalDurationMs,
                    "bufferedPercentage" to it.bufferedPercentage,
                    "hasPreviousMediaItem" to it.hasPreviousMediaItem,
                    "hasNextMediaItem" to it.hasNextMediaItem,
                    "currentMediaItemIndex" to it.currentMediaItemIndex
                )
            },
            restore = {
                VideoControlsState(
                    visible = (it["visible"] as Boolean),
                    isPlaying = (it["isPlaying"] as Boolean),
                    isPaused = (it["isPaused"] as Boolean),
                    isLoading = (it["isLoading"] as Boolean),
                    isEnded = (it["isEnded"] as Boolean),
                    playbackState = (it["playbackState"] as Int),
                    title = (it["title"] as String),
                    currentTimeMs = (it["currentTimeMs"] as Long),
                    totalDurationMs = (it["totalDurationMs"] as Long),
                    bufferedPercentage = (it["bufferedPercentage"] as Int),
                    hasPreviousMediaItem = (it["hasPreviousMediaItem"] as Boolean),
                    hasNextMediaItem = (it["hasNextMediaItem"] as Boolean),
                    currentMediaItemIndex = (it["currentMediaItemIndex"] as Int)
                )
            }
        )
    }
}

@Composable
fun rememberVideoControlsState(player: Player) =
    rememberSaveable(player, saver = VideoControlsState.Saver) {
        VideoControlsState(
            isPlaying = player.isPlaying,
            isPaused = !player.isPlaying && !player.isLoading,
            isLoading = player.isLoading,
            playbackState = player.playbackState,
            title = player.currentMediaItem?.mediaMetadata?.displayTitle.toString(),
            currentTimeMs = player.contentPosition.coerceAtLeast(0),
            totalDurationMs = player.contentDuration.coerceAtLeast(0),
            bufferedPercentage = player.bufferedPercentage,
            hasPreviousMediaItem = player.hasPreviousMediaItem(),
            hasNextMediaItem = player.hasNextMediaItem()
        )
    }

@Composable
fun VideoPlayerControls(
    videoControlsState: VideoControlsState,
    onPlayClick: () -> Unit,
    onPauseClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onReplayClick: () -> Unit,
    onStartedTimeChanging: () -> Unit,
    onFinishTimeChanging: (Float) -> Unit,
    isFullScreen: Boolean,
    onFullScreenClick: () -> Unit,
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        var isUserEditingCurrentTime by rememberSaveable { mutableStateOf(false) }

        VideoPlayerControlsHeader(
            visible = videoControlsState.visible,
            title = videoControlsState.title,
            isFullScreen = isFullScreen,
            navigateBack = navigateBack,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        )

        if (!isUserEditingCurrentTime) {
            VideoPlayerControlsMiddle(
                visible = videoControlsState.visible,
                isPlaying = videoControlsState.isPlaying,
                isPaused = videoControlsState.isPaused,
                isLoading = videoControlsState.isLoading,
                isEnded = videoControlsState.isEnded,
                hasPreviousMediaItem = videoControlsState.hasPreviousMediaItem,
                hasNextMediaItem = videoControlsState.hasNextMediaItem,
                onPreviousClick = onPreviousClick,
                onNextClick = onNextClick,
                onPlayClick = onPlayClick,
                onPauseClick = onPauseClick,
                onReplayClick = onReplayClick,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        VideoPlayerControlsFooter(
            visible = videoControlsState.visible,
            isFullScreen = isFullScreen,
            isUserEditingCurrentTime = isUserEditingCurrentTime,
            currentTimeMs = { videoControlsState.currentTimeMs },
            totalDurationMs = { videoControlsState.totalDurationMs },
            bufferedPercentage = { videoControlsState.bufferedPercentage },
            onStartedTimeChanging = {
                isUserEditingCurrentTime = true
                onStartedTimeChanging()
            },
            onFinishTimeChanging = {
                isUserEditingCurrentTime = false
                onFinishTimeChanging(it)
            },
            onFullScreenClick = onFullScreenClick,
            modifier = Modifier
                .padding(bottom = 20.dp)
                .align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun VideoPlayerControlsHeader(
    visible: Boolean,
    title: String,
    isFullScreen: Boolean,
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically(),
        visible = visible,
        modifier = modifier
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            if (isFullScreen) {
                NavigateBackArrow(onClick = navigateBack)
            }
            Text(
                text = title,
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
private fun VideoPlayerControlsMiddle(
    visible: Boolean,
    isPlaying: Boolean,
    isPaused: Boolean,
    isLoading: Boolean,
    isEnded: Boolean,
    hasPreviousMediaItem: Boolean,
    hasNextMediaItem: Boolean,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onPlayClick: () -> Unit,
    onPauseClick: () -> Unit,
    onReplayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(30.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (hasPreviousMediaItem || hasNextMediaItem) {
                ControlButton(
                    enabled = hasPreviousMediaItem,
                    onClick = onPreviousClick,
                    drawableId = R.drawable.baseline_skip_previous_24
                )
            }

            if (isPlaying) {
                ControlButton(
                    enabled = true,
                    onClick = onPauseClick,
                    drawableId = R.drawable.baseline_pause_24,
                    iconSize = 28.dp,
                    modifier = Modifier.size(52.dp)
                )
            } else if (isPaused) {
                ControlButton(
                    enabled = true,
                    onClick = onPlayClick,
                    drawableId = R.drawable.baseline_play_arrow_24,
                    iconSize = 28.dp,
                    modifier = Modifier.size(52.dp)
                )
            } else if (isEnded) {
                ControlButton(
                    enabled = true,
                    onClick = onReplayClick,
                    drawableId = R.drawable.baseline_replay_24,
                    iconSize = 28.dp,
                    modifier = Modifier.size(52.dp)
                )
            } else if (isLoading) {
                PlayerLoadingIndicator(
                    modifier = Modifier.size(52.dp)
                )
            }

            if (hasPreviousMediaItem || hasNextMediaItem) {
                ControlButton(
                    enabled = hasNextMediaItem,
                    onClick = onNextClick,
                    drawableId = R.drawable.baseline_skip_next_24
                )
            }
        }
    }
}

@Composable
private fun VideoPlayerControlsFooter(
    visible: Boolean,
    isFullScreen: Boolean,
    isUserEditingCurrentTime: Boolean,
    currentTimeMs: () -> Long,
    totalDurationMs: () -> Long,
    bufferedPercentage: () -> Int,
    onStartedTimeChanging: () -> Unit,
    onFinishTimeChanging: (Float) -> Unit,
    onFullScreenClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(
            initialOffsetY = { it / 2 }
        ),
        exit = fadeOut() + slideOutVertically(
            targetOffsetY = { it / 2 }
        ),
        modifier = modifier
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            var currentTimeSliderPosition by remember {
                mutableLongStateOf(currentTimeMs())
            }

            if (isUserEditingCurrentTime) {
                Text(
                    text = currentTimeSliderPosition.formatMillis(),
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(8.dp)
                )
            } else {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CurrentTimeAndTotalDuration(
                        currentTimeMs = currentTimeMs,
                        totalDurationMs = totalDurationMs,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    FullScreenButton(
                        isFullScreen = isFullScreen,
                        onClick = onFullScreenClick,
                        modifier = Modifier
                    )
                }
            }

            TimeAndBufferingView(
                currentTimeMs = {
                    if (isUserEditingCurrentTime) {
                        currentTimeSliderPosition
                    } else {
                        currentTimeMs()
                    }
                },
                isUserEditingCurrentTime = isUserEditingCurrentTime,
                totalDurationMs = totalDurationMs,
                bufferedPercentage = bufferedPercentage,
                onCurrentTimeMsChange = {
                    if (!isUserEditingCurrentTime) {
                        onStartedTimeChanging()
                    }
                    currentTimeSliderPosition = it.toLong()
                },
                onFinishTimeChanging = {
                    onFinishTimeChanging(currentTimeSliderPosition.toFloat())
                },
                modifier = Modifier
                    .height(12.dp)
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
private fun CurrentTimeAndTotalDuration(
    currentTimeMs: () -> Long,
    totalDurationMs: () -> Long,
    modifier: Modifier = Modifier,
    currentTimeColor: Color = Color.White,
    totalDurationColor: Color = MaterialTheme.colorScheme.outline,
    fontSize: TextUnit = 14.sp
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        modifier = modifier
    ) {
        Text(
            text = currentTimeMs().formatMillis(),
            color = currentTimeColor,
            fontSize = fontSize
        )
        Text(
            text = "/ ${totalDurationMs().formatMillis()}",
            color = totalDurationColor,
            fontSize = fontSize
        )
    }
}

@Composable
private fun TimeAndBufferingView(
    currentTimeMs: () -> Long,
    isUserEditingCurrentTime: Boolean,
    totalDurationMs: () -> Long,
    bufferedPercentage: () -> Int,
    onCurrentTimeMsChange: (Float) -> Unit,
    onFinishTimeChanging: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        BufferedPercentageSlider(
            bufferedPercentage = bufferedPercentage,
            modifier = Modifier.fillMaxWidth()
        )
        CurrentTimeSlider(
            isUserEditingCurrentTime = isUserEditingCurrentTime,
            currentTimeMs = currentTimeMs,
            totalDurationMs = totalDurationMs,
            onCurrentTimeMsChange = onCurrentTimeMsChange,
            onFinishTimeChanging = onFinishTimeChanging,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CurrentTimeSlider(
    isUserEditingCurrentTime: Boolean,
    currentTimeMs: () -> Long,
    totalDurationMs: () -> Long,
    onCurrentTimeMsChange: (Float) -> Unit,
    onFinishTimeChanging: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val currentTimeSliderColors = SliderDefaults.colors(
        thumbColor = MaterialTheme.colorScheme.primary,
        inactiveTrackColor = Color.White.copy(alpha = 0.4f),
        activeTrackColor = MaterialTheme.colorScheme.primary
    )

    Slider(
        value = currentTimeMs().toFloat(),
        onValueChange = onCurrentTimeMsChange,
        interactionSource = interactionSource,
        onValueChangeFinished = onFinishTimeChanging,
        valueRange = 0f..totalDurationMs().toFloat(),
        thumb = {
            ThumbSizeIncreasingWhenActive(
                interactionSource = interactionSource,
                defaultSize = DpSize(12.dp, 12.dp),
                increasedActiveSize = DpSize(20.dp, 20.dp),
                isActive = isUserEditingCurrentTime
            )
        },
        track = { sliderState ->
            SliderDefaults.Track(
                sliderState = sliderState,
                colors = currentTimeSliderColors,
                modifier = Modifier.height(12.dp)
            )
        },
        colors = currentTimeSliderColors,
        modifier = modifier
    )
}

@Composable
private fun ThumbSizeIncreasingWhenActive(
    interactionSource: MutableInteractionSource,
    defaultSize: DpSize,
    increasedActiveSize: DpSize,
    isActive: Boolean,
    modifier: Modifier = Modifier,
    colors: SliderColors = SliderDefaults.colors(),
) {
    val thumbModifier = if (!isActive) {
        modifier.offset(
            y = (increasedActiveSize.height - defaultSize.height) / 2
        )
    } else {
        modifier
    }

    SliderDefaults.Thumb(
        interactionSource = interactionSource,
        colors = colors,
        thumbSize = if (!isActive) defaultSize else increasedActiveSize,
        modifier = thumbModifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BufferedPercentageSlider(
    @IntRange(from = 0, to = 100) bufferedPercentage: () -> Int,
    modifier: Modifier = Modifier
) {
    val bufferedSliderColors = SliderDefaults.colors(
        disabledThumbColor = Color.Transparent,
        disabledActiveTrackColor = Color.White
    )
    Slider(
        value = bufferedPercentage().toFloat(),
        enabled = false,
        onValueChange = {},
        valueRange = 0f..100f,
        colors = bufferedSliderColors,
        track = {
            SliderDefaults.Track(
                colors = bufferedSliderColors,
                enabled = false,
                sliderState = it,
                modifier = Modifier.height(12.dp)
            )
        },
        modifier = modifier
    )
}

@Composable
private fun PlayerLoadingIndicator(
    modifier: Modifier = Modifier,
    indicatorColor: Color = Color.White,
) {
    IconButton(
        onClick = {},
        enabled = false,
        colors = IconButtonDefaults.filledIconButtonColors(
            disabledContainerColor = Color.Black.copy(alpha = 0.5f),
            disabledContentColor = Color.White
        ),
        modifier = modifier
    ) {
        CircularProgressIndicator(
            color = indicatorColor,
            strokeWidth = 2.dp,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun ControlButton(
    enabled: Boolean,
    onClick: () -> Unit,
    @DrawableRes drawableId: Int,
    modifier: Modifier = Modifier,
    iconSize: Dp = 24.dp,
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = Color.Black.copy(alpha = 0.5f),
            disabledContainerColor = Color.Black.copy(alpha = 0.2f),
            contentColor = Color.White,
            disabledContentColor = Color.White.copy(alpha = 0.6f)
        ),
        modifier = modifier
    ) {
        Icon(
            painter = painterResource(drawableId),
            contentDescription = "control_button_icon",
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
private fun FullScreenButton(
    isFullScreen: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        colors = IconButtonDefaults.outlinedIconButtonColors(
            contentColor = Color.White
        ),
        modifier = modifier
    ) {
        if (isFullScreen) {
            Icon(
                painter = painterResource(R.drawable.baseline_fullscreen_exit_24),
                contentDescription = "exit_full_screen_button"
            )
        } else {
            Icon(
                painter = painterResource(R.drawable.outline_fullscreen_24),
                contentDescription = "full_screen_button"
            )
        }
    }
}

@Composable
private fun NavigateBackArrow(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = Color.Transparent,
            contentColor = Color.White
        ),
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "arrow_back_icon"
        )
    }
}