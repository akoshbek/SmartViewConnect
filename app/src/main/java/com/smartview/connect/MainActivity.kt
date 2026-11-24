package com.smartview.connect

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(32, 32, 32, 32)
            setBackgroundColor(Color.rgb(15, 23, 42))
        }

        val title = TextView(this).apply {
            text = "SMART VIEW CONNECT"
            textSize = 26f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }

        val subtitle = TextView(this).apply {
            text = "Xavfsiz masofaviy ulanish"
            textSize = 16f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
        }

        layout.addView(title)
        layout.addView(subtitle)

        addButton(layout, "📷 Kamera ulashish") {
            showInfo("Kamera egasining ruxsati bilan ulanadi.")
        }

        addButton(layout, "📱 Ekran ulashish") {
            showInfo("Ekran uzatish Android tasdig‘i bilan boshlanadi.")
        }

        addButton(layout, "👁 Tasvirni ko‘rish") {
            showInfo("PIN-kod orqali ulanish funksiyasi keyingi bosqichda qo‘shiladi.")
        }

        setContentView(layout)
    }

    private fun addButton(
        layout: LinearLayout,
        label: String,
        action: () -> Unit
    ) {
        val button = Button(this).apply {
            text = label
            textSize = 16f
            setOnClickListener { action() }
        }

        layout.addView(
            button,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 24
            }
        )
    }

    private fun showInfo(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Smart View Connect")
            .setMessage(message)
            .setPositiveButton("Tushunarli", null)
            .show()
    }
}
