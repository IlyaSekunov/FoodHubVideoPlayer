package ru.ilyasekunov.foodhubvideoplayer.ui.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import ru.ilyasekunov.foodhubvideoplayer.R

@Stable
internal class SeekAnimationUiState(
    isPlaying: Boolean,
    isForward: Boolean,
    seekTime: Long,
) {
    private var _isPlaying by mutableStateOf(isPlaying)
    val isPlaying get() = _isPlaying

    private var _isForward by mutableStateOf(isForward)
    val isForward get() = _isForward

    private var _seekTime by mutableLongStateOf(seekTime)
    val seekTime get() = _seekTime

    var iterations = 0
        private set

    fun increaseSeekTimeBy(time: Long) {
        incrementIterations()
        _seekTime += time
    }

    fun startAnimation(isForward: Boolean) {
        incrementIterations()
        _isPlaying = true
        _isForward = isForward
        _seekTime = PLAYER_SEEK_INCREMENT
    }

    fun stopAnimation() {
        _isPlaying = false
        iterations = 0
    }

    private fun incrementIterations() {
        if (iterations == 0)
            ++iterations
    }

    fun decrementIterations() {
        if (iterations > 0)
            --iterations
    }
}

@Composable
internal fun rememberSeekAnimationUiState(
    isPlaying: Boolean = false,
    isForward: Boolean = false,
    seekTime: Long = PLAYER_SEEK_INCREMENT,
) = remember {
    SeekAnimationUiState(
        isPlaying = isPlaying,
        isForward = isForward,
        seekTime = seekTime
    )
}

@Composable
internal fun SeekAnimation(
    seekAnimationUiState: SeekAnimationUiState,
    onStart: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = seekAnimationUiState.isPlaying,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        var isFirstTriangleVisible by remember { mutableStateOf(false) }
        var isSecondTriangleVisible by remember { mutableStateOf(false) }
        var isThirdTriangleVisible by remember { mutableStateOf(false) }

        SeekAnimationContent(
            isForward = seekAnimationUiState.isForward,
            seekTimeMs = seekAnimationUiState.seekTime,
            isFirstTriangleVisible = isFirstTriangleVisible,
            isSecondTriangleVisible = isSecondTriangleVisible,
            isThirdTriangleVisible = isThirdTriangleVisible,
        )

        LaunchedEffect(seekAnimationUiState.isForward) {
            onStart()

            while (seekAnimationUiState.iterations > 0) {
                seekAnimationUiState.decrementIterations()

                if (seekAnimationUiState.isForward) {
                    isFirstTriangleVisible = true
                } else {
                    isThirdTriangleVisible = true
                }

                delay(200L)

                isSecondTriangleVisible = true

                delay(200L)

                if (seekAnimationUiState.isForward) {
                    isThirdTriangleVisible = true
                    isFirstTriangleVisible = false
                } else {
                    isFirstTriangleVisible = true
                    isThirdTriangleVisible = false
                }

                delay(200L)

                isSecondTriangleVisible = false

                delay(200L)

                if (seekAnimationUiState.isForward) {
                    isThirdTriangleVisible = false
                } else {
                    isFirstTriangleVisible = false
                }
            }

            onFinish()
        }
    }
}

@Composable
private fun SeekAnimationContent(
    isForward: Boolean,
    seekTimeMs: Long,
    isFirstTriangleVisible: Boolean,
    isSecondTriangleVisible: Boolean,
    isThirdTriangleVisible: Boolean,
    modifier: Modifier = Modifier
) {
    val firstTriangleAlpha by animateFloatAsState(
        targetValue = if (isFirstTriangleVisible) 1f else 0f,
        animationSpec = tween(400),
        label = "firstTriangleAlpha"
    )
    val secondTriangleAlpha by animateFloatAsState(
        targetValue = if (isSecondTriangleVisible) 1f else 0f,
        animationSpec = tween(400),
        label = "secondTriangleAlpha"
    )
    val thirdTriangleAlpha by animateFloatAsState(
        targetValue = if (isThirdTriangleVisible) 1f else 0f,
        animationSpec = tween(400),
        label = "thirdTriangleAlpha"
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
    ) {
        Row {
            Triangle(
                isForward = isForward,
                modifier = Modifier
                    .size(22.dp)
                    .alpha(firstTriangleAlpha)
            )

            Triangle(
                isForward = isForward,
                modifier = Modifier
                    .size(22.dp)
                    .alpha(secondTriangleAlpha)
            )

            Triangle(
                isForward = isForward,
                modifier = Modifier
                    .size(22.dp)
                    .alpha(thirdTriangleAlpha)
            )
        }

        Text(
            text = "${seekTimeMs / 1000} ${stringResource(R.string.video_player_seek_seconds)}",
            color = Color.White,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun Triangle(
    isForward: Boolean,
    modifier: Modifier = Modifier
) {
    Icon(
        painter = painterResource(R.drawable.baseline_play_arrow_24),
        contentDescription = "triangle",
        tint = Color.White,
        modifier = modifier
            .graphicsLayer {
                scaleY = 1.2f
                if (!isForward) {
                    rotationY = -180f
                }
            }
    )
}