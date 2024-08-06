package ru.ilyasekunov.videoplayerexample

import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import ru.ilyasekunov.videoplayerexample.ui.player.FoodHubVideoPlayer
import ru.ilyasekunov.videoplayerexample.ui.player.PLAYER_CONTROLS_VISIBILITY_TIME
import ru.ilyasekunov.videoplayerexample.ui.player.Video
import ru.ilyasekunov.videoplayerexample.util.setLandscape

class FoodHubVideoPlayerTest {
    @get:Rule
    val activityRule = createAndroidComposeRule<MainActivity>()

    private val video = listOf(
        Video(
            url = "https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4",
            title = "Some video"
        )
    )

    private val videos = listOf(
        Video(
            url = "https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4",
            title = "Some video"
        ),
        Video(
            url = "https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4",
            title = "Some video"
        )
    )


    @Test
    fun whenVideoSupplied_videoPlayerIsShownOnScreen() {
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
    fun whenUserClickOnPlayerWithSingleVideoPortrait_videoPlayerControlsAreShown() {
        activityRule.activity.setContent {
            FoodHubVideoPlayer(
                videos = video,
                initiallyStartPlaying = false,
                autoRepeat = false
            )
        }

        activityRule.onNodeWithTag("VideoPlayerControls").performClick()

        activityRule.mainClock.autoAdvance = false
        activityRule.mainClock.advanceTimeBy(PLAYER_CONTROLS_VISIBILITY_TIME / 2)

        assertDefaultVideoPlayerControlsAreShown()

        activityRule.onNodeWithTag("VideoPlayerPreviousButton").assertIsNotDisplayed()

        activityRule.onNodeWithTag("VideoPlayerNextButton").assertIsNotDisplayed()

        activityRule.onNodeWithTag("VideoPlayerNavigateBackButton").assertIsNotDisplayed()
    }

    @Test
    fun whenUserClickOnPlayerWithSingleVideoLandscape_videoPlayerControlsAreShown() {
        setLandscape(activityRule.activity)

        activityRule.activity.setContent {
            FoodHubVideoPlayer(
                videos = video,
                initiallyStartPlaying = false,
                autoRepeat = false
            )
        }

        activityRule.onNodeWithTag("VideoPlayerControls").performClick()

        activityRule.mainClock.autoAdvance = false
        activityRule.mainClock.advanceTimeBy(PLAYER_CONTROLS_VISIBILITY_TIME / 2)

        assertDefaultVideoPlayerControlsAreShown()

        activityRule.onNodeWithTag("VideoPlayerPreviousButton").assertIsNotDisplayed()

        activityRule.onNodeWithTag("VideoPlayerNextButton").assertIsNotDisplayed()

        activityRule.onNodeWithTag("VideoPlayerNavigateBackButton")
            .assertIsDisplayed()
            .assertIsEnabled()
    }

    @Test
    fun whenUserClicksOnPlayerWithMultipleVideosPortrait_videoPlayerControlsAreShown() {
        activityRule.activity.setContent {
            FoodHubVideoPlayer(
                videos = videos,
                initiallyStartPlaying = false,
                autoRepeat = false
            )
        }

        activityRule.onNodeWithTag("VideoPlayerControls").performClick()

        activityRule.mainClock.autoAdvance = false
        activityRule.mainClock.advanceTimeBy(PLAYER_CONTROLS_VISIBILITY_TIME / 2)

        assertDefaultVideoPlayerControlsAreShown()

        activityRule.onNodeWithTag("VideoPlayerPreviousButton")
            .assertIsDisplayed()
            .assertIsNotEnabled()

        activityRule.onNodeWithTag("VideoPlayerNextButton")
            .assertIsDisplayed()
            .assertIsEnabled()

        activityRule.onNodeWithTag("VideoPlayerNavigateBackButton").assertIsNotDisplayed()
    }

    @Test
    fun whenUserClicksOnPlayerWithMultipleVideosLandscape_videoPlayerControlsAreShown() {
        setLandscape(activityRule.activity)

        activityRule.activity.setContent {
            FoodHubVideoPlayer(
                videos = videos,
                initiallyStartPlaying = false,
                autoRepeat = false
            )
        }

        activityRule.onNodeWithTag("VideoPlayerControls").performClick()

        activityRule.mainClock.autoAdvance = false
        activityRule.mainClock.advanceTimeBy(PLAYER_CONTROLS_VISIBILITY_TIME / 2)

        assertDefaultVideoPlayerControlsAreShown()

        activityRule.onNodeWithTag("VideoPlayerPreviousButton")
            .assertIsDisplayed()
            .assertIsNotEnabled()

        activityRule.onNodeWithTag("VideoPlayerNextButton")
            .assertIsDisplayed()
            .assertIsEnabled()

        activityRule.onNodeWithTag("VideoPlayerNavigateBackButton")
            .assertIsDisplayed()
            .assertIsEnabled()
    }

    @Test
    fun whenUserClicksOnVideoPlayerControlsAndItsVisible_VideoPlayerControlsAreHidden() {
        activityRule.activity.setContent {
            FoodHubVideoPlayer(
                videos = videos,
                initiallyStartPlaying = false,
                autoRepeat = false
            )
        }

        activityRule.mainClock.autoAdvance = false

        activityRule.onNodeWithTag("VideoPlayerControls").performClick()

        activityRule.mainClock.advanceTimeBy(PLAYER_CONTROLS_VISIBILITY_TIME / 2)

        activityRule.onNodeWithTag("VideoPlayerControls").performClick()

        assertDefaultVideoPlayerControlsAreNotShown()
    }

    @Test
    fun whenUserIsNotInteractingWithPlayer_videoPlayerControlsAreHidden() {
        activityRule.activity.setContent {
            FoodHubVideoPlayer(
                videos = videos,
                initiallyStartPlaying = false,
                autoRepeat = false
            )
        }

        activityRule.onNodeWithTag("VideoPlayerControls").performClick()

        activityRule.mainClock.advanceTimeBy(PLAYER_CONTROLS_VISIBILITY_TIME * 2)

        assertDefaultVideoPlayerControlsAreNotShown()
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
        activityRule.onNodeWithTag("VideoPlayerVideoTitle").assertIsNotDisplayed()

        activityRule.onNodeWithTag("VideoPlayerPlayButton").assertIsNotDisplayed()

        activityRule.onNodeWithTag("VideoPlayerPauseButton").assertIsNotDisplayed()

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
    }
}