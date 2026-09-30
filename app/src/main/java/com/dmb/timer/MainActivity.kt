package com.dmb.timer

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max
import kotlin.math.min

class MainActivity : android.app.Activity() {
    private val prefs by lazy { getSharedPreferences("dmb", Context.MODE_PRIVATE) }
    private val handler = Handler(Looper.getMainLooper())
    private var lastBackground = -1
    private lateinit var root: FrameLayout
    private lateinit var progress: ProgressBar
    private lateinit var percent: TextView
    private lateinit var days: TextView
    private lateinit var nameView: TextView
    private lateinit var datesView: TextView

    private val bgIds = intArrayOf(
        R.drawable.bg_01, R.drawable.bg_02, R.drawable.bg_03,
        R.drawable.bg_04, R.drawable.bg_05, R.drawable.bg_06
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        if (!prefs.contains("start")) showSetup() else showHome()
    }

    private fun showSetup() {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(52, 60, 52, 50)
            gravity = Gravity.CENTER_HORIZONTAL
            setBackgroundColor(0xFF080808.toInt())
        }

        val title = tv("DMB.", 52f).apply { gravity = Gravity.CENTER }
        val subtitle = tv("НАСТРОЙКА СЛУЖБЫ", 13f).apply { alpha = .65f }
        val name = EditText(this).apply {
            hint = "ИМЯ"
            textSize = 18f
            setTextColor(0xFFFFFFFF.toInt())
            setHintTextColor(0xFF777777.toInt())
            setSingleLine()
        }
        val start = Button(this).apply { text = "ДАТА ПРИЗЫВА" }
        val end = Button(this).apply { text = "ДАТА ДЕМБЕЛЯ" }
        val go = Button(this).apply { text = "НАЧАТЬ СЛУЖБУ" }

        var startCal = Calendar.getInstance()
        var endCal = Calendar.getInstance()
        start.setOnClickListener { pick(start, startCal) }
        end.setOnClickListener { pick(end, endCal) }
        go.setOnClickListener {
            if (name.text.isNullOrBlank()) { name.error = "Введите имя"; return@setOnClickListener }
            val s = startCal.timeInMillis
            val e = endCal.timeInMillis
            if (e <= s) { Toast.makeText(this, "Дата дембеля должна быть позже даты призыва", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            prefs.edit().putString("name", name.text.toString().trim()).putLong("start", s).putLong("end", e).apply()
            showHome()
        }

        box.addView(title, lp())
        box.addView(subtitle, lp(0, 16, 0, 40))
        box.addView(name, lp())
        box.addView(start, lp(0, 18, 0, 0))
        box.addView(end, lp(0, 12, 0, 0))
        box.addView(go, lp(0, 38, 0, 0))
        setContentView(box)
    }

    private fun pick(button: Button, cal: Calendar) {
        DatePickerDialog(this, { _, y, m, d ->
            cal.set(y, m, d, 0, 0, 0); cal.set(Calendar.MILLISECOND, 0)
            button.text = String.format("%02d.%02d.%04d", d, m + 1, y)
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun showHome() {
        root = FrameLayout(this)
        setContentView(root)
        val bg = ImageView(this).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            setImageResource(nextBackground())
        }
        root.addView(bg, FrameLayout.LayoutParams(-1, -1))
        val overlay = View(this).apply { setBackgroundColor(0xB5000000.toInt()) }
        root.addView(overlay, FrameLayout.LayoutParams(-1, -1))

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(36, 70, 36, 40)
        }
        nameView = tv(prefs.getString("name", "")!!.uppercase(), 18f)
        nameView.alpha = .8f
        datesView = tv("", 12f).apply { alpha = .55f }
        percent = tv("0.0%", 31f).apply { gravity = Gravity.CENTER; typeface = android.graphics.Typeface.DEFAULT_BOLD }
        progress = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply { max = 10000 }
        days = tv("0", 92f).apply { gravity = Gravity.CENTER; typeface = android.graphics.Typeface.DEFAULT_BOLD }
        val left = tv("ОСТАЛОСЬ ДНЕЙ", 14f).apply { gravity = Gravity.CENTER; letterSpacing = .18f }
        val small = tv("DMB.", 11f).apply { alpha = .35f; gravity = Gravity.CENTER }

        content.addView(nameView, lp(0, 0, 0, 8))
        content.addView(datesView, lp(0, 0, 0, 75))
        content.addView(percent, lp())
        content.addView(progress, lp(0, 22, 0, 0))
        content.addView(days, lp(0, 55, 0, 0))
        content.addView(left, lp(0, -6, 0, 0))
        content.addView(small, lp(0, 35, 0, 0))
        root.addView(content, FrameLayout.LayoutParams(-1, -1))

        val fade = AlphaAnimation(0f, 1f).apply { duration = 900 }
        content.startAnimation(fade)
        tick()
    }

    private fun tick() {
        if (!::days.isInitialized) return
        val start = prefs.getLong("start", 0)
        val end = prefs.getLong("end", 0)
        val now = System.currentTimeMillis()
        val total = max(1L, end - start)
        val elapsed = now - start
        val remain = max(0L, end - now)
        val p = min(1.0, max(0.0, elapsed.toDouble() / total.toDouble()))
        percent.text = String.format(Locale.US, "%.1f%%", p * 100.0)
        progress.progress = (p * 10000).toInt()
        val d = remain / 86400000L
        days.text = d.toString()
        datesView.text = "${fmt(start)}  —  ${fmt(end)}"
        handler.postDelayed({ tick() }, 1000)
    }

    private fun fmt(ms: Long): String = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(ms))

    private fun nextBackground(): Int {
        val available = bgIds.filter { it != lastBackground }
        val chosen = available.random()
        lastBackground = chosen
        return chosen
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    private fun tv(s: String, size: Float) = TextView(this).apply {
        text = s; textSize = size; setTextColor(0xFFFFFFFF.toInt())
        typeface = android.graphics.Typeface.create("sans-serif-condensed", android.graphics.Typeface.BOLD)
    }

    private fun lp(l: Int = 0, t: Int = 0, r: Int = 0, b: Int = 0) =
        LinearLayout.LayoutParams(-1, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
            setMargins(l, t, r, b)
        }
}
