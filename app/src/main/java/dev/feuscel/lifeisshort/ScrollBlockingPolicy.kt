package dev.feuscel.lifeisshort

/** Instagram screen context remembered while navigating between screens. */
internal enum class ScreenContext {
    HOME_FEED,
    REELS_TAB,
    DIRECT_MESSAGES,
    OTHER,
    UNKNOWN
}

/** Snapshot of the settings that determine whether a screen should be blocked. */
internal data class BlockingConfiguration(
    val enabled: Boolean,
    val blockedSources: Set<ScrollBlockerSettings.Source>
) {
    fun blocksSource(source: ScrollBlockerSettings.Source): Boolean =
        enabled && (blocksAllSources() || source in blockedSources)

    fun blocksViewer(origin: ScreenContext): Boolean {
        if (!enabled) return false
        if (blocksAllSources()) return true

        val source = when (origin) {
            ScreenContext.HOME_FEED -> ScrollBlockerSettings.Source.HOME_FEED
            ScreenContext.REELS_TAB -> ScrollBlockerSettings.Source.REELS_TAB
            ScreenContext.DIRECT_MESSAGES -> ScrollBlockerSettings.Source.DIRECT_MESSAGES
            ScreenContext.OTHER,
            ScreenContext.UNKNOWN -> return false
        }
        return source in blockedSources
    }

    private fun blocksAllSources() =
        blockedSources.size == ScrollBlockerSettings.Source.entries.size
}
