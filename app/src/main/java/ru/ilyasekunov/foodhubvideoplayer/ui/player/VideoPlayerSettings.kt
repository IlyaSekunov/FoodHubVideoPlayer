package ru.ilyasekunov.foodhubvideoplayer.ui.player

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsIgnoringVisibility
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import ru.ilyasekunov.foodhubvideoplayer.R

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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun VideoPlayerSettings(
    visible: Boolean,
    isFullScreen: Boolean,
    onDismiss: () -> Unit,
    onVideoSpeedSelected: (Float) -> Unit,
    modifier: Modifier = Modifier,
    videoControlsState: VideoControlsState,
) {
    var currentSettingSelected by remember { mutableStateOf<AvailableSettings?>(null) }
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val edgePadding = 10.dp
    val windowInsets = if (isFullScreen) {
        WindowInsets.statusBarsIgnoringVisibility.add(WindowInsets.displayCutout)
    } else {
        WindowInsets.navigationBars
    }

    if (visible && currentSettingSelected == null) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetMaxWidth = screenWidth - edgePadding,
            scrimColor = if (isFullScreen) Color.Transparent else BottomSheetDefaults.ScrimColor,
            containerColor = Color.White,
            shape = RoundedCornerShape(10.dp),
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            windowInsets = windowInsets,
            modifier = modifier
        ) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                PlaybackSpeedSelector(
                    currentSpeed = videoControlsState.speed,
                    onClick = {
                        currentSettingSelected = AvailableSettings.PlaybackSpeed
                    },
                    modifier = Modifier.testTag("PlaybackSpeedSelector")
                )
            }
        }
    }

    if (currentSettingSelected != null) {
        ModalBottomSheet(
            onDismissRequest = {
                currentSettingSelected = null
                onDismiss()
            },
            scrimColor = if (isFullScreen) Color.Transparent else BottomSheetDefaults.ScrimColor,
            containerColor = Color.White,
            sheetMaxWidth = screenWidth - edgePadding,
            shape = RoundedCornerShape(10.dp),
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            windowInsets = windowInsets,
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
    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        AvailablePlaybackSpeedValues.forEach { speed ->
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onVideoSpeedSelected(speed) }
                    .padding(16.dp)
            ) {
                Text(
                    text = if (speed == DefaultSpeed) {
                        stringResource(R.string.video_player_default_speed_name)
                    } else {
                        speed.toString()
                    },
                    modifier = Modifier.padding(start = 14.dp)
                )
                if (speed == currentSpeedSelected) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_check_24),
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = "CheckIcon"
                    )
                }
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