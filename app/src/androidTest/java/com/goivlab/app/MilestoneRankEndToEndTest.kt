package com.goivlab.app

import android.os.SystemClock
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.CheckBox
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
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
 * SLOW milestone test: run the real 4,096-combination offline ranker through the
 * Android activity before requesting a device install. Not part of routine
 * emulator smoke tests, and not a Samsung/Pokémon GO compatibility test.
 */
@RunWith(AndroidJUnit4::class)
class MilestoneRankEndToEndTest {
    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private val device get() = UiDevice.getInstance(instrumentation)

    @Test fun actualBulbasaurRankReachesPopupAndCanBeReopened() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            assertTrue("Offline catalog did not load", waitFor(60_000) {
                var ready = false
                scenario.onActivity {
                    ready = buttons(it.window.decorView).any { b ->
                        b.text.toString() == "CALCULATE PVP IV RANK" && b.isEnabled
                    }
                }
                ready
            })
            scenario.onActivity { activity ->
                val root = activity.window.decorView
                // Explicit inputs prevent a prior testing preference from changing results.
                val species = allViews(root).filterIsInstance<AutoCompleteTextView>().first()
                species.setText("Bulbasaur  ·  bulbasaur", false)
                val ivs = allViews(root).filterIsInstance<SeekBar>()
                check(ivs.size >= 3) { "Expected three IV sliders" }
                ivs[0].progress = 0
                ivs[1].progress = 15
                ivs[2].progress = 15
                val spinners = allViews(root).filterIsInstance<Spinner>()
                check(spinners.size >= 2) { "Expected league and level spinners" }
                spinners[0].setSelection(1) // Great
                spinners[1].setSelection(0) // Level 50
                allViews(root).filterIsInstance<CheckBox>()
                    .first { it.text.toString().startsWith("Automatically show") }
                    .isChecked = true
                buttons(root).first { it.text.toString() == "CALCULATE PVP IV RANK" }
                    .performClick()
            }
            // On a slower emulated CPU, exact-score enumeration can be expensive.
            val dialogFound = device.wait(Until.hasObject(By.text("RANK #770")), 360_000)
            if (!dialogFound) {
                var debugText = "No visible app state"
                scenario.onActivity { activity ->
                    debugText = allViews(activity.window.decorView)
                        .filterIsInstance<TextView>()
                        .map { it.text.toString() }
                        .filter { it.contains("Calculating") || it.contains("Could not") ||
                            it.contains("rank #") || it.contains("Ready") }
                        .take(8).joinToString(" | ").take(500)
                }
                throw AssertionError("Real Bulbasaur rank popup did not show: $debugText")
            }
            assertTrue("FLOAT button missing on the rank popup",
                device.hasObject(By.text("FLOAT")))
            device.findObject(By.text("DONE")).click()
            scenario.onActivity { activity ->
                val open = buttons(activity.window.decorView)
                    .first { it.text.toString() == "SHOW RANK POPUP" }
                check(open.isEnabled) { "Manual result popup disabled after calculation" }
                open.performClick()
            }
            assertTrue("Result popup could not reopen", device.wait(
                Until.hasObject(By.text("RANK #770")), 12_000
            ))
        }
    }

    private fun buttons(root: View): List<Button> = allViews(root).filterIsInstance<Button>()

    private fun allViews(root: View): List<View> {
        val result = ArrayList<View>()
        fun collect(view: View) {
            result.add(view)
            if (view is ViewGroup) for (i in 0 until view.childCount) collect(view.getChildAt(i))
        }
        collect(root)
        return result
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
