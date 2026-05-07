package com.profeloop.kalanba.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
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

    val currentUser get() = auth.currentUser
    val currentUid get() = auth.currentUser?.uid

    // ─── Users ───────────────────────────────────────────────────────────────

    suspend fun getUserProfile(uid: String): User? {
        return try {
            val doc = db.collection(Constants.COLLECTION_USERS).document(uid).get().await()
            doc.toObject(User::class.java)?.copy(uid = doc.id)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun saveUserProfile(user: User) {
        db.collection(Constants.COLLECTION_USERS)
            .document(user.uid)
            .set(user)
            .await()
    }

    suspend fun updateFcmToken(uid: String, token: String) {
        db.collection(Constants.COLLECTION_USERS)
            .document(uid)
            .update("fcmToken", token)
            .await()
    }

    // ─── Tasks ────────────────────────────────────────────────────────────────

    suspend fun getTasksForGradeSubjectPeriod(
        grado: Int,
        asignatura: String,
        periodo: Int
    ): List<Task> {
        return try {
            val snapshot = db.collection(Constants.COLLECTION_TASKS)
                .whereEqualTo("grado", grado)
                .whereEqualTo("asignatura", asignatura)
                .whereEqualTo("periodo", periodo)
                .whereEqualTo("activa", true)
                .get()
                .await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Task::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun publishTask(task: Task): String {
        val ref = db.collection(Constants.COLLECTION_TASKS).document()
        val withId = task.copy(id = ref.id)
        ref.set(withId).await()
        return ref.id
    }

    // ─── Subject Assignment ───────────────────────────────────────────────────

    suspend fun getSubjectTeacher(grado: Int, asignatura: String): User? {
        return try {
            val snap = db.collection(Constants.COLLECTION_USERS)
                .whereEqualTo("rol", Constants.ROL_PROFESOR)
                .whereEqualTo("grado", grado)
                .whereArrayContains("asignaturas", asignatura)
                .get()
                .await()
            if (snap.isEmpty) null
            else {
                val doc = snap.documents.first()
                doc.toObject(User::class.java)?.copy(uid = doc.id)
            }
        } catch (e: Exception) {
            null
        }
    }

    // ─── Submissions ──────────────────────────────────────────────────────────

    suspend fun submitTask(submission: Submission): String {
        val ref = db.collection(Constants.COLLECTION_SUBMISSIONS).document()
        val withId = submission.copy(id = ref.id)
        ref.set(withId).await()
        return ref.id
    }

    suspend fun getSubmissionsForTask(tareaId: String): List<Submission> {
        return try {
            val snap = db.collection(Constants.COLLECTION_SUBMISSIONS)
                .whereEqualTo("tareaId", tareaId)
                .get()
                .await()
            snap.documents.mapNotNull { it.toObject(Submission::class.java)?.copy(id = it.id) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getStudentSubmissionForTask(tareaId: String, estudianteUid: String): Submission? {
        return try {
            val snap = db.collection(Constants.COLLECTION_SUBMISSIONS)
                .whereEqualTo("tareaId", tareaId)
                .whereEqualTo("estudianteUid", estudianteUid)
                .get()
                .await()
            if (snap.isEmpty) null
            else {
                val doc = snap.documents.first()
                doc.toObject(Submission::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateSubmissionStatus(submissionId: String, estado: String) {
        db.collection(Constants.COLLECTION_SUBMISSIONS)
            .document(submissionId)
            .update("estado", estado, "revisadoAt", System.currentTimeMillis())
            .await()
    }

    suspend fun gradeSubmission(submissionId: String, calificacion: Double, comentario: String) {
        db.collection(Constants.COLLECTION_SUBMISSIONS)
            .document(submissionId)
            .update(
                mapOf(
                    "estado" to Constants.ESTADO_CALIFICADA,
                    "calificacion" to calificacion,
                    "comentarioProfesor" to comentario,
                    "revisadoAt" to System.currentTimeMillis()
                )
            )
            .await()
    }

    // ─── Notifications ────────────────────────────────────────────────────────

    suspend fun saveNotification(notification: AppNotification) {
        val ref = db.collection(Constants.COLLECTION_NOTIFICATIONS).document()
        ref.set(notification.copy(id = ref.id)).await()
    }

    suspend fun getNotificationsForUser(uid: String): List<AppNotification> {
        return try {
            val snap = db.collection(Constants.COLLECTION_NOTIFICATIONS)
                .whereEqualTo("destinatarioUid", uid)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .await()
            snap.documents.mapNotNull { it.toObject(AppNotification::class.java)?.copy(id = it.id) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun markNotificationRead(notificationId: String) {
        db.collection(Constants.COLLECTION_NOTIFICATIONS)
            .document(notificationId)
            .update("leida", true)
            .await()
    }

    // ─── FCM Token ────────────────────────────────────────────────────────────

    suspend fun getFcmToken(): String? {
        return try {
            FirebaseMessaging.getInstance().token.await()
        } catch (e: Exception) {
            null
        }
    }
}
