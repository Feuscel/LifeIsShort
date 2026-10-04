package dev.feuscel.lifeisshort

import android.content.ComponentName
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.MaterialColors
import com.google.android.material.materialswitch.MaterialSwitch

class MainActivity : AppCompatActivity() {

    private lateinit var accessibilityStatus: TextView
    private lateinit var instagramEnabledSwitch: MaterialSwitch
    private lateinit var instagramOptionsContainer: ViewGroup
    private lateinit var homeFeedSwitch: MaterialSwitch
    private lateinit var reelsTabSwitch: MaterialSwitch
    private lateinit var directMessagesSwitch: MaterialSwitch
    private lateinit var homeFeedStatus: TextView
    private lateinit var reelsTabStatus: TextView
    private lateinit var directMessagesStatus: TextView
    private lateinit var allSourcesButton: MaterialButton
    private lateinit var sourceSummary: TextView
    private var updatingSourceControls = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        accessibilityStatus = findViewById(R.id.accessibilityStatus)
        instagramEnabledSwitch = findViewById(R.id.instagramEnabledSwitch)
        instagramOptionsContainer = findViewById(R.id.instagramOptionsContainer)
        homeFeedSwitch = findViewById(R.id.homeFeedSwitch)
        reelsTabSwitch = findViewById(R.id.reelsTabSwitch)
        directMessagesSwitch = findViewById(R.id.directMessagesSwitch)
        homeFeedStatus = findViewById(R.id.homeFeedStatus)
        reelsTabStatus = findViewById(R.id.reelsTabStatus)
        directMessagesStatus = findViewById(R.id.directMessagesStatus)
        allSourcesButton = findViewById(R.id.allSourcesButton)
        allSourcesButton.isCheckable = true
        sourceSummary = findViewById(R.id.sourceSummary)

        refreshSourceControls()
        instagramEnabledSwitch.setOnCheckedChangeListener { _, enabled ->
            if (!updatingSourceControls) {
                ScrollBlockerSettings.setAppEnabled(
                    this,
                    ScrollBlockerSettings.TargetApp.INSTAGRAM,
                    enabled
                )
                instagramOptionsContainer.visibility = if (enabled) View.VISIBLE else View.GONE
            }
        }
        homeFeedSwitch.setOnCheckedChangeListener { _, blocked ->
            updateSource(ScrollBlockerSettings.Source.HOME_FEED, blocked)
        }
        reelsTabSwitch.setOnCheckedChangeListener { _, blocked ->
            updateSource(ScrollBlockerSettings.Source.REELS_TAB, blocked)
        }
        directMessagesSwitch.setOnCheckedChangeListener { _, blocked ->
            updateSource(ScrollBlockerSettings.Source.DIRECT_MESSAGES, blocked)
        }
        allSourcesButton.setOnClickListener {
            val app = ScrollBlockerSettings.TargetApp.INSTAGRAM
            val blockAll = !ScrollBlockerSettings.areAllSourcesBlocked(this, app)
            ScrollBlockerSettings.setAllSourcesBlocked(this, app, blockAll)
            refreshSourceControls()
        }

        findViewById<Button>(R.id.accessibilitySettingsButton).setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        refreshPermissionStatus()
    }

    override fun onResume() {
        super.onResume()
        if (::accessibilityStatus.isInitialized) {
            refreshPermissionStatus()
            refreshSourceControls()
        }
    }

    private fun refreshPermissionStatus() {
        val accessibilityEnabled = isScrollBlockerAccessibilityEnabled()
        accessibilityStatus.setText(
            if (accessibilityEnabled) R.string.accessibility_permission_enabled
            else R.string.accessibility_permission_disabled
        )
        findViewById<Button>(R.id.accessibilitySettingsButton).visibility =
            if (accessibilityEnabled) View.GONE else View.VISIBLE
    }

    private fun updateSource(source: ScrollBlockerSettings.Source, blocked: Boolean) {
        if (updatingSourceControls) return
        ScrollBlockerSettings.setSourceBlocked(
            this,
            ScrollBlockerSettings.TargetApp.INSTAGRAM,
            source,
            blocked
        )
        refreshSourceControls()
    }

    private fun refreshSourceControls() {
        if (!::allSourcesButton.isInitialized) return
        updatingSourceControls = true
        try {
            val app = ScrollBlockerSettings.TargetApp.INSTAGRAM
            val appEnabled = ScrollBlockerSettings.isAppEnabled(this, app)
            instagramEnabledSwitch.isChecked = appEnabled
            instagramOptionsContainer.visibility = if (appEnabled) View.VISIBLE else View.GONE

            val homeBlocked = ScrollBlockerSettings.isSourceBlocked(
                this,
                app,
                ScrollBlockerSettings.Source.HOME_FEED
            )
            val reelsTabBlocked = ScrollBlockerSettings.isSourceBlocked(
                this,
                app,
                ScrollBlockerSettings.Source.REELS_TAB
            )
            val messagesBlocked = ScrollBlockerSettings.isSourceBlocked(
                this,
                app,
                ScrollBlockerSettings.Source.DIRECT_MESSAGES
            )
            homeFeedSwitch.isChecked = homeBlocked
            reelsTabSwitch.isChecked = reelsTabBlocked
            directMessagesSwitch.isChecked = messagesBlocked
            updateSourceStatus(
                homeFeedSwitch,
                homeFeedStatus,
                homeBlocked,
                R.string.home_feed_title
            )
            updateSourceStatus(
                reelsTabSwitch,
                reelsTabStatus,
                reelsTabBlocked,
                R.string.reels_tab_title
            )
            updateSourceStatus(
                directMessagesSwitch,
                directMessagesStatus,
                messagesBlocked,
                R.string.direct_messages_title
            )

            val selectedCount = listOf(homeBlocked, reelsTabBlocked, messagesBlocked).count { it }
            sourceSummary.text = resources.getQuantityString(
                R.plurals.source_selection_summary,
                selectedCount,
                selectedCount,
                ScrollBlockerSettings.Source.entries.size
            )
            val allBlocked = ScrollBlockerSettings.areAllSourcesBlocked(this, app)
            allSourcesButton.isChecked = allBlocked
            allSourcesButton.setText(
                if (allBlocked) R.string.unblock_all_sources
                else R.string.block_all_sources
            )
        } finally {
            updatingSourceControls = false
        }
    }

    /** Make the switch state clear by color and an explicit, high-contrast text label. */
    private fun updateSourceStatus(
        sourceSwitch: MaterialSwitch,
        statusView: TextView,
        blocked: Boolean,
        titleResource: Int
    ) {
        val statusResource = if (blocked) R.string.source_status_blocked
        else R.string.source_status_allowed
        val backgroundColor = MaterialColors.getColor(
            statusView,
            if (blocked) com.google.android.material.R.attr.colorPrimary
            else com.google.android.material.R.attr.colorSurface
        )
        val foregroundColor = MaterialColors.getColor(
            statusView,
            if (blocked) com.google.android.material.R.attr.colorOnPrimary
            else com.google.android.material.R.attr.colorOnSurfaceVariant
        )

        statusView.setText(statusResource)
        statusView.backgroundTintList = ColorStateList.valueOf(backgroundColor)
        statusView.setTextColor(foregroundColor)
        sourceSwitch.contentDescription = getString(
            R.string.source_switch_accessibility,
            getString(titleResource),
            getString(statusResource)
        )
    }

    /** Check this service specifically, rather than relying on another app's service. */
    private fun isScrollBlockerAccessibilityEnabled(): Boolean {
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        val expectedComponent = ComponentName(this, ScrollBlockerService::class.java)
        val expectedShortName = expectedComponent.flattenToString()
        return enabledServices.split(':')
            .any { it.equals(expectedShortName, ignoreCase = true) }
    }
}
