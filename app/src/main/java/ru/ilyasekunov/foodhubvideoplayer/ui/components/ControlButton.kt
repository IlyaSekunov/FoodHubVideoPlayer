package ru.ilyasekunov.foodhubvideoplayer.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.ilyasekunov.foodhubvideoplayer.ui.ifTrue

internal data class ControlButtonColors(
    val disabledContainer: Color,
    val enabledContainer: Color,
    val disabledContent: Color,
    val enabledContent: Color,
)

internal object ControlButtonDefaults {
    @Composable
    fun colors(
        disabledContainer: Color = Color.Black.copy(alpha = 0.3f),
        enabledContainer: Color = Color.Black.copy(alpha = 0.5f),
        disabledContent: Color = Color.White.copy(alpha = 0.6f),
        enabledContent: Color = Color.White,
    ) = ControlButtonColors(
        disabledContainer = disabledContainer,
        enabledContainer = enabledContainer,
        disabledContent = disabledContent,
        enabledContent = enabledContent
    )
}

@Composable
internal fun ControlButton(
    enabled: Boolean,
    onClick: () -> Unit,
    @DrawableRes drawableId: Int,
    modifier: Modifier = Modifier,
    colors: ControlButtonColors = ControlButtonDefaults.colors(),
    iconSize: Dp = 24.dp,
    withBackground: Boolean = true,
    padding: Dp = iconSize / 4,
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .ifTrue(withBackground) {
                background(
                    color = if (enabled) colors.enabledContainer else colors.disabledContainer,
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
            tint = if (enabled) colors.enabledContent else colors.disabledContent,
            modifier = Modifier.size(iconSize)
        )
    }
}