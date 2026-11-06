package io.github.jaffa944099.oneapp

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri

internal fun Context.openTask(task: PilotTask): Boolean {
    val url = task.url.toUri()
    val browserPackage = CustomTabsClient.getPackageName(this, emptyList())
    if (browserPackage != null) {
        val tab = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()
        tab.intent.setPackage(browserPackage)
        try {
            tab.launchUrl(this, url)
            return true
        } catch (_: ActivityNotFoundException) {
            // The browser may have been disabled or removed after resolution.
        }
    }

    return try {
        startActivity(Intent(Intent.ACTION_VIEW, url).addCategory(Intent.CATEGORY_BROWSABLE))
        true
    } catch (_: ActivityNotFoundException) {
        false
    }
}
