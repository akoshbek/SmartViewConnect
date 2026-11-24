
package com.smartview.connect

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.text.InputType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.util.UUID
import java.security.SecureRandom

class MainActivity : Activity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    private fun deviceId(): String {
        val prefs = getSharedPreferences("device_data", MODE_PRIVATE)
        var id = prefs.getString("device_id", null)

        if (id == null) {
            id = UUID.randomUUID().toString()
            prefs.edit().putString("device_id", id).apply()
        }
        return id
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance(
            "https://smartviewconnect-6860d-default-rtdb.firebaseio.com/"
        )

        if (auth.currentUser != null) showHome()
        else showLogin()
    }

    private fun createLayout(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(32, 32, 32, 32)
            setBackgroundColor(Color.rgb(15, 23, 42))
        }
    }

    private fun makeField(hintText: String, type: Int): EditText {
        return EditText(this).apply {
            hint = hintText
            inputType = type
            setTextColor(Color.WHITE)
            setHintTextColor(Color.LTGRAY)
            textSize = 16f
            backgroundTintList =
                android.content.res.ColorStateList.valueOf(Color.WHITE)

            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 60
            ).apply {
                bottomMargin = 16
            }
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

        val email = makeField(
            "Email manzilingiz",
            InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        )

        val password = makeField(
            "Parol",
            InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_VARIATION_PASSWORD
        )

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

    private fun authenticate(
        email: String,
        password: String,
        register: Boolean
    ) {
        if (email.isBlank() || password.length < 6) {
            Toast.makeText(
                this,
                "Email va kamida 6 belgili parol kiriting",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val task = if (register) {
            auth.createUserWithEmailAndPassword(email.trim(), password)
        } else {
            auth.signInWithEmailAndPassword(email.trim(), password)
        }

        task.addOnCompleteListener { result ->
            if (result.isSuccessful) {
                showHome()
            } else {
                showInfo(
                    result.exception?.localizedMessage ?: "Kirishda xatolik"
                )
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

        addButton(layout, "Ulanish kodi yaratish") {
            createPairingCode()
        }

        addButton(layout, "Kod bilan ulanish") {
            enterPairingCode()
        }

        addButton(layout, "Kamera ulashish") {
            showInfo("Avval telefonlarni bog‘lash kerak. Kamera egasining roziligi talab qilinadi.")
        }

        addButton(layout, "Ekran ulashish") {
            showInfo("Ekran uzatish Android tasdig‘i bilan ishlaydi. Bu funksiya hali qo‘shilmagan.")
        }

        addButton(layout, "Tasvirni ko‘rish") {
            showInfo("Avval telefonlarni PIN-kod orqali bog‘lang.")
        }

        addButton(layout, "Hisobdan chiqish") {
            auth.signOut()
            showLogin()
        }

        setContentView(layout)
    }

    private fun createPairingCode() {
        val uid = auth.currentUser?.uid ?: return
        val code = String.format(
            "%06d",
            SecureRandom().nextInt(1_000_000)
        )

        val pairingRef = database.reference
            .child("users")
            .child(uid)
            .child("pairings")
            .child(code)

        val data = mapOf(
            "hostDeviceId" to deviceId(),
            "guestDeviceId" to "",
            "status" to "pending"
        )

        pairingRef.setValue(data)
            .addOnSuccessListener {
                AlertDialog.Builder(this)
                    .setTitle("Ulanish kodi")
                    .setMessage(
                        "Ikkinchi telefonda ushbu kodni kiriting:\n\n$code\n\n" +
                        "Ikkinchi telefon ulanish so‘rovi yuborganda xabar olasiz."
                    )
                    .setPositiveButton("Kuzatish", null)
                    .show()

                pairingRef.addValueEventListener(
                    object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val status = snapshot.child("status")
                                .getValue(String::class.java)

                            if (status == "connected") {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Ikkinchi telefon ulanish so‘rovi yubordi.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            showInfo("Firebase xatosi: ${error.message}")
                        }
                    }
                )
            }
            .addOnFailureListener {
                showInfo("Kod yaratilmadi: ${it.message}")
            }
    }

    private fun enterPairingCode() {
        val uid = auth.currentUser?.uid ?: return
        val input = makeField(
            "6 xonali kod",
            InputType.TYPE_CLASS_NUMBER
        )

        AlertDialog.Builder(this)
            .setTitle("Ikkinchi telefonni ulash")
            .setView(input)
            .setNegativeButton("Bekor qilish", null)
            .setPositiveButton("Ulanish") { _, _ ->
                val code = input.text.toString().trim()

                if (!code.matches(Regex("[0-9]{6}"))) {
                    showInfo("6 xonali kod kiriting.")
                    return@setPositiveButton
                }

                val ref = database.reference
                    .child("users")
                    .child(uid)
                    .child("pairings")
                    .child(code)

                ref.get()
                    .addOnSuccessListener { snapshot ->
                        val status = snapshot.child("status")
                            .getValue(String::class.java)

                        val hostId = snapshot.child("hostDeviceId")
                            .getValue(String::class.java)

                        if (!snapshot.exists() ||
                            status != "pending" ||
                            hostId == deviceId()
                        ) {
                            showInfo("Kod noto‘g‘ri, eskirgan yoki bu telefonning o‘z kodi.")
                            return@addOnSuccessListener
                        }

                        ref.updateChildren(
                            mapOf(
                                "guestDeviceId" to deviceId(),
                                "status" to "connected"
                            )
                        ).addOnSuccessListener {
                            showInfo("Ulanish so‘rovi yuborildi. Birinchi telefon tasdig‘ini kuting.")
                        }.addOnFailureListener {
                            showInfo("So‘rov yuborilmadi: ${it.message}")
                        }
                    }
                    .addOnFailureListener {
                        showInfo("Firebase xatosi: ${it.message}")
                    }
            }
            .show()
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
