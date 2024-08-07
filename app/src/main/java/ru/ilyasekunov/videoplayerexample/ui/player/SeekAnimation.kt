package ru.ilyasekunov.videoplayerexample.ui.player

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import ru.ilyasekunov.videoplayerexample.R

@Composable
internal fun SeekAnimation(
    isForward: Boolean,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFirstTriangleVisible by remember {
        mutableStateOf(false)
    }
    var isSecondTriangleVisible by remember {
        mutableStateOf(false)
    }
    var isThirdTriangleVisible by remember {
        mutableStateOf(false)
    }
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

        val seekSeconds = if (isForward) {
            PLAYER_SEEK_FORWARD_INCREMENT / 1000
        } else {
            PLAYER_SEEK_BACK_INCREMENT / 1000
        }
        Text(
            text = "$seekSeconds ${stringResource(R.string.video_player_seek_seconds)}",
            color = Color.White,
            fontSize = 14.sp
        )
    }

    LaunchedEffect(Unit) {
        if (isForward) {
            isFirstTriangleVisible = true
        } else {
            isThirdTriangleVisible = true
        }

        delay(200L)

        isSecondTriangleVisible = true

        delay(200L)

        if (isForward) {
            isThirdTriangleVisible = true
            isFirstTriangleVisible = false
        } else {
            isFirstTriangleVisible = true
            isThirdTriangleVisible = false
        }

        delay(200L)

        isSecondTriangleVisible = false

        delay(200L)

        if (isForward) {
            isThirdTriangleVisible = false
        } else {
            isFirstTriangleVisible = false
        }

        onFinish()
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