package dev.feuscel.lifeisshort

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScrollBlockingPolicyTest {
    private val homeFeed = ScrollBlockerSettings.Source.HOME_FEED
    private val reelsTab = ScrollBlockerSettings.Source.REELS_TAB
    private val directMessages = ScrollBlockerSettings.Source.DIRECT_MESSAGES

    @Test
    fun blocksViewerOnlyWhenItsKnownOriginIsSelected() {
        val configuration = BlockingConfiguration(
            enabled = true,
            blockedSources = setOf(homeFeed, directMessages)
        )

        assertTrue(configuration.blocksViewer(ScreenContext.HOME_FEED))
        assertFalse(configuration.blocksViewer(ScreenContext.REELS_TAB))
        assertTrue(configuration.blocksViewer(ScreenContext.DIRECT_MESSAGES))
        assertFalse(configuration.blocksViewer(ScreenContext.OTHER))
        assertFalse(configuration.blocksViewer(ScreenContext.UNKNOWN))
    }

    @Test
    fun disabledConfigurationNeverBlocks() {
        val configuration = BlockingConfiguration(enabled = false, blockedSources = setOf(reelsTab))

        assertFalse(configuration.blocksSource(reelsTab))
        assertFalse(configuration.blocksViewer(ScreenContext.REELS_TAB))
    }

    @Test
    fun allSelectedSourcesAlsoBlockViewerWithUnknownOrigin() {
        val configuration = BlockingConfiguration(
            enabled = true,
            blockedSources = ScrollBlockerSettings.Source.entries.toSet()
        )

        assertTrue(configuration.blocksViewer(ScreenContext.UNKNOWN))
        assertTrue(configuration.blocksSource(reelsTab))
    }
}
