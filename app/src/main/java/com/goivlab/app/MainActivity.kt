package com.goivlab.app

import android.app.Activity
import android.content.Intent
import android.provider.Settings
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.WindowInsets
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import com.goivlab.iv.IvSpread
import com.goivlab.iv.PokemonStatsCatalog
import com.goivlab.iv.PvpIvLookup
import com.goivlab.iv.PvpIvLookupResult
import com.goivlab.nickname.NicknameData
import com.goivlab.nickname.NicknameFormatter
import com.goivlab.nickname.NicknameResult
import java.util.Locale
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

/**
 * Manual-IV proof of concept. All calculation and catalog data stay on-device.
 * This experimental branch adds manual floating results; screen recording and automatic Pokémon GO input are NOT implemented.
 */
class MainActivity : Activity() {
    private data class League(val label: String, val code: String, val cpCap: Int)
    private data class Calculation(
        val result: PvpIvLookupResult,
        val league: League,
        val ivs: IvSpread,
        val maxLevel: Double
    )

    private val leagues = listOf(
        League("Little · 500 CP", "LC", 500),
        League("Great · 1500 CP", "GL", 1500),
        League("Ultra · 2500 CP", "UL", 2500),
        League("Master · no CP cap", "ML", Int.MAX_VALUE)
    )
    private val bg = Color.rgb(244, 247, 252)
    private val ink = Color.rgb(24, 39, 64)
    private val muted = Color.rgb(92, 105, 126)
    private val blue = Color.rgb(44, 98, 206)
    private val worker = Executors.newSingleThreadExecutor()
    private val generation = AtomicInteger(0)
    private lateinit var prefs: SharedPreferences
    private lateinit var pokemonInput: AutoCompleteTextView
    private lateinit var leagueSpinner: Spinner
    private lateinit var maxLevelSpinner: Spinner
    private lateinit var attackBar: SeekBar
    private lateinit var defenseBar: SeekBar
    private lateinit var hpBar: SeekBar
    private lateinit var calculateButton: Button
    private lateinit var copyButton: Button
    private lateinit var showPopupButton: Button
    private lateinit var autoPopupCheck: CheckBox
    private lateinit var presetSpinner: Spinner
    private lateinit var presetStore: NicknamePresets
    private lateinit var templateInput: EditText
    private lateinit var statusText: TextView
    private lateinit var resultText: TextView
    private lateinit var nicknamePreview: TextView

    private var catalog: PokemonStatsCatalog? = null
    private var lookup: PvpIvLookup? = null
    private var selectedId: String? = null
    private var labelToId = emptyMap<String, String>()
    private var lastCalculation: Calculation? = null
    private var lastNickname: NicknameResult? = null
    private var pendingOverlayPermissionResult: Calculation? = null
    private var pendingOverlayNickname: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences("offline_go_iv_lab_v02", MODE_PRIVATE)
        presetStore = NicknamePresets(prefs)
        window.navigationBarColor = bg
        window.statusBarColor = bg

        val scroll = ScrollView(this).apply {
            setBackgroundColor(bg)
            isFillViewport = true
            clipToPadding = false
            setOnApplyWindowInsetsListener { view, insets ->
                val bars = insets.getInsets(WindowInsets.Type.systemBars())
                view.setPadding(0, bars.top, 0, bars.bottom)
                insets
            }
        }
        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18.dp(), 20.dp(), 18.dp(), 34.dp())
        }
        scroll.addView(column)
        setContentView(scroll)

        column.addView(text("GO IV LAB", 28f, true, ink))
        column.addView(text("Offline PvP IV checker · v0.4 floating overlay experiment", 13f, false, muted))
        spacer(column, 9)

        val searchCard = card(column)
        searchCard.addView(text("1 · Select a Pokémon or form", 17f, true, ink))
        spacer(searchCard, 4)
        pokemonInput = AutoCompleteTextView(this).apply {
            hint = "Search species, Alolan, Shadow…"
            threshold = 1
            setSingleLine(true)
            setTextColor(ink)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            contentDescription = "Pokémon species and form search"
        }
        searchCard.addView(pokemonInput, fullRow())
        searchCard.addView(text("Choose an exact entry from the suggestions. Forms are separate.", 12f, false, muted))
        pokemonInput.setOnClickListener { if (labelToId.isNotEmpty()) pokemonInput.showDropDown() }
        pokemonInput.onEdit {
            // The selected entry is retained only when the visible text still
            // exactly identifies that same species/form. No guessing on partial input.
            val typed = pokemonInput.text.toString()
            if (selectedId != null && labelToId[typed] != selectedId) {
                selectedId = null
            }
            invalidateResult("Species changed — choose a form and tap Calculate.")
        }
        pokemonInput.onItemClickListener = AdapterView.OnItemClickListener { parent, _, position, _ ->
            val entry = parent.getItemAtPosition(position)?.toString().orEmpty()
            selectedId = labelToId[entry]
            invalidateResult("Form selected — tap Calculate.")
        }

        spacer(column, 8)
        val ivCard = card(column)
        ivCard.addView(text("2 · Appraisal IVs", 17f, true, ink))
        ivCard.addView(text("Set each bar precisely from 0 to 15.", 12f, false, muted))
        spacer(ivCard, 4)
        attackBar = addIvControl(ivCard, "Attack", 0)
        defenseBar = addIvControl(ivCard, "Defense", 15)
        hpBar = addIvControl(ivCard, "HP", 15)
        val quickIvs = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        Button(this).apply {
            text = "0/15/15"
            contentDescription = "Set IVs to zero attack, fifteen defense and fifteen HP"
            setOnClickListener { setIvs(0, 15, 15) }
        }.also { quickIvs.addView(it, LinearLayout.LayoutParams(0, 44.dp(), 1f)) }
        Button(this).apply {
            text = "15/15/15"
            contentDescription = "Set all three IVs to fifteen"
            setOnClickListener { setIvs(15, 15, 15) }
        }.also { quickIvs.addView(it, LinearLayout.LayoutParams(0, 44.dp(), 1f)) }
        ivCard.addView(quickIvs, fullRow())

        spacer(column, 8)
        val leagueCard = card(column)
        leagueCard.addView(text("3 · League and maximum level", 17f, true, ink))
        spacer(leagueCard, 4)
        leagueSpinner = spinner(leagues.map { it.label }, 1)
        leagueCard.addView(leagueSpinner, fullRow())
        maxLevelSpinner = spinner(listOf("Level 50 (standard)", "Level 51 (Best Buddy active)"), 0)
        leagueCard.addView(maxLevelSpinner, fullRow())
        leagueCard.addView(text("Level 51 applies only while that Pokémon has an active Best Buddy boost. Cup eligibility is not checked.", 12f, false, muted))
        val selectionListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                invalidateResult("League or level changed — tap Calculate.")
            }
        }
        leagueSpinner.onItemSelectedListener = selectionListener
        maxLevelSpinner.onItemSelectedListener = selectionListener

        spacer(column, 8)
        val resultCard = card(column)
        calculateButton = Button(this).apply {
            text = "CALCULATE PVP IV RANK"
            isEnabled = false
            setOnClickListener { calculate() }
        }
        resultCard.addView(calculateButton, fullRow())
        spacer(resultCard, 7)
        statusText = text("Loading offline Pokémon database…", 13f, false, muted)
        resultCard.addView(statusText)
        spacer(resultCard, 5)
        resultText = text("Select a Pokémon and calculate to see its IV rank.", 16f, false, ink)
        resultCard.addView(resultText)
        showPopupButton = Button(this).apply {
            text = "SHOW RANK POPUP"
            isEnabled = false
            setOnClickListener { showRankPopup() }
        }
        resultCard.addView(showPopupButton, fullRow())
        val overlayQuickActions = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        Button(this).apply {
            text = "FLOAT RANK"
            contentDescription = "Float this calculated IV rank above Pokémon GO"
            setOnClickListener {
                val current = lastCalculation
                if (current == null) statusText.text = "Calculate an IV rank first."
                else startFloatingOverlay(current)
            }
        }.also { overlayQuickActions.addView(it, LinearLayout.LayoutParams(0, 49.dp(), 1f)) }
        Button(this).apply {
            text = "OPEN POKÉMON GO"
            contentDescription = "Open Pokémon GO after starting the floating result"
            setOnClickListener {
                val launch = packageManager.getLaunchIntentForPackage("com.nianticlabs.pokemongo")
                if (launch == null) {
                    statusText.text = "Pokémon GO is not available as a launchable installed app."
                } else {
                    try {
                        startActivity(launch)
                    } catch (exception: Exception) {
                        statusText.text = "Could not open Pokémon GO: ${exception.javaClass.simpleName}"
                    }
                }
            }
        }.also { overlayQuickActions.addView(it, LinearLayout.LayoutParams(0, 49.dp(), 1f)) }
        resultCard.addView(overlayQuickActions, fullRow())

        val overlayMaintenance = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        Button(this).apply {
            text = "STOP OVERLAY"
            contentDescription = "Stop the current floating IV result"
            setOnClickListener {
                stopService(Intent(this@MainActivity, ManualOverlayService::class.java))
                statusText.text = "Overlay stopped."
            }
        }.also { overlayMaintenance.addView(it, LinearLayout.LayoutParams(0, 49.dp(), 1f)) }
        Button(this).apply {
            text = "COPY DIAGNOSTICS"
            contentDescription = "Copy local overlay permission and lifecycle report"
            setOnClickListener {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(
                    ClipData.newPlainText("GO IV Lab overlay diagnostics", OverlayDiagnostics.report(this@MainActivity))
                )
                statusText.text = "Overlay diagnostics copied. No Pokémon GO account or location data included."
            }
        }.also { overlayMaintenance.addView(it, LinearLayout.LayoutParams(0, 49.dp(), 1f)) }
        resultCard.addView(overlayMaintenance, fullRow())
        autoPopupCheck = CheckBox(this).apply {
            text = "Automatically show results popup after calculation"
            setTextColor(ink)
            isChecked = prefs.getBoolean("auto_popup_v03", true)
            setOnCheckedChangeListener { _, checked ->
                prefs.edit().putBoolean("auto_popup_v03", checked).apply()
            }
        }
        resultCard.addView(autoPopupCheck, fullRow())

        spacer(column, 8)
        val nicknameCard = card(column)
        nicknameCard.addView(text("4 · Nickname maker", 17f, true, ink))
        nicknameCard.addView(text("Tokens: {name} {league} {rank} {attack} {defense} {hp}", 12f, false, muted))
        templateInput = EditText(this).apply {
            setSingleLine(true)
            hint = "{league}{rank}"
            setText(prefs.getString("nickname_template", "{league}{rank}") ?: "{league}{rank}")
            setTextColor(ink)
            contentDescription = "Nickname template"
        }
        nicknameCard.addView(templateInput, fullRow())
        templateInput.onEdit {
            prefs.edit().putString("nickname_template", templateInput.text.toString()).apply()
            renderNickname()
        }
        nicknamePreview = text("Calculate a rank to preview your nickname.", 16f, true, ink)
        nicknameCard.addView(nicknamePreview)
        copyButton = Button(this).apply {
            text = "COPY VALID NICKNAME"
            isEnabled = false
            setOnClickListener { copyNickname() }
        }
        nicknameCard.addView(copyButton, fullRow())
        spacer(nicknameCard, 9)
        nicknameCard.addView(text("Saved nickname templates (local only)", 13f, true, ink))
        presetSpinner = spinner(presetStore.all(), 0)
        nicknameCard.addView(presetSpinner, fullRow())
        Button(this).apply {
            text = "USE SELECTED PRESET"
            setOnClickListener {
                val selected = presetSpinner.selectedItem?.toString() ?: return@setOnClickListener
                templateInput.setText(selected)
                templateInput.setSelection(templateInput.text.length)
            }
        }.also { nicknameCard.addView(it, fullRow()) }
        val presetButtons = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val savePreset = Button(this).apply {
            text = "SAVE TEMPLATE"
            setOnClickListener {
                if (presetStore.save(templateInput.text.toString())) {
                    refreshPresets()
                    statusText.text = "Nickname preset saved on this device."
                } else statusText.text = "Template must contain 1–120 characters."
            }
        }
        val deletePreset = Button(this).apply {
            text = "DELETE PRESET"
            setOnClickListener {
                val selected = presetSpinner.selectedItem?.toString() ?: return@setOnClickListener
                presetStore.remove(selected)
                refreshPresets()
                statusText.text = "Selected nickname preset removed."
            }
        }
        presetButtons.addView(savePreset, LinearLayout.LayoutParams(0, 48.dp(), 1f))
        presetButtons.addView(deletePreset, LinearLayout.LayoutParams(0, 48.dp(), 1f))
        nicknameCard.addView(presetButtons, fullRow())

        spacer(column, 9)
        column.addView(text(
            "IV spread rank ≠ battle/meta ranking. PvPoke stats snapshot 26 Oct 2026. Manual floating overlay is experimental; screenshot scanning is not implemented.",
            12f, false, muted
        ))
        loadOfflineCatalog()
    }

    private fun loadOfflineCatalog() {
        worker.execute {
            try {
                val json = assets.open("pokemon-stats.json").bufferedReader(Charsets.UTF_8).use { it.readText() }
                val parsed = PokemonStatsCatalog.parse(json)
                val entries = parsedSpeciesOptions(parsed, json)
                runOnUiThread {
                    if (isDestroyed) return@runOnUiThread
                    catalog = parsed
                    lookup = PvpIvLookup(parsed)
                    labelToId = entries.associate { it.first to it.second }
                    pokemonInput.setAdapter(ArrayAdapter(
                        this, android.R.layout.simple_dropdown_item_1line, entries.map { it.first }
                    ))
                    val saved = prefs.getString("species_id", null)
                    val initiallySelected = entries.find { it.second == saved }
                        ?: entries.find { it.second == "bulbasaur" }
                    if (initiallySelected != null) {
                        pokemonInput.setText(initiallySelected.first, false)
                        selectedId = initiallySelected.second
                    }
                    calculateButton.isEnabled = true
                    statusText.text = "Ready · ${parsed.size} offline species/form entries"
                    resultText.text = "Starting example: Bulbasaur, 0/15/15. Edit any values, then Calculate."
                }
            } catch (exception: Exception) {
                runOnUiThread {
                    if (isDestroyed) return@runOnUiThread
                    statusText.text = "Unable to load offline data: ${exception.message ?: "unknown error"}"
                    calculateButton.isEnabled = false
                }
            }
        }
    }

    // The UI's visible form names come from the exact same pinned JSON as the stats,
    // never from separately maintained names or guessed regional forms.
    private fun parsedSpeciesOptions(parsed: PokemonStatsCatalog, json: String): List<Pair<String, String>> {
        val records = org.json.JSONArray(json)
        val options = ArrayList<Pair<String, String>>(records.length())
        for (index in 0 until records.length()) {
            val item = records.getJSONObject(index)
            val id = item.getString("speciesId")
            val name = parsed.requireSpecies(id).name
            options.add("$name  ·  $id" to id)
        }
        return options.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.first })
    }

    private fun calculate() {
        val raw = pokemonInput.text.toString().trim()
        val id = labelToId[raw] ?: catalog?.find(raw)?.speciesId
        if (id == null) {
            statusText.text = "Choose a Pokémon or exact form from the suggestions."
            return
        }
        val engine = lookup ?: return
        val ivs = IvSpread(attackBar.progress, defenseBar.progress, hpBar.progress)
        val league = leagues[leagueSpinner.selectedItemPosition]
        val maxLevel = if (maxLevelSpinner.selectedItemPosition == 1) 51.0 else 50.0
        val request = generation.incrementAndGet()
        prefs.edit().putString("species_id", id).apply()
        calculateButton.isEnabled = false
        statusText.text = "Calculating 4,096 IV combinations offline…"
        lastCalculation = null
        pendingOverlayPermissionResult = null
        pendingOverlayNickname = null
        stopService(Intent(this, ManualOverlayService::class.java))
        showPopupButton.isEnabled = false
        renderNickname()
        worker.execute {
            try {
                val result = engine.check(id, ivs, league.cpCap, maxLevel)
                runOnUiThread {
                    if (isDestroyed || generation.get() != request) return@runOnUiThread
                    lastCalculation = Calculation(result, league, ivs, maxLevel)
                    showPopupButton.isEnabled = true
                    calculateButton.isEnabled = true
                    statusText.text = "Calculated locally · no network requests"
                    resultText.text = buildString {
                        append(result.species.name)
                        append("\nIVs: ${ivs.attack}/${ivs.defense}/${ivs.stamina}")
                        append("\n${league.code} rank #${result.rank} of ${result.eligibleSpreads}")
                        append("\nAt level ${result.level}: CP ${result.cp}")
                        append("\nStat product: ${String.format(Locale.US, "%.2f", result.statProductPercent)}% of rank #1")
                        append("\n\nThis ranks this species' IV spreads, not its PvP battle performance.")
                    }
                    renderNickname()
                    if (autoPopupCheck.isChecked) showRankPopup()
                }
            } catch (exception: Exception) {
                runOnUiThread {
                    if (isDestroyed || generation.get() != request) return@runOnUiThread
                    calculateButton.isEnabled = true
                    statusText.text = "Could not calculate: ${exception.message ?: "unknown error"}"
                    resultText.text = "No rank was generated. Check your species and CP limit."
                }
            }
        }
    }

    private fun renderNickname() {
        if (!::nicknamePreview.isInitialized || !::copyButton.isInitialized) return
        val current = lastCalculation
        if (current == null) {
            lastNickname = null
            nicknamePreview.text = "Calculate a rank to preview your nickname."
            copyButton.isEnabled = false
            return
        }
        val r = current.result
        val nickname = NicknameFormatter.format(
            templateInput.text.toString(),
            NicknameData(
                name = r.species.name,
                league = current.league.code,
                rank = r.rank,
                attack = current.ivs.attack,
                defense = current.ivs.defense,
                hp = current.ivs.stamina
            )
        )
        lastNickname = nickname
        nicknamePreview.text = if (nickname.fits) {
            "${nickname.value} · ${nickname.length}/12 characters"
        } else {
            "${nickname.value} · ${nickname.length}/12 — too long; edit template"
        }
        copyButton.isEnabled = nickname.fits && nickname.value.isNotBlank()
    }

    private fun refreshPresets() {
        presetSpinner.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, presetStore.all()
        )
    }

    override fun onResume() {
        super.onResume()
        // Continue exactly one user-initiated FLOAT request after returning
        // from the Draw over other apps settings screen.
        val pending = pendingOverlayPermissionResult ?: return
        val nicknameSnapshot = pendingOverlayNickname
        pendingOverlayPermissionResult = null
        pendingOverlayNickname = null
        if (!::statusText.isInitialized) return
        if (lastCalculation !== pending) {
            statusText.text = "Result changed while permission was open — calculate again."
        } else if (Settings.canDrawOverlays(this)) {
            startFloatingOverlay(pending, nicknameSnapshot)
        } else {
            statusText.text = "Overlay not allowed. Select GO IV Lab Overlay Lab in Draw over other apps; in-app popup still works."
        }
    }

    private fun startFloatingOverlay(
        calculation: Calculation,
        nicknameSnapshot: String? = lastNickname?.takeIf { it.fits && it.value.isNotBlank() }?.value
    ) {
        // If an older dialog is still open, never float an obsolete calculation.
        if (lastCalculation !== calculation) {
            statusText.text = "Result changed — calculate again before floating."
            return
        }
        if (!Settings.canDrawOverlays(this)) {
            pendingOverlayPermissionResult = calculation
            pendingOverlayNickname = nicknameSnapshot
            OverlayDiagnostics.record(this, "Overlay permission requested in Android Settings")
            statusText.text = "In Draw over other apps, select GO IV Lab Overlay Lab and allow it; return here."
            try {
                // Android 11+ ignores package data and always shows the full app list.
                startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))
            } catch (exception: Exception) {
                pendingOverlayPermissionResult = null
                pendingOverlayNickname = null
                statusText.text = "Open Settings and allow GO IV Lab to draw over other apps."
            }
            return
        }
        val result = calculation.result
        val summary = OverlayResultFormatter.format(result, calculation.league.code)
        try {
            OverlayDiagnostics.record(this, "Overlay start requested from GO IV Lab")
            startForegroundService(Intent(this, ManualOverlayService::class.java).apply {
                action = ManualOverlayService.ACTION_SHOW
                putExtra(ManualOverlayService.EXTRA_RESULT, summary)
                nicknameSnapshot?.let {
                    putExtra(ManualOverlayService.EXTRA_NICKNAME, it)
                }
            })
            statusText.text = "Overlay start requested. You can minimize, drag or stop it."
        } catch (exception: Exception) {
            OverlayDiagnostics.record(this, "Overlay start request failed", exception.javaClass.simpleName)
            statusText.text = "Overlay could not start: ${exception.javaClass.simpleName}"
        }
    }

    private fun showRankPopup() {
        val current = lastCalculation ?: return
        val nickname = lastNickname?.takeIf { it.fits && it.value.isNotBlank() }?.value
        RankPopup.show(
            this, current.result, current.league.label, current.maxLevel, nickname,
            onFloat = { startFloatingOverlay(current, nickname) },
            onCopy = {
                // Copy the exact value displayed, not a later calculation.
                if (nickname != null) {
                    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("GO IV Lab nickname", nickname))
                    statusText.text = "Popup nickname copied. Paste it manually into Pokémon GO."
                }
            }
        )
    }

    private fun copyNickname() {
        val current = lastNickname ?: return
        if (!current.fits || current.value.isBlank()) return
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("GO IV Lab nickname", current.value))
        statusText.text = "Nickname copied. Paste it manually into Pokémon GO."
    }

    private fun invalidateResult(message: String) {
        generation.incrementAndGet()
        // Never leave a now-stale species/IV result floating over the game.
        if (lastCalculation != null) stopService(Intent(this, ManualOverlayService::class.java))
        pendingOverlayPermissionResult = null
        pendingOverlayNickname = null
        lastCalculation = null
        if (::showPopupButton.isInitialized) showPopupButton.isEnabled = false
        if (::calculateButton.isInitialized) calculateButton.isEnabled = lookup != null
        if (::statusText.isInitialized) statusText.text = message
        if (::resultText.isInitialized) resultText.text = "Tap Calculate to refresh the rank."
        renderNickname()
    }

    private fun setIvs(attack: Int, defense: Int, hp: Int) {
        attackBar.progress = attack
        defenseBar.progress = defense
        hpBar.progress = hp
        invalidateResult("IV preset applied — tap Calculate.")
    }

    private fun addIvControl(parent: LinearLayout, name: String, initial: Int): SeekBar {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val nameView = text(name, 14f, true, ink)
        row.addView(nameView, LinearLayout.LayoutParams(64.dp(), ViewGroup.LayoutParams.WRAP_CONTENT))
        val down = Button(this).apply {
            text = "−"
            minWidth = 0
            minimumWidth = 0
            setPadding(0, 0, 0, 0)
            contentDescription = "Decrease $name IV"
        }
        val up = Button(this).apply {
            text = "+"
            minWidth = 0
            minimumWidth = 0
            setPadding(0, 0, 0, 0)
            contentDescription = "Increase $name IV"
        }
        val valueText = text(initial.toString(), 14f, true, blue).apply { gravity = Gravity.CENTER }
        val bar = SeekBar(this).apply {
            max = 15
            progress = initial
            contentDescription = "$name IV, from zero to fifteen"
        }
        row.addView(down, LinearLayout.LayoutParams(31.dp(), 40.dp()))
        row.addView(bar, LinearLayout.LayoutParams(0, 44.dp(), 1f))
        row.addView(up, LinearLayout.LayoutParams(31.dp(), 40.dp()))
        row.addView(valueText, LinearLayout.LayoutParams(29.dp(), 40.dp()))
        down.setOnClickListener { bar.progress = (bar.progress - 1).coerceAtLeast(0) }
        up.setOnClickListener { bar.progress = (bar.progress + 1).coerceAtMost(15) }
        bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                valueText.text = progress.toString()
                invalidateResult("$name IV set to $progress — tap Calculate.")
            }
        })
        parent.addView(row, fullRow())
        return bar
    }

    private fun spinner(values: List<String>, selection: Int): Spinner {
        return Spinner(this).apply {
            adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, values)
            setSelection(selection)
        }
    }

    private fun card(parent: LinearLayout): LinearLayout {
        val view = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(12.dp(), 10.dp(), 12.dp(), 10.dp())
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = 16.dp().toFloat()
            }
            elevation = 2.dp().toFloat()
        }
        parent.addView(view, fullRow())
        return view
    }

    private fun text(value: String, size: Float, bold: Boolean, color: Int): TextView {
        return TextView(this).apply {
            text = value
            setTextSize(TypedValue.COMPLEX_UNIT_SP, size)
            setTextColor(color)
            if (bold) setTypeface(typeface, Typeface.BOLD)
        }
    }

    private fun spacer(parent: LinearLayout, height: Int) {
        parent.addView(View(this), LinearLayout.LayoutParams(1, height.dp()))
    }

    private fun fullRow() = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
    )

    private fun Int.dp(): Int = (this * resources.displayMetrics.density + 0.5f).toInt()

    private fun EditText.onEdit(action: () -> Unit) {
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                action()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    override fun onDestroy() {
        generation.incrementAndGet()
        worker.shutdownNow()
        super.onDestroy()
    }
}
