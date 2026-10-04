package dev.feuscel.lifeisshort

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class ScrollBlockerSettingsTest {
    @Test
    fun migratesLegacySettingsAndPersistsSourceSelections() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val preferences = context.getSharedPreferences(
            "scroll_blocker_settings",
            Context.MODE_PRIVATE
        )
        val previousValues = preferences.all

        assertTrue(preferences.edit().clear().commit())
        try {
            assertTrue(preferences.edit()
                .putBoolean("source_preferences_initialized", true)
                .putBoolean("blocking_enabled", false)
                .putBoolean("block_home_feed", true)
                .putBoolean("block_reels_tab", false)
                .putBoolean("block_direct_messages", true)
                .commit())

            val app = ScrollBlockerSettings.TargetApp.INSTAGRAM
            assertTrue(ScrollBlockerSettings.isAppEnabled(context, app))
            assertTrue(
                ScrollBlockerSettings.isSourceBlocked(
                    context,
                    app,
                    ScrollBlockerSettings.Source.HOME_FEED
                )
            )
            assertFalse(
                ScrollBlockerSettings.isSourceBlocked(
                    context,
                    app,
                    ScrollBlockerSettings.Source.REELS_TAB
                )
            )
            assertTrue(
                ScrollBlockerSettings.isSourceBlocked(
                    context,
                    app,
                    ScrollBlockerSettings.Source.DIRECT_MESSAGES
                )
            )

            ScrollBlockerSettings.setAppEnabled(context, app, false)
            assertFalse(ScrollBlockerSettings.isAppEnabled(context, app))
            ScrollBlockerSettings.setAppEnabled(context, app, true)
            ScrollBlockerSettings.setAllSourcesBlocked(context, app, true)
            assertTrue(ScrollBlockerSettings.areAllSourcesBlocked(context, app))

            ScrollBlockerSettings.setAllSourcesBlocked(context, app, false)
            assertFalse(ScrollBlockerSettings.areAllSourcesBlocked(context, app))
            ScrollBlockerSettings.Source.entries.forEach { source ->
                assertFalse(ScrollBlockerSettings.isSourceBlocked(context, app, source))
            }
        } finally {
            val restore = preferences.edit().clear()
            previousValues.forEach { (key, value) ->
                if (value is Boolean) restore.putBoolean(key, value)
            }
            assertTrue(restore.commit())
        }
    }
}
