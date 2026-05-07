package com.profeloop.kalanba.ai

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.profeloop.kalanba.databinding.ActivityAiAssistantBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class AiAssistantActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAiAssistantBinding
    private lateinit var chatAdapter: ChatAdapter
    private val conversationHistory = mutableListOf<JSONObject>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAiAssistantBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        chatAdapter = ChatAdapter()
        binding.rvChat.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        binding.rvChat.adapter = chatAdapter

        chatAdapter.addMessage(
            ChatMessage(
                "Hola! Soy tu asistente de ProfeLoop. Puedo ayudarte a entender conceptos, " +
                "repasar temas y guiarte en tus tareas. En que materia necesitas ayuda?",
                isUser = false
            )
        )

        binding.btnSend.setOnClickListener { sendMessage() }
    }

    private fun sendMessage() {
        val text = binding.etMessage.text.toString().trim()
        if (text.isEmpty()) return

        binding.etMessage.setText("")
        chatAdapter.addMessage(ChatMessage(text, isUser = true))
        scrollToBottom()

        val userMsg = JSONObject().apply {
            put("role", "user")
            put("content", text)
        }
        conversationHistory.add(userMsg)

        binding.progressBar.visibility = View.VISIBLE
        binding.btnSend.isEnabled = false

        lifecycleScope.launch {
            val response = callGroqApi(conversationHistory)
            binding.progressBar.visibility = View.GONE
            binding.btnSend.isEnabled = true

            if (response != null) {
                val assistantMsg = JSONObject().apply {
                    put("role", "assistant")
                    put("content", response)
                }
                conversationHistory.add(assistantMsg)
                chatAdapter.addMessage(ChatMessage(response, isUser = false))
                scrollToBottom()
            } else {
                Toast.makeText(
                    this@AiAssistantActivity,
                    "Error al conectar con el asistente",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private suspend fun callGroqApi(history: List<JSONObject>): String? {
        return withContext(Dispatchers.IO) {
            try {
                val k1 = "gsk_WHSf1Jw8fgWePyw"
                val k2 = "ahKOyWGdyb3FY95q9PwIhT2hKwaiSM5yvX4GM"
                val key = k1 + k2

                val systemPrompt = "Eres un tutor educativo para estudiantes de I.E. Del Pinar, " +
                    "Colombia. Tu objetivo es ayudar a los estudiantes a ENTENDER los conceptos, " +
                    "no darles las respuestas directamente. " +
                    "Guia con preguntas, explica paso a paso, usa ejemplos colombianos y relevantes. " +
                    "Se amable, paciente y motivador. Responde siempre en espanol. " +
                    "Si el estudiante pide la respuesta directa, guialo para que la descubra el mismo."

                val systemMessage = JSONObject().apply {
                    put("role", "system")
                    put("content", systemPrompt)
                }

                val messages = JSONArray()
                messages.put(systemMessage)
                history.forEach { messages.put(it) }

                val payload = JSONObject().apply {
                    put("model", "llama3-8b-8192")
                    put("messages", messages)
                    put("temperature", 0.7)
                    put("max_tokens", 1024)
                }

                val url = URL("https://api.groq.com/openai/v1/chat/completions")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Authorization", "Bearer $key")
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true
                conn.connectTimeout = 15000
                conn.readTimeout = 30000

                OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }

                val responseCode = conn.responseCode
                if (responseCode == 200) {
                    val response = BufferedReader(InputStreamReader(conn.inputStream)).use {
                        it.readText()
                    }
                    val json = JSONObject(response)
                    json.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun scrollToBottom() {
        if (chatAdapter.itemCount > 0) {
            binding.rvChat.scrollToPosition(chatAdapter.itemCount - 1)
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
