package ru.ilyasekunov.videoplayerexample

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.view.autofill.AutofillValue
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.changedToDownIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.size
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerControlView
import androidx.media3.ui.PlayerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import ru.ilyasekunov.videoplayerexample.ui.theme.VideoPlayerExampleTheme
import java.util.Locale

const val PLAYER_SEEK_BACK_INCREMENT = 10 * 1000L // 10 seconds
const val PLAYER_SEEK_FORWARD_INCREMENT = 10 * 1000L // 10 seconds
const val PLAYER_CONTROLS_VISIBILITY_TIME = 5 * 1000L // 5 seconds

data class Video(
    val url: String,
    val title: String
)

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
                    "hasNextMediaItem" to it.hasNextMediaItem
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
                    hasNextMediaItem = (it["hasNextMediaItem"] as Boolean)
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

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            VideoPlayerExampleTheme {
                FoodHubVideoPlayer(
                    videos = listOf(
                        Video(
                            url = "https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4",
                            title = "Some video"
                        )
                    ),
                    initiallyStartPlaying = true,
                    autoRepeat = false,
                    modifier = Modifier
                        //.fillMaxSize()
                        .fillMaxWidth()
                        .aspectRatio(16 / 9f)
                )
            }
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun FoodHubVideoPlayer(
    videos: List<Video>,
    initiallyStartPlaying: Boolean,
    autoRepeat: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .setSeekBackIncrementMs(PLAYER_SEEK_BACK_INCREMENT)
            .setSeekForwardIncrementMs(PLAYER_SEEK_FORWARD_INCREMENT)
            .build().apply {
                setMediaItems(videos.toMediaItems())
                playWhenReady = initiallyStartPlaying
                repeatMode = if (autoRepeat) Player.REPEAT_MODE_ALL else Player.REPEAT_MODE_OFF

                prepare()
            }
    }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(Unit) {
        val observer = object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                super.onStart(owner)
                if (!exoPlayer.isPlaying) {
                    exoPlayer.play()
                }
            }

            override fun onStop(owner: LifecycleOwner) {
                exoPlayer.stop()
                super.onStop(owner)
            }
        }
        lifecycle.addObserver(observer)

        onDispose {
            lifecycle.removeObserver(observer)
            exoPlayer.release()
        }
    }

    val configuration = LocalConfiguration.current
    val isFullScreen = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    VideoPlayerView(
        player = exoPlayer,
        isFullScreen = isFullScreen,
        onFullScreenClick = {
            if (isFullScreen) {
                setPortrait(context)
            } else {
                setLandscape(context)
            }
        },
        modifier = modifier
    )
}

@Composable
fun VideoPlayerView(
    player: Player,
    isFullScreen: Boolean,
    onFullScreenClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    BackHandler {
        if (isFullScreen) {
            setPortrait(context)
        }
    }

    Box(modifier = modifier) {
        val videoControlsState = rememberVideoControlsState(player)

        LaunchedEffect(videoControlsState.visible) {
            if (videoControlsState.visible) {
                delay(PLAYER_CONTROLS_VISIBILITY_TIME)
                videoControlsState.visible = false
            }
        }

        LaunchedEffect(videoControlsState.isPlaying) {
            if (videoControlsState.isPlaying) {
                while (true) {
                    videoControlsState.currentTimeMs = player.contentPosition
                        .coerceAtLeast(0)
                    delay(200)
                }
            }
        }

        DisposableEffect(Unit) {
            val playerListener = object : Player.Listener {
                override fun onEvents(player: Player, events: Player.Events) {
                    super.onEvents(player, events)
                    with(videoControlsState) {
                        isPlaying = player.isPlaying
                        isLoading = player.isLoading
                        playbackState = player.playbackState
                        isEnded = playbackState == Player.STATE_ENDED
                        hasPreviousMediaItem = player.hasPreviousMediaItem()
                        hasNextMediaItem = player.hasNextMediaItem()
                        isPaused = !isPlaying && !isLoading && !isEnded
                        totalDurationMs = player.contentDuration.coerceAtLeast(0)
                        currentTimeMs = player.contentPosition.coerceAtLeast(0)
                        bufferedPercentage = player.bufferedPercentage
                    }
                }

                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    super.onMediaItemTransition(mediaItem, reason)
                    videoControlsState.title = mediaItem?.mediaMetadata?.displayTitle.toString()
                }
            }

            player.addListener(playerListener)

            onDispose {
                player.removeListener(playerListener)
            }
        }

        VideoPlayer(
            player = player,
            onClick = { videoControlsState.visible = !videoControlsState.visible },
            modifier = Modifier.fillMaxSize()
        )

        VideoPlayerControls(
            videoControlsState = videoControlsState,
            onPlayClick = player::play,
            onPauseClick = player::pause,
            onPreviousClick = player::seekToPrevious,
            onNextClick = player::seekForward,
            onReplayClick = player::seekBack,
            onCurrentTimeMsChange = {
                player.seekTo(it.toLong())
            },
            isFullScreen = isFullScreen,
            onFullScreenClick = onFullScreenClick,
            navigateBack = {
                if (isFullScreen) {
                    setPortrait(context)
                }
            },
            modifier = Modifier
                .fillMaxSize()
                //.safeDrawingPadding()
        )
    }
}

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(
    player: Player,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    AndroidView(
        factory = {
            PlayerView(context).apply {
                this.player = player
                useController = false
                //clipToOutline = true
            }
        },
        modifier = modifier.noRippleClickable(onClick = onClick)
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
    onCurrentTimeMsChange: (Float) -> Unit,
    isFullScreen: Boolean,
    onFullScreenClick: () -> Unit,
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
    ) {
        var isUserEditingCurrentTime by rememberSaveable { mutableStateOf(false) }

        VideoPlayerControlsHeader(
            visible = videoControlsState.visible,
            title = videoControlsState.title,
            isFullScreen = isFullScreen,
            navigateBack = navigateBack
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
                onReplayClick = onReplayClick
            )
        }

        VideoPlayerControlsFooter(
            visible = videoControlsState.visible,
            isUserEditingCurrentTime = isUserEditingCurrentTime,
            currentTimeMs = videoControlsState.currentTimeMs,
            totalDurationMs = videoControlsState.totalDurationMs,
            bufferedPercentage = videoControlsState.bufferedPercentage,
            onStartedTimeChanging = {
                onPauseClick()
                isUserEditingCurrentTime = true
            },
            onFinishTimeChanging = {
                onCurrentTimeMsChange(it)
                onPlayClick()
                isUserEditingCurrentTime = false
            },
            onFullScreenClick = onFullScreenClick
        )
    }
}

@Composable
fun VideoPlayerControlsHeader(
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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isFullScreen) {
                NavigateBackArrow(onClick = navigateBack)
            }
            Text(
                text = title,
                color = Color.White,
                modifier = Modifier
                    .weight(1f)
                    .wrapContentWidth()
            )
        }
    }
}

@Composable
fun VideoPlayerControlsMiddle(
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
        exit = fadeOut()
    ) {
        Row(modifier = modifier) {
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
                    drawableId = R.drawable.baseline_pause_24
                )
            } else if (isPaused) {
                ControlButton(
                    enabled = true,
                    onClick = onPlayClick,
                    drawableId = R.drawable.baseline_play_arrow_24
                )
            } else if (isEnded) {
                ControlButton(
                    enabled = true,
                    onClick = onReplayClick,
                    drawableId = R.drawable.baseline_replay_24
                )
            } else if (isLoading) {
                CircularProgressIndicator()
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
fun VideoPlayerControlsFooter(
    visible: Boolean,
    isUserEditingCurrentTime: Boolean,
    currentTimeMs: Long,
    totalDurationMs: Long,
    bufferedPercentage: Int,
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
                mutableLongStateOf(currentTimeMs)
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
                    Text(
                        text = "${currentTimeMs.formatMillis()}/${totalDurationMs.formatMillis()}",
                        color = Color.White
                    )
                    FullScreenButton(
                        onClick = onFullScreenClick,
                        modifier = Modifier
                    )
                }
            }

            TimeAndBufferingView(
                currentTimeMs = currentTimeSliderPosition,
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
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun TimeAndBufferingView(
    currentTimeMs: Long,
    totalDurationMs: Long,
    bufferedPercentage: Int,
    onCurrentTimeMsChange: (Float) -> Unit,
    onFinishTimeChanging: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Slider(
            value = bufferedPercentage.toFloat(),
            enabled = false,
            onValueChange = {},
            valueRange = 0f..100f,
            colors = SliderDefaults.colors(
                disabledThumbColor = Color.Transparent,
                disabledActiveTrackColor = Color.Gray
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Slider(
            value = currentTimeMs.toFloat(),
            onValueChange = {
                onCurrentTimeMsChange(it)
            },
            onValueChangeFinished = onFinishTimeChanging,
            valueRange = 0f..totalDurationMs.toFloat()
        )
    }
}

@Composable
fun ControlButton(
    enabled: Boolean,
    onClick: () -> Unit,
    @DrawableRes drawableId: Int,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = Color.Black.copy(alpha = 0.5f),
            contentColor = Color.White
        ),
        modifier = modifier
    ) {
        Icon(
            painter = painterResource(drawableId),
            contentDescription = "control_button_icon"
        )
    }
}

@Composable
fun FullScreenButton(
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
        Icon(
            painter = painterResource(R.drawable.outline_fullscreen_24),
            contentDescription = "full_screen_button"
        )
    }
}

@Composable
fun NavigateBackArrow(
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

fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier = composed {
    this.clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
    )
}

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

fun setLandscape(context: Context) {
    val activity = context.findActivity()
    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
}

fun setPortrait(context: Context) {
    val activity = context.findActivity()
    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
}

fun Long.formatMillis(): String {
    val seconds = this / 1000
    val minutesFormatted = seconds / 60
    val secondsAfterMinute = seconds % 60

    return String.format(
        locale = Locale.getDefault(),
        format = "%02d:%02d",
        args = arrayOf(minutesFormatted, secondsAfterMinute)
    )
}

fun Modifier.ifTrue(condition: Boolean, block: Modifier.() -> Modifier): Modifier =
    if (condition) {
        this.block()
    } else {
        this
    }