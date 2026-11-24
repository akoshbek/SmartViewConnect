package com.smartview.connect

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class MainActivity : Activity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) {
            showHome()
        } else {
            showLogin()
        }
    }

    private fun createLayout(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(32, 32, 32, 32)
            setBackgroundColor(Color.rgb(15, 23, 42))
        }
    }

    private fun showLogin() {
        val layout = createLayout()

        val title = TextView(this).apply {
            text = "SMART VIEW CONNECT"
            textSize = 24f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }

        val email = EditText(this).apply {
            hint = "Email manzilingiz"
            inputType = 33
        }

        val password = EditText(this).apply {
            hint = "Parol"
            inputType = 129
        }

        layout.addView(title)
        layout.addView(email)
        layout.addView(password)

        addButton(layout, "Kirish") {
            authenticate(email.text.toString(), password.text.toString(), false)
        }

        addButton(layout, "Ro‘yxatdan o‘tish") {
            authenticate(email.text.toString(), password.text.toString(), true)
        }

        setContentView(layout)
    }

    private fun authenticate(email: String, password: String, register: Boolean) {
        if (email.isBlank() || password.length < 6) {
            Toast.makeText(
                this,
                "Email kiriting va kamida 6 belgili parol yozing",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val task = if (register) {
            auth.createUserWithEmailAndPassword(email, password)
        } else {
            auth.signInWithEmailAndPassword(email, password)
        }

        task.addOnCompleteListener { result ->
            if (result.isSuccessful) {
                showHome()
            } else {
                Toast.makeText(
                    this,
                    result.exception?.localizedMessage ?: "Xatolik yuz berdi",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun showHome() {
        val layout = createLayout()

        val title = TextView(this).apply {
            text = "SMART VIEW CONNECT"
            textSize = 25f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }

        layout.addView(title)

        addButton(layout, "Kamera ulashish") {
            showInfo("Kamera egasining roziligi va Android ruxsati kerak.")
        }

        addButton(layout, "Ekran ulashish") {
            showInfo("Ekran uzatish Android tasdig‘i bilan boshlanadi.")
        }

        addButton(layout, "Tasvirni ko‘rish") {
            showInfo("Keyingi bosqichda PIN-kodli tasdiqlangan ulanish qo‘shiladi.")
        }

        addButton(layout, "Hisobdan chiqish") {
            auth.signOut()
            showLogin()
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
            setOnClickListener { action() }
        }

        layout.addView(
            button,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 18
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
