package com.goivlab.app

import android.content.Context
import android.content.Intent
import android.os.ParcelFileDescriptor
import android.os.SystemClock
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Emulator smoke tests, NOT evidence of Pokémon GO or Samsung visibility.
 * No screenshots, external accounts, accessibility service or network access.
 */
@RunWith(AndroidJUnit4::class)
class PopupAndOverlaySmokeTest {
    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private val device get() = UiDevice.getInstance(instrumentation)
    private val app get() = instrumentation.targetContext

    @Test fun calculatedIvRankOpensInAppPopup() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            calculateBulbasaur(scenario)
            assertTrue(
                "Expected offline Bulbasaur 0/15/15 GL rank #770 popup",
                device.wait(Until.hasObject(By.text("RANK #770")), 120_000)
            )
            assertTrue("Popup FLOAT action missing", device.hasObject(By.text("FLOAT")))
            assertTrue("Popup DONE action missing", device.hasObject(By.text("DONE")))
            device.findObject(By.text("DONE")).click()
        }
    }

    @Test fun manuallyStartedOverlayAttachesOverAndroidHome() {
        grantOwnOverlayPermission()
        assertTrue("Overlay permission was not granted", Settings.canDrawOverlays(app))
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            try {
                calculateBulbasaur(scenario)
                assertTrue("In-app popup did not show", device.wait(
                    Until.hasObject(By.text("RANK #770")), 120_000
                ))
                device.findObject(By.text("FLOAT")).click()
                assertTrue("Overlay window attachment not recorded", waitFor(15_000) {
                    app.getSharedPreferences("go_iv_lab_overlay_diagnostics", Context.MODE_PRIVATE)
                        .getString("last_event", "") == "Overlay window attached"
                })
                device.pressHome()
                assertTrue(
                    "Rank card not observed above Android home (not a Pokémon GO test)",
                    device.wait(Until.hasObject(By.textContains("Rank #770/4096")), 12_000)
                )
            } finally {
                app.stopService(Intent(app, ManualOverlayService::class.java))
            }
        }
    }

    private fun calculateBulbasaur(scenario: ActivityScenario<MainActivity>) {
        assertTrue("Offline app did not finish loading", waitFor(30_000) {
            var ready = false
            scenario.onActivity { activity ->
                ready = findButton(activity.window.decorView, "CALCULATE PVP IV RANK")?.isEnabled == true
            }
            ready
        })
        scenario.onActivity { activity ->
            val button = findButton(activity.window.decorView, "CALCULATE PVP IV RANK")
                ?: error("Calculate button missing")
            button.performClick()
        }
    }

    private fun grantOwnOverlayPermission() {
        val cmd = "appops set ${app.packageName} SYSTEM_ALERT_WINDOW allow"
        val descriptor = instrumentation.uiAutomation.executeShellCommand(cmd)
        ParcelFileDescriptor.AutoCloseInputStream(descriptor).use { it.readBytes() }
        assertTrue("Emulator did not apply own overlay AppOp", waitFor(5_000) {
            Settings.canDrawOverlays(app)
        })
    }

    private fun findButton(view: View, text: String): Button? {
        if (view is Button && view.text.toString() == text) return view
        if (view is ViewGroup) {
            for (index in 0 until view.childCount) {
                findButton(view.getChildAt(index), text)?.let { return it }
            }
        }
        return null
    }

    private fun waitFor(timeoutMs: Long, condition: () -> Boolean): Boolean {
        val deadline = SystemClock.elapsedRealtime() + timeoutMs
        while (SystemClock.elapsedRealtime() < deadline) {
            if (condition()) return true
            SystemClock.sleep(200)
        }
        return condition()
    }
}
