package ru.ilyasekunov.foodhubvideoplayer

import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import ru.ilyasekunov.foodhubvideoplayer.ui.player.FoodHubVideoPlayer
import ru.ilyasekunov.foodhubvideoplayer.ui.player.PLAYER_CONTROLS_VISIBILITY_TIME
import ru.ilyasekunov.foodhubvideoplayer.ui.player.VideoUiState
import ru.ilyasekunov.foodhubvideoplayer.util.setLandscape

class FoodHubVideoPlayerTest {
    @get:Rule
    val activityRule = createAndroidComposeRule<MainActivity>()

    private val video = listOf(
        VideoUiState(
            url = "https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4",
            title = "Some video"
        )
    )

    private val videos = listOf(
        VideoUiState(
            url = "https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4",
            title = "Some video"
        ),
        VideoUiState(
            url = "https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4",
            title = "Some video"
        )
    )


    @Test
    fun `check video player is shown`() {
        activityRule.activity.setContent {
            FoodHubVideoPlayer(
                videos = video,
                initiallyStartPlaying = false,
                autoRepeat = false
            )
        }

        activityRule.onNodeWithTag("VideoPlayer").assertIsDisplayed()
    }

    @Test
    fun `when user in portrait mode clicks on video player with single video proper controls are shown`() {
        activityRule.activity.setContent {
            FoodHubVideoPlayer(
                videos = video,
                initiallyStartPlaying = false,
                autoRepeat = false
            )
        }

        openVideoControls()

        assertDefaultVideoPlayerControlsAreShown()
        assertSettingsButtonIsVisibleAndEnabled()
        assertMultipleVideosControlsAreNotShown()
        assertLandscapeControlsAreNotShown()
    }

    @Test
    fun `when user in landscape mode clicks on video player with single video proper controls are shown`() {
        setLandscape(activityRule.activity)

        activityRule.activity.setContent {
            FoodHubVideoPlayer(
                videos = video,
                initiallyStartPlaying = false,
                autoRepeat = false
            )
        }

        openVideoControls()

        assertDefaultVideoPlayerControlsAreShown()
        assertMultipleVideosControlsAreNotShown()
        assertLandscapeControlsAreShown()
    }

    @Test
    fun `when user in portrait mode clicks on video player with multiple videos proper controls are shown`() {
        activityRule.activity.setContent {
            FoodHubVideoPlayer(
                videos = videos,
                initiallyStartPlaying = false,
                autoRepeat = false
            )
        }

        openVideoControls()

        assertDefaultVideoPlayerControlsAreShown()
        assertSettingsButtonIsVisibleAndEnabled()
        assertMultipleVideosControlsAreShown()
        assertLandscapeControlsAreNotShown()
    }

    @Test
    fun `when user in landscape mode clicks on video player with multiple videos proper controls are shown`() {
        setLandscape(activityRule.activity)

        activityRule.activity.setContent {
            FoodHubVideoPlayer(
                videos = videos,
                initiallyStartPlaying = false,
                autoRepeat = false
            )
        }

        openVideoControls()

        assertDefaultVideoPlayerControlsAreShown()
        assertMultipleVideosControlsAreShown()
        assertLandscapeControlsAreShown()
    }

    @Test
    fun `when user clicks on video player and controls are already visible controls are hidden`() {
        activityRule.activity.setContent {
            FoodHubVideoPlayer(
                videos = videos,
                initiallyStartPlaying = false,
                autoRepeat = false
            )
        }

        openVideoControls()

        activityRule.onNodeWithTag("VideoPlayerControls").performClick()
        activityRule.mainClock.advanceTimeBy(PLAYER_CONTROLS_VISIBILITY_TIME)

        assertDefaultVideoPlayerControlsAreNotShown()
    }

    @Test
    fun `when user clicks on video player and does not interact with them controls are hidden`() {
        activityRule.activity.setContent {
            FoodHubVideoPlayer(
                videos = videos,
                initiallyStartPlaying = false,
                autoRepeat = false
            )
        }

        openVideoControls()

        activityRule.mainClock.advanceTimeBy(PLAYER_CONTROLS_VISIBILITY_TIME)

        assertDefaultVideoPlayerControlsAreNotShown()
    }

    @Test
    fun `when user clicks on settings button settings are visible`() {
        activityRule.activity.setContent {
            FoodHubVideoPlayer(
                videos = videos,
                initiallyStartPlaying = false,
                autoRepeat = false
            )
        }

        openVideoControls()
        openSettings()

        activityRule.onNodeWithTag("VideoPlayerSettings")
            .assertIsDisplayed()
    }

    @Test
    fun `when user clicks on playback speed settings available settings for speed are shown`() {
        activityRule.activity.setContent {
            FoodHubVideoPlayer(
                videos = videos,
                initiallyStartPlaying = false,
                autoRepeat = false
            )
        }

        openVideoControls()
        openSettings()

        activityRule.onNodeWithTag("PlaybackSpeedSelector").performClick()

        activityRule.onNodeWithTag("AvailablePlaybackSpeedValues")
            .assertIsDisplayed()
    }

    @Test
    fun `when user drags after long press video player speed accelerates`() {
        activityRule.activity.setContent {
            FoodHubVideoPlayer(
                videos = videos,
                initiallyStartPlaying = false,
                autoRepeat = false
            )
        }

        activityRule.onNodeWithTag("VideoPlayerControls")
            .performTouchInput {
                longClick(durationMillis = 2000)
            }

        activityRule.mainClock.autoAdvance = false

        activityRule.onNodeWithTag("VideoPlayerCurrentSpeedHeader")
            .assertIsDisplayed()
    }

    private fun openVideoControls() {
        activityRule.onNodeWithTag("VideoPlayerControls").performClick()
        activityRule.mainClock.advanceTimeBy(PLAYER_CONTROLS_VISIBILITY_TIME / 2)
    }

    private fun openSettings() {
        activityRule.onNodeWithTag("VideoPlayerSettingButton").performClick()
    }

    private fun assertSettingsButtonIsVisibleAndEnabled() {
        activityRule.onNodeWithTag("VideoPlayerSettingButton")
            .assertIsDisplayed()
            .assertIsEnabled()
    }

    private fun assertDefaultVideoPlayerControlsAreShown() {
        activityRule.onNodeWithTag("VideoPlayerVideoTitle").assertIsDisplayed()

        val playButtonDisplayed =
            activityRule.onNodeWithTag("VideoPlayerPlayButton").isDisplayed()
        val pauseButtonDisplayed =
            activityRule.onNodeWithTag("VideoPlayerPauseButton").isDisplayed()
        val loadingIndicatorDisplayed =
            activityRule.onNodeWithTag("VideoPlayerLoadingIndicator").isDisplayed()

        assertTrue(
            (playButtonDisplayed && !pauseButtonDisplayed && !loadingIndicatorDisplayed) ||
                    (!playButtonDisplayed && pauseButtonDisplayed && !loadingIndicatorDisplayed) ||
                    (!playButtonDisplayed && !pauseButtonDisplayed && loadingIndicatorDisplayed)
        )

        activityRule.onNodeWithTag("VideoPlayerTimeAndBufferingView")
            .assertIsDisplayed()

        activityRule.onNodeWithTag("VideoPlayerFullScreenButton")
            .assertIsDisplayed()
            .assertIsEnabled()

        activityRule.onNodeWithTag("VideoPlayerCurrentTimeAndTotalDuration")
            .assertIsDisplayed()
    }

    private fun assertDefaultVideoPlayerControlsAreNotShown() {
        activityRule.onNodeWithTag("VideoPlayerVideoTitle")
            .assertIsNotDisplayed()

        activityRule.onNodeWithTag("VideoPlayerPlayButton")
            .assertIsNotDisplayed()

        activityRule.onNodeWithTag("VideoPlayerPauseButton")
            .assertIsNotDisplayed()

        activityRule.onNodeWithTag("VideoPlayerTimeAndBufferingView")
            .assertIsNotDisplayed()

        activityRule.onNodeWithTag("VideoPlayerCurrentTimeAndTotalDuration")
            .assertIsNotDisplayed()

        activityRule.onNodeWithTag("VideoPlayerFullScreenButton")
            .assertIsNotDisplayed()

        activityRule.onNodeWithTag("VideoPlayerPreviousButton")
            .assertIsNotDisplayed()

        activityRule.onNodeWithTag("VideoPlayerNextButton")
            .assertIsNotDisplayed()

        activityRule.onNodeWithTag("VideoPlayerNavigateBackButton")
            .assertIsNotDisplayed()

        activityRule.onNodeWithTag("VideoPlayerSettingButton")
            .assertIsNotDisplayed()
    }

    private fun assertMultipleVideosControlsAreShown() {
        activityRule.onNodeWithTag("VideoPlayerPreviousButton")
            .assertIsDisplayed()
            .assertIsNotEnabled()

        activityRule.onNodeWithTag("VideoPlayerNextButton")
            .assertIsDisplayed()
            .assertIsEnabled()
    }

    private fun assertMultipleVideosControlsAreNotShown() {
        activityRule.onNodeWithTag("VideoPlayerPreviousButton")
            .assertIsNotDisplayed()

        activityRule.onNodeWithTag("VideoPlayerNextButton")
            .assertIsNotDisplayed()
    }

    private fun assertLandscapeControlsAreShown() {
        activityRule.onNodeWithTag("VideoPlayerNavigateBackButton")
            .assertIsDisplayed()
            .assertIsEnabled()
    }

    private fun assertLandscapeControlsAreNotShown() {
        activityRule.onNodeWithTag("VideoPlayerNavigateBackButton")
            .assertIsNotDisplayed()
    }
}