package com.goivlab.app

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.goivlab.iv.PvpIvLookupResult
import java.util.Locale

/** Native, in-app result dialog; does not request Android overlay permission. */
internal object RankPopup {
    fun show(
        context: Context,
        result: PvpIvLookupResult,
        league: String,
        maxLevel: Double,
        nickname: String?,
        onFloat: (() -> Unit)? = null,
        onCopy: () -> Unit
    ): AlertDialog {
        val density = context.resources.displayMetrics.density
        fun dp(v: Int) = (v * density + 0.5f).toInt()
        val ink = Color.rgb(24, 39, 64)
        val muted = Color.rgb(93, 108, 130)
        val primary = Color.rgb(44, 98, 206)
        val content = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(18), dp(22), dp(14))
        }
        fun line(value: String, size: Float, color: Int = ink, bold: Boolean = false): TextView {
            return TextView(context).apply {
                text = value
                textSize = size
                setTextColor(color)
                if (bold) setTypeface(typeface, Typeface.BOLD)
                setPadding(0, dp(3), 0, dp(3))
            }.also { content.addView(it) }
        }
        line(result.species.name + " · " + result.species.speciesId, 19f, ink, true)
        line("$league · Level cap ${maxLevel.toInt()}", 13f, muted)
        line("RANK #${result.rank}", 32f, primary, true)
        line("of ${result.eligibleSpreads} IV combinations", 13f, muted)
        val ivs = result.requestedIvs
        line("IVs  ${ivs.attack} / ${ivs.defense} / ${ivs.stamina}", 17f, ink, true)
        line("CP ${result.cp}  ·  Level ${result.level}", 16f)
        line("Stat product ${String.format(Locale.US, "%.2f", result.statProductPercent)}% of #1", 14f)
        if (!nickname.isNullOrBlank()) {
            line("Nickname  $nickname", 17f, primary, true)
            Button(context).apply {
                text = "COPY NICKNAME"
                setOnClickListener { onCopy() }
            }.also { content.addView(it) }
        }
        line("IV spread rank only — not a PvPoke battle/meta rating.", 12f, muted)
        val dialog = AlertDialog.Builder(context)
            .setView(ScrollView(context).apply { addView(content) })
            .setPositiveButton("DONE", null)
            .apply { if (onFloat != null) setNeutralButton("FLOAT", { _, _ -> onFloat() }) }
            .create()
        dialog.show()
        dialog.window?.setBackgroundDrawable(
            GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dp(18).toFloat()
            }
        )
        dialog.window?.setLayout(
            (context.resources.displayMetrics.widthPixels * .92f).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        return dialog
    }
}
