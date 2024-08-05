package ru.ilyasekunov.videoplayerexample.ui

import androidx.compose.ui.Modifier

fun Modifier.ifTrue(condition: Boolean, block: Modifier.() -> Modifier): Modifier =
    if (condition) {
        this.block()
    } else {
        this
    }