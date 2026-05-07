package com.profeloop.kalanba.auth

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.profeloop.kalanba.databinding.ActivityRegisterBinding
import com.profeloop.kalanba.models.User
import com.profeloop.kalanba.utils.FirebaseUtils
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSpinners()
        binding.btnRegister.setOnClickListener { register() }
    }

    private fun setupSpinners() {
        val roles = listOf("Estudiante", "Profesor")
        binding.spinnerRol.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, roles)

        val niveles = listOf("Primaria", "Bachillerato")
        binding.spinnerNivel.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, niveles)

        val grados = (1..9).map { "Grado $it" }
        binding.spinnerGrado.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, grados)
    }

    private fun register() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val rolSelected = binding.spinnerRol.selectedItemPosition
        val nivelSelected = binding.spinnerNivel.selectedItemPosition
        val gradoSelected = binding.spinnerGrado.selectedItemPosition + 1

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }
        if (password.length < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
            return
        }

        val rol = if (rolSelected == 0) "estudiante" else "profesor"
        val nivel = if (nivelSelected == 0) "primaria" else "bachillerato"

        showLoading(true)
        lifecycleScope.launch {
            try {
                val result = FirebaseUtils.auth
                    .createUserWithEmailAndPassword(email, password).await()
                val uid = result.user?.uid ?: return@launch
                val user = User(
                    uid = uid,
                    nombre = name,
                    email = email,
                    rol = rol,
                    nivel = nivel,
                    grado = gradoSelected
                )
                FirebaseUtils.saveUserProfile(user)
                Toast.makeText(this@RegisterActivity, "Cuenta creada exitosamente", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                showLoading(false)
                Toast.makeText(this@RegisterActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnRegister.isEnabled = !show
    }
}
