package ru.ilyasekunov.videoplayerexample.ui.player

import androidx.annotation.DrawableRes
import androidx.annotation.IntRange
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import ru.ilyasekunov.videoplayerexample.R
import ru.ilyasekunov.videoplayerexample.ui.ifTrue
import ru.ilyasekunov.videoplayerexample.util.vibrate
import java.util.Locale

@Immutable
class VideoUiState(
    val url: String,
    val title: String
)

@Immutable
private data class SeekAnimationUiState(
    val isPlaying: Boolean,
    val isForward: Boolean,
)

@Composable
private fun rememberSeekAnimationUiState(
    isPlaying: Boolean = false,
    isForward: Boolean = false,
) = remember { mutableStateOf(SeekAnimationUiState(isPlaying, isForward)) }

@Immutable
private data class VideoSpeedAcceleratingUiState(
    val isActive: Boolean,
    val videoSpeedBeforeAccelerating: Float,
    val shouldOpenControlsAfterFinishing: Boolean,
)

@Composable
private fun rememberVideoSpeedAcceleratingUiState(
    isActive: Boolean = false,
    videoSpeedBeforeAccelerating: Float,
    shouldOpenControlsAfterFinishing: Boolean,
) = remember {
    mutableStateOf(
        VideoSpeedAcceleratingUiState(
            isActive = isActive,
            videoSpeedBeforeAccelerating = videoSpeedBeforeAccelerating,
            shouldOpenControlsAfterFinishing = shouldOpenControlsAfterFinishing
        )
    )
}

internal fun List<VideoUiState>.toMediaItems(): List<MediaItem> =
    map { it.toMediaItem() }

internal fun VideoUiState.toMediaItem(): MediaItem =
    MediaItem.Builder()
        .setUri(url)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setDisplayTitle(title)
                .build()
        )
        .build()

@Stable
internal class VideoControlsState(
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
    speed: Float = DefaultSpeed,
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
    var speed by mutableFloatStateOf(speed)

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
                    "currentMediaItemIndex" to it.currentMediaItemIndex,
                    "speed" to it.speed,
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
                    currentMediaItemIndex = (it["currentMediaItemIndex"] as Int),
                    speed = (it["speed"] as Float),
                )
            }
        )
    }
}

@Composable
internal fun rememberVideoControlsState(player: Player) =
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
internal fun VideoPlayerControls(
    videoControlsState: VideoControlsState,
    onClick: () -> Unit,
    onSeekForward: () -> Unit,
    onSeekBack: () -> Unit,
    onPlayClick: () -> Unit,
    onPauseClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onReplayClick: () -> Unit,
    onStartedTimeChanging: () -> Unit,
    onFinishTimeChanging: (Float) -> Unit,
    onVideoPlaybackSpeedSelected: (Float) -> Unit,
    isFullScreen: Boolean,
    onFullScreenClick: () -> Unit,
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var seekAnimationUiState by rememberSeekAnimationUiState()
    var videoSpeedAcceleratingUiState by rememberVideoSpeedAcceleratingUiState(
        videoSpeedBeforeAccelerating = videoControlsState.speed,
        shouldOpenControlsAfterFinishing = videoControlsState.visible
    )

    BlackoutBackground(
        visible = videoControlsState.visible || seekAnimationUiState.isPlaying,
        modifier = Modifier
            .testTag("BlackoutBackground")
            .fillMaxSize()
    )

    Box(
        modifier = modifier
            .observeVideoPlayerTapGestures(
                onClick = onClick,
                onDoubleClickOnRightSide = {
                    onSeekForward()
                    seekAnimationUiState = seekAnimationUiState.copy(
                        isPlaying = true,
                        isForward = true
                    )
                },
                onDoubleClickOnLeftSide = {
                    onSeekBack()
                    seekAnimationUiState = seekAnimationUiState.copy(
                        isPlaying = true,
                        isForward = false
                    )
                },
            )
            .observeVideoPlayerDragGestures(
                onLongPressStart = {
                    videoSpeedAcceleratingUiState = videoSpeedAcceleratingUiState.copy(
                        isActive = true,
                        shouldOpenControlsAfterFinishing = videoControlsState.visible
                    )
                    videoControlsState.visible = false
                    vibrate(context)
                    onVideoPlaybackSpeedSelected(MaxSpeed)
                },
                onLongPressFinish = {
                    videoSpeedAcceleratingUiState = videoSpeedAcceleratingUiState.copy(
                        isActive = false
                    )
                    videoControlsState.visible =
                        videoSpeedAcceleratingUiState.shouldOpenControlsAfterFinishing

                    onVideoPlaybackSpeedSelected(
                        videoSpeedAcceleratingUiState.videoSpeedBeforeAccelerating
                    )
                }
            )
    ) {
        var isUserEditingCurrentTime by rememberSaveable { mutableStateOf(false) }
        var isVideoPlayerSettingsVisible by rememberSaveable { mutableStateOf(false) }

        CurrentSpeedHeader(
            visible = videoSpeedAcceleratingUiState.isActive,
            modifier = Modifier
                .testTag("VideoPlayerCurrentSpeedHeader")
                .align(Alignment.TopCenter)
                .padding(top = 20.dp)
        )

        VideoPlayerControlsHeader(
            visible = videoControlsState.visible,
            title = videoControlsState.title,
            isFullScreen = isFullScreen,
            onSettingsClick = {
                isVideoPlayerSettingsVisible = true
            },
            navigateBack = navigateBack,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
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
                .padding(bottom = 20.dp, end = 8.dp, start = 8.dp)
                .align(Alignment.BottomCenter)
        )

        VideoPlayerControlsSeekAnimation(
            seekAnimationUiState = seekAnimationUiState,
            videoControlsState = videoControlsState,
            onFinish = { seekAnimationUiState = seekAnimationUiState.copy(isPlaying = false) },
            modifier = Modifier
                .testTag("VideoPlayerSeekAnimation")
                .align(Alignment.Center)
        )

        VideoPlayerSettings(
            videoControlsState = videoControlsState,
            visible = isVideoPlayerSettingsVisible,
            onDismiss = { isVideoPlayerSettingsVisible = false },
            onVideoSpeedSelected = {
                isVideoPlayerSettingsVisible = false
                onVideoPlaybackSpeedSelected(it)
            },
            modifier = Modifier
                .testTag("VideoPlayerSettings")
                .offset(y = (-20).dp)
        )
    }
}

@Composable
private fun CurrentSpeedHeader(
    visible: Boolean,
    modifier: Modifier = Modifier,
    currentSpeed: Float = MaxSpeed,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(4.dp)
        ) {
            Text(
                text = "${currentSpeed}x",
                fontSize = 12.sp,
                color = Color.White,
                modifier = Modifier.padding(end = 6.dp)
            )
            Icon(
                painter = painterResource(R.drawable.baseline_play_arrow_24),
                tint = Color.White,
                contentDescription = null,
                modifier = Modifier.size(14.dp)
            )
            Icon(
                painter = painterResource(R.drawable.baseline_play_arrow_24),
                tint = Color.White,
                contentDescription = null,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun VideoPlayerControlsSeekAnimation(
    seekAnimationUiState: SeekAnimationUiState,
    videoControlsState: VideoControlsState,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val seekAnimationOffset = screenWidth / 4

    AnimatedVisibility(
        visible = seekAnimationUiState.isPlaying,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
            .offset(
                x = if (seekAnimationUiState.isForward) {
                    seekAnimationOffset.dp
                } else {
                    (-seekAnimationOffset).dp
                }
            )
    ) {
        val shouldShowControlsAfterAnimation = videoControlsState.visible
        videoControlsState.visible = false

        SeekAnimation(
            isForward = seekAnimationUiState.isForward,
            onFinish = {
                onFinish()
                videoControlsState.visible = shouldShowControlsAfterAnimation
            }
        )
    }
}

@Composable
private fun BlackoutBackground(
    visible: Boolean,
    modifier: Modifier = Modifier,
    enterTransition: EnterTransition = fadeIn(),
    exitTransition: ExitTransition = fadeOut(),
) {
    AnimatedVisibility(
        visible = visible,
        enter = enterTransition,
        exit = exitTransition,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.Black.copy(alpha = 0.5f))
        )
    }
}

@Composable
private fun VideoPlayerControlsHeader(
    visible: Boolean,
    title: String,
    isFullScreen: Boolean,
    onSettingsClick: () -> Unit,
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        enter = fadeIn(),
        exit = fadeOut(),
        visible = visible,
        modifier = modifier
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            if (isFullScreen) {
                NavigateBackArrow(
                    onClick = navigateBack,
                    modifier = Modifier.testTag("VideoPlayerNavigateBackButton")
                )
            }
            Text(
                text = title,
                color = Color.White,
                modifier = Modifier
                    .testTag("VideoPlayerVideoTitle")
                    .align(Alignment.Center)
            )
            SettingsButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .testTag("VideoPlayerSettingButton")
                    .align(Alignment.TopEnd)
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
    Row(
        horizontalArrangement = Arrangement.spacedBy(50.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        if (hasPreviousMediaItem || hasNextMediaItem) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                ControlButton(
                    enabled = hasPreviousMediaItem,
                    onClick = onPreviousClick,
                    drawableId = R.drawable.baseline_skip_previous_24,
                    modifier = Modifier.testTag("VideoPlayerPreviousButton")
                )
            }
        }

        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 2.dp,
                modifier = Modifier
                    .testTag("VideoPlayerLoadingIndicator")
                    .size(48.dp)
            )
        } else {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                if (isPlaying) {
                    ControlButton(
                        enabled = true,
                        onClick = onPauseClick,
                        drawableId = R.drawable.baseline_pause_24,
                        iconSize = 38.dp,
                        modifier = Modifier.testTag("VideoPlayerPauseButton")
                    )
                } else if (isPaused) {
                    ControlButton(
                        enabled = true,
                        onClick = onPlayClick,
                        drawableId = R.drawable.baseline_play_arrow_24,
                        iconSize = 38.dp,
                        modifier = Modifier.testTag("VideoPlayerPlayButton")
                    )
                } else if (isEnded) {
                    ControlButton(
                        enabled = true,
                        onClick = onReplayClick,
                        drawableId = R.drawable.baseline_replay_24,
                        iconSize = 38.dp,
                        modifier = Modifier.testTag("VideoPlayerRepeatButton")
                    )
                }
            }
        }

        if (hasPreviousMediaItem || hasNextMediaItem) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                ControlButton(
                    enabled = hasNextMediaItem,
                    onClick = onNextClick,
                    drawableId = R.drawable.baseline_skip_next_24,
                    modifier = Modifier.testTag("VideoPlayerNextButton")
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
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            var currentTimeSliderPosition by remember {
                mutableLongStateOf(currentTimeMs())
            }

            if (isUserEditingCurrentTime) {
                CurrentEditingTime(
                    currentTimeMs = { currentTimeSliderPosition },
                    modifier = Modifier.testTag("VideoPlayerCurrentEditingTime")
                )
            } else {
                TimeWithFullScreenButtonSection(
                    currentTimeMs = currentTimeMs,
                    totalDurationMs = totalDurationMs,
                    isFullScreen = isFullScreen,
                    onFullScreenClick = onFullScreenClick,
                    modifier = Modifier
                        .testTag("VideoPlayerTimeWithFullScreenButtonSection")
                        .fillMaxWidth()
                )
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
                    .testTag("VideoPlayerTimeAndBufferingView")
                    .height(12.dp)
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
private fun TimeWithFullScreenButtonSection(
    currentTimeMs: () -> Long,
    totalDurationMs: () -> Long,
    isFullScreen: Boolean,
    onFullScreenClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        CurrentTimeAndTotalDuration(
            currentTimeMs = currentTimeMs,
            totalDurationMs = totalDurationMs,
            modifier = Modifier
                .testTag("VideoPlayerCurrentTimeAndTotalDuration")
                .padding(horizontal = 20.dp)
        )
        FullScreenButton(
            isFullScreen = isFullScreen,
            onClick = onFullScreenClick,
            modifier = Modifier.testTag("VideoPlayerFullScreenButton")
        )
    }
}

@Composable
private fun CurrentEditingTime(
    currentTimeMs: () -> Long,
    modifier: Modifier = Modifier
) {
    Text(
        text = currentTimeMs().formatMillis(),
        color = Color.White,
        modifier = modifier
            .offset(y = (-16).dp)
            .fillMaxWidth()
            .wrapContentWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.5f))
            .padding(8.dp)
    )
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
            modifier = Modifier
                .testTag("VideoPlayerCurrentTimeSlider")
                .fillMaxWidth()
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
    val thumbWidth by animateDpAsState(
        targetValue = if (isActive) increasedActiveSize.width else defaultSize.width,
        label = "thumbWidth"
    )
    val thumbHeight by animateDpAsState(
        targetValue = if (isActive) increasedActiveSize.height else defaultSize.height,
        label = "thumbHeight"
    )
    val thumbOffset by animateDpAsState(
        targetValue = if (!isActive) {
            (increasedActiveSize.height - defaultSize.height) / 2
        } else {
            0.dp
        },
        label = "thumbOffset"
    )

    SliderDefaults.Thumb(
        interactionSource = interactionSource,
        colors = colors,
        thumbSize = if (!isActive) defaultSize else increasedActiveSize,
        modifier = modifier
            .offset(y = thumbOffset)
            .height(thumbHeight)
            .width(thumbWidth)
    )
}

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
        modifier = modifier
    )
}

@Composable
private fun ControlButton(
    enabled: Boolean,
    onClick: () -> Unit,
    @DrawableRes drawableId: Int,
    modifier: Modifier = Modifier,
    iconSize: Dp = 24.dp,
    withBackground: Boolean = true,
    padding: Dp = iconSize / 4,
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .ifTrue(withBackground) {
                background(
                    color = Color.Black.copy(alpha = if (enabled) 0.5f else 0.3f),
                    shape = CircleShape
                )
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(color = Color.White),
                onClick = onClick,
                enabled = enabled,
                role = Role.Button
            )
            .padding(padding)
    ) {
        Icon(
            painter = painterResource(drawableId),
            contentDescription = "control_icon_button",
            tint = if (enabled) Color.White else Color.White.copy(alpha = 0.6f),
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
    ControlButton(
        iconSize = 22.dp,
        enabled = true,
        withBackground = false,
        padding = 14.dp,
        onClick = onClick,
        drawableId = if (isFullScreen) {
            R.drawable.baseline_fullscreen_exit_24
        } else {
            R.drawable.outline_fullscreen_24
        },
        modifier = modifier
    )
}

@Composable
private fun SettingsButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ControlButton(
        iconSize = 22.dp,
        enabled = true,
        withBackground = false,
        padding = 14.dp,
        onClick = onClick,
        drawableId = R.drawable.outline_settings_24,
        modifier = modifier
    )
}

@Composable
private fun NavigateBackArrow(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ControlButton(
        iconSize = 22.dp,
        enabled = true,
        withBackground = false,
        padding = 14.dp,
        onClick = onClick,
        drawableId = R.drawable.baseline_arrow_back_24,
        modifier = modifier
    )
}

private fun Modifier.observeVideoPlayerDragGestures(
    onLongPressStart: () -> Unit,
    onLongPressFinish: () -> Unit,
): Modifier = this.pointerInput(Unit) {
    detectDragGesturesAfterLongPress(
        onDragStart = { onLongPressStart() },
        onDragEnd = { onLongPressFinish() },
        onDragCancel = { onLongPressFinish() },
        onDrag = { _, _ -> }
    )
}

private fun Modifier.observeVideoPlayerTapGestures(
    onClick: () -> Unit,
    onDoubleClickOnRightSide: () -> Unit,
    onDoubleClickOnLeftSide: () -> Unit,
): Modifier = this.pointerInput(Unit) {
    detectTapGestures(
        onTap = { onClick() },
        onDoubleTap = { offset ->
            if (offset.x > size.width / 2) {
                onDoubleClickOnRightSide()
            } else {
                onDoubleClickOnLeftSide()
            }
        }
    )
}

private fun Long.formatMillis(): String {
    val seconds = this / 1000
    val minutesFormatted = seconds / 60
    val secondsAfterMinute = seconds % 60

    return String.format(
        locale = Locale.getDefault(),
        format = "%02d:%02d",
        args = arrayOf(minutesFormatted, secondsAfterMinute)
    )
}