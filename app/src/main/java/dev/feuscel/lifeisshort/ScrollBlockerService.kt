package dev.feuscel.lifeisshort

import android.accessibilityservice.AccessibilityService
import android.content.pm.ApplicationInfo
import android.os.SystemClock
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/** Blocks Reels only when their selected source is enabled in LifeIsShort. */
class ScrollBlockerService : AccessibilityService() {

    private var lastStableContext = ScreenContext.UNKNOWN
    private var viewerBackPending = false
    private var reelsTabRedirectPending = false
    private var lastHomeTabClickAt = 0L
    private var lastSourceDiagnosticAt = 0L
    private var lastSourceDiagnosticContext = ScreenContext.UNKNOWN

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return

        if (event.packageName?.toString() != INSTAGRAM_PACKAGE) {
            if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                resetRedirectState()
                lastStableContext = ScreenContext.UNKNOWN
            }
            return
        }

        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED,
            AccessibilityEvent.TYPE_VIEW_SELECTED,
            AccessibilityEvent.TYPE_VIEW_CLICKED -> Unit
            else -> return
        }

        val configuration = ScrollBlockerSettings.blockingConfiguration(
            this,
            ScrollBlockerSettings.TargetApp.INSTAGRAM
        )
        if (!configuration.enabled) {
            resetRedirectState()
            return
        }

        val root = rootInActiveWindow ?: return
        try {
            val state = inspectInstagramWindow(root)
            val now = SystemClock.uptimeMillis()

            if (!state.reelsViewerVisible) {
                when {
                    state.screenContext != ScreenContext.UNKNOWN -> {
                        lastStableContext = state.screenContext
                    }
                    event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                        // An unrecognized screen transition must not inherit a stale source.
                        lastStableContext = ScreenContext.OTHER
                    }
                }
            }

            // The navigation-tab option is distinct from individual Reel viewers.
            if (state.reelsTabSelected) {
                viewerBackPending = false
                if (!configuration.blocksSource(ScrollBlockerSettings.Source.REELS_TAB)) {
                    reelsTabRedirectPending = false
                    return
                }

                reelsTabRedirectPending = true
                if (now - lastHomeTabClickAt < HOME_TAB_RETRY_INTERVAL_MS) return
                lastHomeTabClickAt = now
                if (clickHomeTab(root)) {
                    Log.i(TAG, "Blocked the Reels tab; returned to Instagram Home")
                } else {
                    Log.w(TAG, "Could not click Instagram Home while Reels tab was selected")
                }
                return
            }

            if (state.reelsViewerVisible) {
                if (viewerBackPending) return

                val origin = when (lastStableContext) {
                    ScreenContext.HOME_FEED,
                    ScreenContext.REELS_TAB,
                    ScreenContext.DIRECT_MESSAGES -> lastStableContext
                    ScreenContext.OTHER,
                    ScreenContext.UNKNOWN -> state.viewerOrigin.takeIf {
                        it != ScreenContext.UNKNOWN
                    } ?: lastStableContext
                }
                if (!configuration.blocksViewer(origin)) return

                // For individual Reels, one Back action preserves the source screen (feed or
                // discussion) instead of forcing every user to Instagram Home.
                viewerBackPending = true
                if (performGlobalAction(GLOBAL_ACTION_BACK)) {
                    Log.i(TAG, "Blocked a Reel from $origin; returned to its source")
                } else {
                    viewerBackPending = false
                    Log.w(TAG, "Android rejected Back while blocking a Reel")
                }
                return
            }

            if (viewerBackPending) {
                viewerBackPending = false
                return
            }

            if (reelsTabRedirectPending) {
                if (state.homeTabSelected) {
                    resetRedirectState()
                } else if (state.homeTabVisible &&
                    now - lastHomeTabClickAt >= HOME_TAB_RETRY_INTERVAL_MS
                ) {
                    lastHomeTabClickAt = now
                    clickHomeTab(root)
                }
            }
        } finally {
            root.recycle()
        }
    }

    /** Reads visible tab/view IDs and DM markers; never reads or logs message text. */
    private fun inspectInstagramWindow(root: AccessibilityNodeInfo): InstagramWindowState {
        var visitedNodes = 0
        var reelsViewerVisible = false
        var reelsTabSelected = false
        var homeTabSelected = false
        var directTabSelected = false
        var otherTabSelected = false
        var directMessageMarkerVisible = false
        val visibleResourceIds = mutableListOf<String>()

        fun visit(node: AccessibilityNodeInfo, selectedAncestor: Boolean) {
            if (visitedNodes++ >= MAX_VISITED_NODES) return

            val selectedInPath = selectedAncestor || node.isSelected
            val fullId = node.viewIdResourceName.orEmpty()
            val id = fullId.substringAfterLast('/')
            val normalizedId = id.lowercase()

            if (node.isVisibleToUser) {
                if (id == REELS_VIEWER_ID) reelsViewerVisible = true
                if (id == REELS_TAB_ID && selectedInPath) reelsTabSelected = true
                if (id == HOME_TAB_ID && selectedInPath) homeTabSelected = true
                if (id in DIRECT_TAB_IDS && selectedInPath) directTabSelected = true
                if (selectedInPath && id.endsWith("_tab") &&
                    id !in setOf(REELS_TAB_ID, HOME_TAB_ID) && id !in DIRECT_TAB_IDS
                ) {
                    otherTabSelected = true
                }
                if (DIRECT_MESSAGE_ID_MARKERS.any(normalizedId::contains)) {
                    directMessageMarkerVisible = true
                }
                if (visibleResourceIds.size < MAX_DIAGNOSTIC_IDS &&
                    (normalizedId.contains("direct") || normalizedId.contains("thread") ||
                        normalizedId.contains("message") || normalizedId.contains("inbox") ||
                        id == HOME_TAB_ID || id == REELS_TAB_ID)
                ) {
                    visibleResourceIds += id
                }
            }

            for (index in 0 until node.childCount) {
                if (visitedNodes >= MAX_VISITED_NODES) break
                val child = node.getChild(index) ?: continue
                try {
                    visit(child, selectedInPath)
                } finally {
                    child.recycle()
                }
            }
        }

        visit(root, selectedAncestor = false)

        val context = when {
            reelsTabSelected -> ScreenContext.REELS_TAB
            directMessageMarkerVisible || directTabSelected -> ScreenContext.DIRECT_MESSAGES
            homeTabSelected -> ScreenContext.HOME_FEED
            otherTabSelected -> ScreenContext.OTHER
            else -> ScreenContext.UNKNOWN
        }
        val viewerOrigin = when {
            reelsTabSelected -> ScreenContext.REELS_TAB
            directMessageMarkerVisible || directTabSelected -> ScreenContext.DIRECT_MESSAGES
            homeTabSelected -> ScreenContext.HOME_FEED
            otherTabSelected -> ScreenContext.OTHER
            else -> ScreenContext.UNKNOWN
        }

        logSourceDiagnostics(viewerOrigin, reelsViewerVisible, visibleResourceIds)
        return InstagramWindowState(
            reelsViewerVisible = reelsViewerVisible,
            reelsTabSelected = reelsTabSelected,
            homeTabVisible = visibleResourceIds.contains(HOME_TAB_ID),
            homeTabSelected = homeTabSelected,
            screenContext = context,
            viewerOrigin = viewerOrigin
        )
    }

    /** Clicks the visible feed_tab node, or the nearest visible clickable ancestor. */
    private fun clickHomeTab(root: AccessibilityNodeInfo): Boolean {
        var visitedNodes = 0

        fun clickNodeOrClickableAncestor(node: AccessibilityNodeInfo): Boolean {
            if (node.isVisibleToUser && node.isClickable &&
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            ) {
                return true
            }

            var ancestor = node.parent
            repeat(MAX_CLICK_ANCESTORS) {
                val current = ancestor ?: return false
                if (current.isVisibleToUser && current.isClickable &&
                    current.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                ) {
                    current.recycle()
                    return true
                }
                val next = current.parent
                current.recycle()
                ancestor = next
            }
            ancestor?.recycle()
            return false
        }

        fun visit(node: AccessibilityNodeInfo): Boolean {
            if (visitedNodes++ >= MAX_VISITED_NODES) return false
            val id = node.viewIdResourceName.orEmpty().substringAfterLast('/')
            if (id == HOME_TAB_ID && clickNodeOrClickableAncestor(node)) return true

            for (index in 0 until node.childCount) {
                if (visitedNodes >= MAX_VISITED_NODES) break
                val child = node.getChild(index) ?: continue
                val clicked = try {
                    visit(child)
                } finally {
                    child.recycle()
                }
                if (clicked) return true
            }
            return false
        }

        return visit(root)
    }

    /** Logs structural hints only in debug builds to help tune DM detection on-device. */
    private fun logSourceDiagnostics(
        context: ScreenContext,
        reelsViewerVisible: Boolean,
        visibleIds: List<String>
    ) {
        if (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE == 0) return
        val now = SystemClock.uptimeMillis()
        if (context == lastSourceDiagnosticContext &&
            now - lastSourceDiagnosticAt < SOURCE_DIAGNOSTIC_INTERVAL_MS
        ) {
            return
        }
        lastSourceDiagnosticAt = now
        lastSourceDiagnosticContext = context
        Log.d(
            TAG,
            "sourceContext=$context reelsViewer=$reelsViewerVisible visibleSourceIds=$visibleIds"
        )
    }

    private fun resetRedirectState() {
        viewerBackPending = false
        reelsTabRedirectPending = false
        lastHomeTabClickAt = 0L
    }

    override fun onInterrupt() = Unit

    private data class InstagramWindowState(
        val reelsViewerVisible: Boolean,
        val reelsTabSelected: Boolean,
        val homeTabVisible: Boolean,
        val homeTabSelected: Boolean,
        val screenContext: ScreenContext,
        val viewerOrigin: ScreenContext
    )

    companion object {
        private const val TAG = "ScrollBlockerService"
        private const val INSTAGRAM_PACKAGE = "com.instagram.android"
        private const val REELS_VIEWER_ID = "clips_viewer_view_pager"
        private const val REELS_TAB_ID = "clips_tab"
        private const val HOME_TAB_ID = "feed_tab"
        private const val MAX_VISITED_NODES = 1_500
        private const val MAX_DIAGNOSTIC_IDS = 20
        private const val MAX_CLICK_ANCESTORS = 8
        private const val HOME_TAB_RETRY_INTERVAL_MS = 500L
        private const val SOURCE_DIAGNOSTIC_INTERVAL_MS = 1_000L
        private val DIRECT_TAB_IDS = setOf("direct_tab", "inbox_tab")
        private val DIRECT_MESSAGE_ID_MARKERS = setOf(
            "direct_thread",
            "thread_message",
            "message_composer",
            "direct_inbox",
            "inbox_refreshable_thread_list_recyclerview"
        )
    }
}
