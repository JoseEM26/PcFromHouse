package com.cibertec.notasfirebaseapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cibertec.notasfirebaseapp.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var b: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(b.root)

        auth = FirebaseAuth.getInstance()

        b.btnLogin.setOnClickListener {
            val email = b.etEmail.text?.toString()?.trim().orEmpty()
            val pass = b.etPassword.text?.toString()?.trim().orEmpty()
            if (email.isEmpty() || pass.isEmpty()) {
                toast(getString(R.string.msg_complete_fields)); return@setOnClickListener
            }
            auth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                .addOnFailureListener { toast(getString(R.string.msg_error, it.message ?: "")) }
        }

        b.btnRegister.setOnClickListener {
            val email = b.etEmail.text?.toString()?.trim().orEmpty()
            val pass = b.etPassword.text?.toString()?.trim().orEmpty()
            if (email.isEmpty() || pass.isEmpty()) {
                toast(getString(R.string.msg_complete_fields)); return@setOnClickListener
            }
            auth.createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener { toast(getString(R.string.msg_user_registered)) }
                .addOnFailureListener { toast(getString(R.string.msg_error, it.message ?: "")) }
        }
    }

    override fun onStart() {
        super.onStart()
        if (FirebaseAuth.getInstance().currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
