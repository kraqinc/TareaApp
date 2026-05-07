package com.profeloop.kalanba.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.profeloop.kalanba.models.AppNotification
import com.profeloop.kalanba.models.Submission
import com.profeloop.kalanba.models.Task
import com.profeloop.kalanba.models.User
import kotlinx.coroutines.tasks.await

object FirebaseUtils {
    val auth: FirebaseAuth get() = FirebaseAuth.getInstance()
    val db: FirebaseFirestore get() = FirebaseFirestore.getInstance()
    val storage: FirebaseStorage get() = FirebaseStorage.getInstance()
    val currentUid: String? get() = auth.currentUser?.uid

    suspend fun getUserProfile(uid: String): User? {
        return try {
            db.collection(Constants.COLLECTION_USERS).document(uid).get().await()
                .toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun saveUserProfile(user: User) {
        db.collection(Constants.COLLECTION_USERS).document(user.uid).set(user).await()
    }

    suspend fun getTasks(grado: Int, asignatura: String, periodo: Int): List<Task> {
        return try {
            db.collection(Constants.COLLECTION_TASKS)
                .whereEqualTo("grado", grado)
                .whereEqualTo("asignatura", asignatura)
                .whereEqualTo("periodo", periodo)
                .get().await()
                .toObjects(Task::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun publishTask(task: Task): Boolean {
        return try {
            val docRef = db.collection(Constants.COLLECTION_TASKS).document()
            db.collection(Constants.COLLECTION_TASKS).document(docRef.id)
                .set(task.copy(id = docRef.id)).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun submitTask(submission: Submission): Boolean {
        return try {
            val docRef = db.collection(Constants.COLLECTION_SUBMISSIONS).document()
            db.collection(Constants.COLLECTION_SUBMISSIONS).document(docRef.id)
                .set(submission.copy(id = docRef.id)).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getSubmissionsForTask(taskId: String): List<Submission> {
        return try {
            db.collection(Constants.COLLECTION_SUBMISSIONS)
                .whereEqualTo("taskId", taskId).get().await()
                .toObjects(Submission::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getMySubmission(taskId: String, estudianteId: String): Submission? {
        return try {
            db.collection(Constants.COLLECTION_SUBMISSIONS)
                .whereEqualTo("taskId", taskId)
                .whereEqualTo("estudianteId", estudianteId)
                .get().await().toObjects(Submission::class.java).firstOrNull()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun gradeSubmission(submissionId: String, nota: String, comentario: String) {
        db.collection(Constants.COLLECTION_SUBMISSIONS).document(submissionId)
            .update(mapOf(
                "nota" to nota,
                "comentarioProfe" to comentario,
                "estado" to Constants.ESTADO_CALIFICADO
            )).await()
    }

    suspend fun getNotifications(uid: String): List<AppNotification> {
        return try {
            db.collection(Constants.COLLECTION_NOTIFICATIONS)
                .whereEqualTo("targetUserId", uid)
                .get().await().toObjects(AppNotification::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun sendNotification(notification: AppNotification) {
        try {
            val docRef = db.collection(Constants.COLLECTION_NOTIFICATIONS).document()
            db.collection(Constants.COLLECTION_NOTIFICATIONS).document(docRef.id)
                .set(notification.copy(id = docRef.id)).await()
        } catch (e: Exception) {
            // ignore
        }
    }

    suspend fun checkTeacherInSubject(grado: Int, asignatura: String): String? {
        return try {
            val docs = db.collection(Constants.COLLECTION_USERS)
                .whereEqualTo("rol", Constants.ROL_PROFESOR)
                .whereArrayContains("asignaturas", "$grado-$asignatura")
                .get().await()
            if (docs.isEmpty) null
            else docs.toObjects(User::class.java).firstOrNull()?.nombre
        } catch (e: Exception) {
            null
        }
    }
}
