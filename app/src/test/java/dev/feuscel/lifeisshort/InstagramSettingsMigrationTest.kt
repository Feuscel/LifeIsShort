package dev.feuscel.lifeisshort

import org.junit.Assert.assertEquals
import org.junit.Test

class InstagramSettingsMigrationTest {
    @Test
    fun freshInstallKeepsLegacyDefaultBlockingEnabledForReelsTabOnly() {
        assertEquals(
            MigratedInstagramSettings(
                enabled = true,
                homeFeedBlocked = false,
                reelsTabBlocked = true,
                directMessagesBlocked = false
            ),
            migrateInstagramSettings(emptyMap<String, Any>())
        )
    }

    @Test
    fun migratesExistingSourceSelectionsAndEnablesTheApp() {
        assertEquals(
            MigratedInstagramSettings(
                enabled = true,
                homeFeedBlocked = true,
                reelsTabBlocked = false,
                directMessagesBlocked = true
            ),
            migrateInstagramSettings(
                mapOf(
                    "source_preferences_initialized" to true,
                    "blocking_enabled" to false,
                    "block_home_feed" to true,
                    "block_reels_tab" to false,
                    "block_direct_messages" to true
                )
            )
        )
    }

    @Test
    fun migratesLegacyMasterSwitchWhenSourceSettingsDidNotExist() {
        assertEquals(
            MigratedInstagramSettings(
                enabled = false,
                homeFeedBlocked = false,
                reelsTabBlocked = false,
                directMessagesBlocked = false
            ),
            migrateInstagramSettings(mapOf("blocking_enabled" to false))
        )
    }
}
