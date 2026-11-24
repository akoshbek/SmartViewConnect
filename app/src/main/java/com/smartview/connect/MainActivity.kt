
package com.smartview.connect

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.UUID

class MainActivity : Activity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance(
        "https://smartviewconnect-6860d-default-rtdb.firebaseio.com/"
    ).reference

    private val deviceId = UUID.randomUUID().toString()
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showLogin()
    }

    private fun showLogin() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 48, 32, 32)
        }

        val email = EditText(this).apply { hint = "Email" }
        val password = EditText(this).apply {
            hint = "Parol"
            inputType = 129
        }

        val login = Button(this).apply { text = "Kirish" }
        val register = Button(this).apply { text = "Ro‘yxatdan o‘tish" }

        layout.addView(email)
        layout.addView(password)
        layout.addView(login)
        layout.addView(register)
        setContentView(layout)

        login.setOnClickListener {
            auth.signInWithEmailAndPassword(
                email.text.toString().trim(),
                password.text.toString()
            ).addOnSuccessListener { showHome() }
                .addOnFailureListener {
                    Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                }
        }

        register.setOnClickListener {
            auth.createUserWithEmailAndPassword(
                email.text.toString().trim(),
                password.text.toString()
            ).addOnSuccessListener { showHome() }
                .addOnFailureListener {
                    Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun showHome() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 40, 24, 24)
        }

        status = TextView(this).apply {
            text = "Smart View Connect"
            textSize = 20f
        }
        layout.addView(status)

        addButton(layout, "Ulanish kodi yaratish") {
            createCode()
        }
        addButton(layout, "Kod bilan ulanish") {
            enterCode()
        }
        addButton(layout, "Kamera ulashish") {
            info("Kamera ulashish keyingi bosqichda qo‘shiladi.")
        }
        addButton(layout, "Ekran ulashish") {
            info("Ekran ulashish Android tasdig‘i bilan keyingi bosqichda qo‘shiladi.")
        }
        addButton(layout, "Hisobdan chiqish") {
            auth.signOut()
            showLogin()
        }

        setContentView(layout)
    }

    private fun addButton(
        layout: LinearLayout,
        title: String,
        action: () -> Unit
    ) {
        val button = Button(this).apply {
            text = title
            setOnClickListener { action() }
        }
        layout.addView(button)
    }

    private fun createCode() {
        val uid = auth.currentUser?.uid ?: return
        val code = (100000..999999).random().toString()
        val request = mapOf(
            "ownerUid" to uid,
            "ownerDevice" to deviceId,
            "status" to "pending",
            "createdAt" to System.currentTimeMillis()
        )

        db.child("pairingRequests").child(code).setValue(request)
            .addOnSuccessListener {
                AlertDialog.Builder(this)
                    .setTitle("Ulanish kodi")
                    .setMessage("Kod: $code\nUni ikkinchi telefonga kiriting.")
                    .setPositiveButton("Yopish", null)
                    .show()
                listenForRequest(code)
            }
            .addOnFailureListener {
                info("Kod yaratilmadi: ${it.message}")
            }
    }

    private fun listenForRequest(code: String) {
        db.child("pairingRequests").child(code).child("status")
            .get()
            .addOnSuccessListener {
                status.text = "So‘rov holati: ${it.value ?: "Kutilmoqda"}"
            }
    }

    private fun enterCode() {
        val input = EditText(this).apply {
            hint = "6 xonali kod"
        }

        AlertDialog.Builder(this)
            .setTitle("Ulanish kodi")
            .setView(input)
            .setNegativeButton("Bekor qilish", null)
            .setPositiveButton("Yuborish") { _, _ ->
                val code = input.text.toString().trim()
                if (code.length != 6 || code.any { !it.isDigit() }) {
                    info("6 xonali kod kiriting.")
                } else {
                    info("Kod tekshiruvi uchun xavfsiz server tasdig‘i keyingi bosqichda qo‘shiladi.")
                }
            }
            .show()
    }

    private fun info(message: String) {
        AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}
