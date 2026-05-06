package com.profeloop.kalanba.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.profeloop.kalanba.MainActivity
import com.profeloop.kalanba.databinding.ActivityLoginBinding
import com.profeloop.kalanba.utils.FirebaseUtils
import com.profeloop.kalanba.utils.gone
import com.profeloop.kalanba.utils.toast
import com.profeloop.kalanba.utils.visible
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var googleSignInClient: GoogleSignInClient

    companion object {
        private const val RC_SIGN_IN = 9001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupGoogleSignIn()
        setupListeners()
    }

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("23246300357-5r8s862ova6kf9hgp3bi6vcbt7vkofis.apps.googleusercontent.com")
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val email    = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty()) {
                binding.tilEmail.error = "Ingresa tu correo"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                binding.tilPassword.error = "Ingresa tu contraseña"
                return@setOnClickListener
            }

            binding.tilEmail.error    = null
            binding.tilPassword.error = null
            doLogin(email, password)
        }

        binding.btnGoogleSignIn.setOnClickListener {
            binding.progressBar.visible()
            binding.btnGoogleSignIn.isEnabled = false
            startActivityForResult(googleSignInClient.signInIntent, RC_SIGN_IN)
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun doLogin(email: String, password: String) {
        binding.progressBar.visible()
        binding.btnLogin.isEnabled = false

        FirebaseUtils.auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                lifecycleScope.launch {
                    val uid   = FirebaseUtils.currentUid ?: return@launch
                    val token = FirebaseUtils.getFcmToken()
                    if (token != null) FirebaseUtils.updateFcmToken(uid, token)

                    binding.progressBar.gone()
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                }
            }
            .addOnFailureListener { e ->
                binding.progressBar.gone()
                binding.btnLogin.isEnabled = true
                val msg = when {
                    e.message?.contains("no user record") == true -> "No existe una cuenta con este correo"
                    e.message?.contains("password is invalid") == true -> "Contraseña incorrecta"
                    e.message?.contains("network") == true -> "Sin conexión a internet"
                    else -> "Error al iniciar sesión: ${e.message}"
                }
                toast(msg)
            }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account = task.getResult(ApiException::class.java)
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                FirebaseAuth.getInstance().signInWithCredential(credential)
                    .addOnCompleteListener(this) { authTask ->
                        binding.progressBar.gone()
                        binding.btnGoogleSignIn.isEnabled = true
                        if (authTask.isSuccessful) {
                            lifecycleScope.launch {
                                val uid = FirebaseUtils.currentUid ?: return@launch
                                val token = FirebaseUtils.getFcmToken()
                                if (token != null) FirebaseUtils.updateFcmToken(uid, token)
                            }
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        } else {
                            toast("Error con Google: ${authTask.exception?.message}")
                        }
                    }
            } catch (e: ApiException) {
                binding.progressBar.gone()
                binding.btnGoogleSignIn.isEnabled = true
                toast("Error Google Sign-In: ${e.statusCode}")
            }
        }
    }
}
