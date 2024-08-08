package ru.ilyasekunov.videoplayerexample.ui.player

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.ilyasekunov.videoplayerexample.R

internal const val DefaultSpeed = 1f
internal const val MaxSpeed = 2f

internal val AvailablePlaybackSpeedValues = listOf(
    0.25f,
    0.5f,
    0.75f,
    DefaultSpeed,
    1.25f,
    1.5f,
    1.75f,
    MaxSpeed,
)

internal enum class AvailableSettings {
    PlaybackSpeed,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun VideoPlayerSettings(
    visible: Boolean,
    onDismiss: () -> Unit,
    onVideoSpeedSelected: (Float) -> Unit,
    modifier: Modifier = Modifier,
    videoControlsState: VideoControlsState,
) {
    var currentSettingSelected by remember { mutableStateOf<AvailableSettings?>(null) }
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val edgePadding = 10.dp

    if (visible && currentSettingSelected == null) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetMaxWidth = screenWidth.dp - edgePadding,
            containerColor = Color.White,
            shape = RoundedCornerShape(10.dp),
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            windowInsets = WindowInsets.safeContent,
            modifier = modifier
        ) {
            PlaybackSpeedSelector(
                currentSpeed = videoControlsState.speed,
                onClick = {
                    currentSettingSelected = AvailableSettings.PlaybackSpeed
                },
                modifier = Modifier.testTag("PlaybackSpeedSelector")
            )
        }
    }

    if (currentSettingSelected != null) {
        ModalBottomSheet(
            onDismissRequest = {
                currentSettingSelected = null
                onDismiss()
            },
            containerColor = Color.White,
            sheetMaxWidth = screenWidth.dp - edgePadding,
            shape = RoundedCornerShape(10.dp),
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            windowInsets = WindowInsets.safeContent,
            modifier = modifier
        ) {
            when (currentSettingSelected) {
                AvailableSettings.PlaybackSpeed -> {
                    AvailablePlaybackSpeedValues(
                        currentSpeedSelected = videoControlsState.speed,
                        onVideoSpeedSelected = {
                            onVideoSpeedSelected(it)
                            currentSettingSelected = null
                        },
                        modifier = Modifier.testTag("AvailablePlaybackSpeedValues")
                    )
                }

                else -> {}
            }
        }
    }
}

@Composable
private fun PlaybackSpeedSelector(
    currentSpeed: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SettingSelector(
        leadingIconId = R.drawable.baseline_speed_24,
        descriptionId = R.string.video_player_playback_speed_description,
        onClick = onClick,
        currentValue = if (currentSpeed == DefaultSpeed) {
            stringResource(R.string.video_player_default_speed_name)
        } else {
            currentSpeed.toString()
        },
        modifier = modifier
    )
}

@Composable
private fun AvailablePlaybackSpeedValues(
    currentSpeedSelected: Float,
    onVideoSpeedSelected: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        AvailablePlaybackSpeedValues.forEach { speed ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onVideoSpeedSelected(speed) }
                    .padding(16.dp)
            ) {
                if (speed == currentSpeedSelected) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_check_24),
                        contentDescription = "CheckIcon"
                    )
                }
                Text(
                    text = if (speed == DefaultSpeed) {
                        stringResource(R.string.video_player_default_speed_name)
                    } else {
                        speed.toString()
                    },
                    modifier = Modifier.padding(start = 14.dp)
                )
            }
        }
    }
}

@Composable
private fun SettingSelector(
    @DrawableRes leadingIconId: Int,
    @StringRes descriptionId: Int,
    currentValue: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Icon(
            painter = painterResource(leadingIconId),
            contentDescription = "SettingsSelectorLeadingIcon"
        )
        Text(
            text = stringResource(descriptionId),
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 14.dp)
        )
        Text(
            text = currentValue,
            fontSize = 14.sp,
            color = Color.Gray,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .wrapContentWidth(Alignment.End)
        )
        Icon(
            painter = painterResource(R.drawable.baseline_keyboard_arrow_right_24),
            tint = Color.Gray,
            contentDescription = null,
            modifier = Modifier
                .size(24.dp)
                .padding(start = 6.dp)
        )
    }
}