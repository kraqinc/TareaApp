package com.profeloop.kalanba.tasks

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.profeloop.kalanba.databinding.ActivityTaskDetailBinding
import com.profeloop.kalanba.models.AppNotification
import com.profeloop.kalanba.models.Submission
import com.profeloop.kalanba.models.Task
import com.profeloop.kalanba.utils.Constants
import com.profeloop.kalanba.utils.FirebaseUtils
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TaskDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_GRADO = "extra_grado"
        const val EXTRA_ASIGNATURA = "extra_asignatura"
    }

    private lateinit var binding: ActivityTaskDetailBinding
    private var task: Task? = null
    private var selectedFileUri: Uri? = null
    private lateinit var submissionAdapter: SubmissionAdapter

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedFileUri = it
            val fileName = uri.lastPathSegment ?: "archivo"
            binding.tvSelectedFile.text = fileName
            binding.tvSelectedFile.visibility = View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val taskId = intent.getStringExtra(EXTRA_TASK_ID) ?: run { finish(); return }

        submissionAdapter = SubmissionAdapter(emptyList()) { submission, nota ->
            gradeSubmission(submission, nota)
        }
        binding.rvSubmissions.layoutManager = LinearLayoutManager(this)
        binding.rvSubmissions.adapter = submissionAdapter

        lifecycleScope.launch {
            val uid = FirebaseUtils.currentUid ?: return@launch
            val user = FirebaseUtils.getUserProfile(uid)
            loadTask(taskId, uid, user?.rol == Constants.ROL_PROFESOR)
        }

        binding.btnAttachFile.setOnClickListener {
            filePickerLauncher.launch("*/*")
        }

        binding.btnSubmit.setOnClickListener {
            submitTask()
        }
    }

    private suspend fun loadTask(taskId: String, uid: String, isTeacher: Boolean) {
        binding.progressBar.visibility = View.VISIBLE
        try {
            val doc = FirebaseUtils.db.collection(Constants.COLLECTION_TASKS)
                .document(taskId).get().await()
            task = doc.toObject(Task::class.java)
            task?.let { t ->
                binding.tvTitle.text = t.titulo
                binding.tvSubjectBadge.text = "${t.asignatura} - Grado ${t.grado}"
                binding.tvProfessor.text = "Profesor: ${t.profesorNombre}"
                val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                binding.tvDeadline.text = "Fecha límite: ${sdf.format(Date(t.fechaLimite))}"
                binding.tvDescription.text = t.descripcion

                if (t.archivoUrl.isNotEmpty()) {
                    binding.btnDownloadFile.visibility = View.VISIBLE
                    binding.btnDownloadFile.setOnClickListener {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(t.archivoUrl))
                        startActivity(intent)
                    }
                }

                if (isTeacher) {
                    binding.tvSubmissionsHeader.visibility = View.VISIBLE
                    binding.rvSubmissions.visibility = View.VISIBLE
                    loadSubmissions(taskId)
                } else {
                    binding.cardSubmit.visibility = View.VISIBLE
                    checkExistingSubmission(taskId, uid)
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error cargando tarea: ${e.message}", Toast.LENGTH_SHORT).show()
        }
        binding.progressBar.visibility = View.GONE
    }

    private suspend fun checkExistingSubmission(taskId: String, uid: String) {
        val existing = FirebaseUtils.getMySubmission(taskId, uid)
        if (existing != null) {
            binding.tvSubmissionStatus.visibility = View.VISIBLE
            binding.tvSubmissionStatus.text = when (existing.estado) {
                Constants.ESTADO_CALIFICADO -> "Entregado ✓ - Nota: ${existing.nota}"
                else -> "Entregado ✓ - En revisión"
            }
            binding.btnSubmit.isEnabled = false
        }
    }

    private suspend fun loadSubmissions(taskId: String) {
        val submissions = FirebaseUtils.getSubmissionsForTask(taskId)
        submissionAdapter.updateData(submissions)
    }

    private fun submitTask() {
        val uid = FirebaseUtils.currentUid ?: return
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSubmit.isEnabled = false

        lifecycleScope.launch {
            try {
                val user = FirebaseUtils.getUserProfile(uid) ?: return@launch
                var fileUrl = ""
                var fileName = ""

                selectedFileUri?.let { uri ->
                    fileName = uri.lastPathSegment ?: "archivo"
                    val ref = FirebaseUtils.storage.reference
                        .child("submissions/$uid/${task?.id}/$fileName")
                    ref.putFile(uri).await()
                    fileUrl = ref.downloadUrl.await().toString()
                }

                val submission = Submission(
                    taskId = task?.id ?: "",
                    estudianteId = uid,
                    estudianteNombre = user.nombre,
                    archivoUrl = fileUrl,
                    archivoNombre = fileName,
                    fechaEnvio = System.currentTimeMillis(),
                    estado = Constants.ESTADO_ENVIADO
                )

                val success = FirebaseUtils.submitTask(submission)
                if (success) {
                    Toast.makeText(this@TaskDetailActivity, "Tarea entregada exitosamente", Toast.LENGTH_SHORT).show()
                    binding.tvSubmissionStatus.visibility = View.VISIBLE
                    binding.tvSubmissionStatus.text = "Entregado ✓ - En revisión"
                    binding.btnSubmit.isEnabled = false

                    // Notify professor
                    task?.profesorId?.let { profId ->
                        if (profId.isNotEmpty()) {
                            FirebaseUtils.sendNotification(
                                AppNotification(
                                    titulo = "Nueva entrega",
                                    mensaje = "${user.nombre} entregó: ${task?.titulo}",
                                    tipo = "submission",
                                    timestamp = System.currentTimeMillis(),
                                    targetUserId = profId
                                )
                            )
                        }
                    }
                } else {
                    Toast.makeText(this@TaskDetailActivity, "Error al entregar", Toast.LENGTH_SHORT).show()
                    binding.btnSubmit.isEnabled = true
                }
            } catch (e: Exception) {
                Toast.makeText(this@TaskDetailActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                binding.btnSubmit.isEnabled = true
            }
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun gradeSubmission(submission: Submission, nota: String) {
        lifecycleScope.launch {
            try {
                FirebaseUtils.gradeSubmission(submission.id, nota, "")
                Toast.makeText(this@TaskDetailActivity, "Calificación guardada", Toast.LENGTH_SHORT).show()
                FirebaseUtils.sendNotification(
                    AppNotification(
                        titulo = "Tarea calificada",
                        mensaje = "Tu tarea '${task?.titulo}' fue calificada: $nota",
                        tipo = "grade",
                        timestamp = System.currentTimeMillis(),
                        targetUserId = submission.estudianteId
                    )
                )
                loadSubmissions(submission.taskId)
            } catch (e: Exception) {
                Toast.makeText(this@TaskDetailActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
