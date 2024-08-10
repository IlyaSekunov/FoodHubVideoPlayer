package ru.ilyasekunov.foodhubvideoplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import ru.ilyasekunov.foodhubvideoplayer.ui.player.FoodHubVideoPlayer
import ru.ilyasekunov.foodhubvideoplayer.ui.player.VideoUiState
import ru.ilyasekunov.foodhubvideoplayer.ui.theme.FoodHubVideoPlayerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            navigationBarStyle = SystemBarStyle.auto(
                lightScrim = android.graphics.Color.TRANSPARENT,
                darkScrim = android.graphics.Color.TRANSPARENT
            )
        )

        setContent {
            FoodHubVideoPlayerTheme(dynamicColor = false) {
                FoodHubVideoPlayer(
                    videos = listOf(
                        VideoUiState(
                            url = "https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4",
                            title = "Some video"
                        ),
                        VideoUiState(
                            url = "https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4",
                            title = "Some video"
                        )
                    ),
                    initiallyStartPlaying = true,
                    autoRepeat = false,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}