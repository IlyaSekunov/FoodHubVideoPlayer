package ru.ilyasekunov.videoplayerexample.ui.player

import android.app.Activity
import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay
import ru.ilyasekunov.videoplayerexample.ui.conditional
import ru.ilyasekunov.videoplayerexample.util.hideSystemUi
import ru.ilyasekunov.videoplayerexample.util.setLandscape
import ru.ilyasekunov.videoplayerexample.util.setPortrait

internal const val PLAYER_SEEK_BACK_INCREMENT = 10 * 1000L // 10 seconds
internal const val PLAYER_SEEK_FORWARD_INCREMENT = 10 * 1000L // 10 seconds
internal const val PLAYER_CONTROLS_VISIBILITY_TIME = 5 * 1000L // 5 seconds

private fun videoPlayerStateListener(videoControlsState: VideoControlsState): Player.Listener =
    object : Player.Listener {
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
                title = player.currentMediaItem?.mediaMetadata?.displayTitle.toString()
                currentMediaItemIndex = player.currentMediaItemIndex
                speed = player.playbackParameters.speed
            }
        }
    }

private fun videoPlayerLifecycleObserver(
    videoPlayer: Player,
    videoControlsState: VideoControlsState
): DefaultLifecycleObserver = object : DefaultLifecycleObserver {
    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)

        // Restore state
        with(videoPlayer) {
            seekTo(videoControlsState.currentMediaItemIndex, videoControlsState.currentTimeMs)
            setPlaybackSpeed(videoControlsState.speed)
        }

        videoPlayer.prepare()
        if (!videoControlsState.isPaused) {
            videoPlayer.play()
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        videoPlayer.stop()
    }
}

@OptIn(UnstableApi::class)
@Composable
fun FoodHubVideoPlayer(
    videos: List<VideoUiState>,
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

    val videoControlsState = rememberVideoControlsState(exoPlayer)

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(Unit) {
        val playerListener = videoPlayerStateListener(videoControlsState)
        val videoPlayerLifecycleObserver = videoPlayerLifecycleObserver(
            videoPlayer = exoPlayer,
            videoControlsState = videoControlsState
        )

        exoPlayer.addListener(playerListener)
        lifecycle.addObserver(videoPlayerLifecycleObserver)

        onDispose {
            lifecycle.removeObserver(videoPlayerLifecycleObserver)
            exoPlayer.removeListener(playerListener)
            exoPlayer.release()
        }
    }

    val configuration = LocalConfiguration.current
    val isFullScreen = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    VideoPlayerWithControls(
        player = exoPlayer,
        videoControlsState = videoControlsState,
        isFullScreen = isFullScreen,
        onFullScreenClick = {
            if (isFullScreen) {
                setPortrait(context)
            } else {
                setLandscape(context)
            }
        },
        modifier = modifier
            .systemBarsPadding()
            .conditional(
                condition = isFullScreen,
                trueBlock = { fillMaxSize() },
                falseBlock = { aspectRatio(16f / 9f) }
            )
    )
}

@Composable
private fun VideoPlayerWithControls(
    player: Player,
    videoControlsState: VideoControlsState,
    isFullScreen: Boolean,
    onFullScreenClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    if (isFullScreen) {
        hideSystemUi(context as Activity)

        BackHandler {
            setPortrait(context)
        }
    }

    Box(modifier = modifier) {
        var isUserInteractingWithPlayer by rememberSaveable { mutableStateOf(false) }

        LaunchedEffect(videoControlsState.visible, isUserInteractingWithPlayer) {
            if (videoControlsState.visible && !isUserInteractingWithPlayer) {
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

        VideoPlayer(
            player = player,
            modifier = Modifier
                .testTag("VideoPlayer")
                .fillMaxSize()
        )

        VideoPlayerControls(
            videoControlsState = videoControlsState,
            onClick = { videoControlsState.visible = !videoControlsState.visible },
            onSeekForward = player::seekForward,
            onSeekBack = player::seekBack,
            onPlayClick = player::play,
            onPauseClick = player::pause,
            onPreviousClick = player::seekToPrevious,
            onNextClick = player::seekToNext,
            onReplayClick = { player.seekTo(0) },
            onStartedTimeChanging = {
                isUserInteractingWithPlayer = true
            },
            onFinishTimeChanging = {
                player.seekTo(it.toLong())
                isUserInteractingWithPlayer = false
            },
            onVideoPlaybackSpeedSelected = player::setPlaybackSpeed,
            isFullScreen = isFullScreen,
            onFullScreenClick = onFullScreenClick,
            navigateBack = {
                if (isFullScreen) {
                    setPortrait(context)
                }
            },
            modifier = Modifier
                .testTag("VideoPlayerControls")
                .fillMaxSize()
                .displayCutoutPadding()
        )
    }
}

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(
    player: Player,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        AndroidView(
            factory = {
                PlayerView(it).apply {
                    this.player = player
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    useController = false
                    clipToOutline = true
                }
            }
        )
    }
}