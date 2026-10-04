package dev.feuscel.lifeisshort

import android.content.Context

/** Per-app master and source preferences shared by the UI and accessibility service. */
object ScrollBlockerSettings {
    enum class TargetApp(val packageName: String, val settingsKey: String) {
        INSTAGRAM("com.instagram.android", "instagram")
    }

    enum class Source(val settingsKey: String) {
        HOME_FEED("home_feed"),
        REELS_TAB("reels_tab"),
        DIRECT_MESSAGES("direct_messages")
    }

    private const val PREFERENCES_NAME = "scroll_blocker_settings"
    private const val INSTAGRAM_SETTINGS_INITIALIZED = "instagram_settings_initialized"

    fun isAppEnabled(context: Context, app: TargetApp): Boolean {
        initializeAppPreferences(context, app)
        return preferences(context).getBoolean(appKey(app, "enabled"), false)
    }

    fun setAppEnabled(context: Context, app: TargetApp, enabled: Boolean) {
        initializeAppPreferences(context, app)
        preferences(context).edit().putBoolean(appKey(app, "enabled"), enabled).apply()
    }

    fun isSourceBlocked(context: Context, app: TargetApp, source: Source): Boolean {
        initializeAppPreferences(context, app)
        return preferences(context).getBoolean(sourceKey(app, source), false)
    }

    fun setSourceBlocked(context: Context, app: TargetApp, source: Source, blocked: Boolean) {
        initializeAppPreferences(context, app)
        preferences(context).edit().putBoolean(sourceKey(app, source), blocked).apply()
    }

    fun areAllSourcesBlocked(context: Context, app: TargetApp): Boolean =
        Source.entries.all { isSourceBlocked(context, app, it) }

    fun setAllSourcesBlocked(context: Context, app: TargetApp, blocked: Boolean) {
        initializeAppPreferences(context, app)
        preferences(context).edit().apply {
            Source.entries.forEach { putBoolean(sourceKey(app, it), blocked) }
        }.apply()
    }

    internal fun blockingConfiguration(context: Context, app: TargetApp): BlockingConfiguration {
        initializeAppPreferences(context, app)
        val prefs = preferences(context)
        return BlockingConfiguration(
            enabled = prefs.getBoolean(appKey(app, "enabled"), false),
            blockedSources = Source.entries.filterTo(mutableSetOf()) {
                prefs.getBoolean(sourceKey(app, it), false)
            }
        )
    }

    /** Migrate the existing Instagram source choices without changing their behavior. */
    private fun initializeAppPreferences(context: Context, app: TargetApp) {
        if (app != TargetApp.INSTAGRAM) return
        val prefs = preferences(context)
        if (prefs.getBoolean(INSTAGRAM_SETTINGS_INITIALIZED, false)) return

        synchronized(this) {
            if (prefs.getBoolean(INSTAGRAM_SETTINGS_INITIALIZED, false)) return

            val migrated = migrateInstagramSettings(prefs.all)

            prefs.edit()
                .putBoolean(appKey(TargetApp.INSTAGRAM, "enabled"), migrated.enabled)
                .putBoolean(
                    sourceKey(TargetApp.INSTAGRAM, Source.HOME_FEED),
                    migrated.homeFeedBlocked
                )
                .putBoolean(
                    sourceKey(TargetApp.INSTAGRAM, Source.REELS_TAB),
                    migrated.reelsTabBlocked
                )
                .putBoolean(
                    sourceKey(TargetApp.INSTAGRAM, Source.DIRECT_MESSAGES),
                    migrated.directMessagesBlocked
                )
                .putBoolean(INSTAGRAM_SETTINGS_INITIALIZED, true)
                .apply()
        }
    }

    private fun appKey(app: TargetApp, key: String) = "${app.settingsKey}.$key"

    private fun sourceKey(app: TargetApp, source: Source) = appKey(app, source.settingsKey)

    private fun preferences(context: Context) =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
}

internal data class MigratedInstagramSettings(
    val enabled: Boolean,
    val homeFeedBlocked: Boolean,
    val reelsTabBlocked: Boolean,
    val directMessagesBlocked: Boolean
)

/** Pure migration mapping so legacy defaults and selections can be tested on the JVM. */
internal fun migrateInstagramSettings(existing: Map<String, *>): MigratedInstagramSettings {
    fun boolean(key: String, default: Boolean) = existing[key] as? Boolean ?: default

    val oldSourcesInitialized = boolean("source_preferences_initialized", false)
    val legacyBlockingEnabled = boolean("blocking_enabled", true)
    return MigratedInstagramSettings(
        enabled = if (oldSourcesInitialized) true else legacyBlockingEnabled,
        homeFeedBlocked = if (oldSourcesInitialized) {
            boolean("block_home_feed", false)
        } else {
            false
        },
        reelsTabBlocked = if (oldSourcesInitialized) {
            boolean("block_reels_tab", legacyBlockingEnabled)
        } else {
            legacyBlockingEnabled
        },
        directMessagesBlocked = if (oldSourcesInitialized) {
            boolean("block_direct_messages", false)
        } else {
            false
        }
    )
}
