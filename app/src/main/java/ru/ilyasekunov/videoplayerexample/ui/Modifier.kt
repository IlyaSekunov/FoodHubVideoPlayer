package ru.ilyasekunov.videoplayerexample.ui

import androidx.compose.ui.Modifier

fun Modifier.ifTrue(condition: Boolean, block: Modifier.() -> Modifier): Modifier =
    if (condition) {
        this.block()
    } else {
        this
    }

fun Modifier.conditional(
    condition: Boolean,
    trueBlock: Modifier.() -> Modifier,
    falseBlock: Modifier.() -> Modifier,
): Modifier = if (condition) {
    this.trueBlock()
} else {
    this.falseBlock()
}