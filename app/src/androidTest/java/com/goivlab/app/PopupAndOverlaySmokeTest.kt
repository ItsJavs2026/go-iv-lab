package com.goivlab.app

import android.content.Context
import android.content.Intent
import android.os.ParcelFileDescriptor
import android.os.SystemClock
import android.provider.Settings
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.goivlab.iv.BaseStats
import com.goivlab.iv.IvSpread
import com.goivlab.iv.PokemonSpecies
import com.goivlab.iv.PvpIvLookupResult
import java.util.concurrent.atomic.AtomicBoolean
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Fast Android UI smoke tests. Use a fixed example result here to test actual
 * window, popup and touch controls WITHOUT the heavy 4096-spread ranker.
 * Independent ranker unit tests cover real math; the milestone instrumentation
 * suite checks their end-to-end integration with the Android UI.
 * This emulator cannot establish behavior in Pokémon GO or Samsung One UI.
 */
@RunWith(AndroidJUnit4::class)
class PopupAndOverlaySmokeTest {
    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private val device get() = UiDevice.getInstance(instrumentation)
    private val app get() = instrumentation.targetContext

    private val example = PvpIvLookupResult(
        PokemonSpecies("bulbasaur", "Bulbasaur", BaseStats(118, 111, 128)),
        IvSpread(0, 15, 15),
        770, 4096, 50.0, 1118, 88.72
    )

    @Test fun inAppPopupShowsRankFloatAndCopyControls() {
        val copied = AtomicBoolean(false)
        val floated = AtomicBoolean(false)
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                RankPopup.show(
                    activity, example, "Great · 1500 CP", 50.0, "GL770",
                    onFloat = { floated.set(true) },
                    onCopy = { copied.set(true) }
                )
            }
            assertTrue("In-app dialog did not render rank", device.wait(
                Until.hasObject(By.text("RANK #770")), 15_000
            ))
            assertTrue("FLOAT action missing", device.hasObject(By.text("FLOAT")))
            device.findObject(By.text("COPY NICKNAME")).click()
            assertTrue("Nickname copy callback not invoked after click",
                waitFor(5_000) { copied.get() })
            device.findObject(By.text("FLOAT")).click()
            assertTrue("FLOAT callback not invoked after click",
                waitFor(5_000) { floated.get() })
        }
    }

    @Test fun manualOverlayAttachesAndRendersAboveAndroidHome() {
        grantOwnOverlayPermission()
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            try {
                scenario.onActivity { activity ->
                    activity.startForegroundService(
                        Intent(activity, ManualOverlayService::class.java).apply {
                            action = ManualOverlayService.ACTION_SHOW
                            putExtra(ManualOverlayService.EXTRA_RESULT,
                                OverlayResultFormatter.format(example, "GL"))
                            putExtra(ManualOverlayService.EXTRA_NICKNAME, "GL770")
                        }
                    )
                }
                assertTrue("WindowManager overlay was not attached; see local diagnostics",
                    waitFor(20_000) {
                        app.getSharedPreferences("go_iv_lab_overlay_diagnostics", Context.MODE_PRIVATE)
                            .getString("last_event", "") == "Overlay window attached"
                    })
                device.pressHome()
                assertTrue("Overlay rank missing above emulator HOME screen",
                    device.wait(Until.hasObject(By.textContains("Rank #770/4096")), 15_000))
                assertTrue("Overlay close control missing",
                    device.hasObject(By.desc("Close floating result")))
                device.findObject(By.desc("Close floating result")).click()
                assertTrue("Overlay did not close on user action", waitFor(8_000) {
                    !device.hasObject(By.desc("Close floating result"))
                })
            } finally {
                app.stopService(Intent(app, ManualOverlayService::class.java))
            }
        }
    }

    private fun grantOwnOverlayPermission() {
        val fd = instrumentation.uiAutomation.executeShellCommand(
            "appops set ${app.packageName} SYSTEM_ALERT_WINDOW allow"
        )
        ParcelFileDescriptor.AutoCloseInputStream(fd).use { it.readBytes() }
        assertTrue("Emulator did not grant Overlay Lab its own overlay AppOp", waitFor(5_000) {
            Settings.canDrawOverlays(app)
        })
    }

    private fun waitFor(timeoutMs: Long, condition: () -> Boolean): Boolean {
        val until = SystemClock.elapsedRealtime() + timeoutMs
        while (SystemClock.elapsedRealtime() < until) {
            if (condition()) return true
            SystemClock.sleep(200)
        }
        return condition()
    }
}
