package com.iistable.atreus.ai
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

data class AgentMemory(
    var positiveRules: MutableList<String> = mutableListOf(),
    var negativeRules: MutableList<String> = mutableListOf(),
    var fewShots: MutableList<String> = mutableListOf(),
    var customPrompt: String = ""
)

object EvolutionEngine {
    private const val FILE_NAME = "atreus_agent_memory.gz"

    suspend fun saveMemory(context: Context, memory: AgentMemory) = withContext(Dispatchers.IO) {
        val json = JSONObject().apply {
            put("pos", JSONArray(memory.positiveRules))
            put("neg", JSONArray(memory.negativeRules))
            put("shots", JSONArray(memory.fewShots))
            put("prompt", memory.customPrompt)
        }
        GZIPOutputStream(FileOutputStream(File(context.filesDir, FILE_NAME))).use { it.write(json.toString().toByteArray()) }
    }

    suspend fun loadMemory(context: Context): AgentMemory = withContext(Dispatchers.IO) {
        val file = File(context.filesDir, FILE_NAME)
        if (!file.exists()) return@withContext AgentMemory()
        try {
            val jsonString = GZIPInputStream(FileInputStream(file)).bufferedReader().use { it.readText() }
            val json = JSONObject(jsonString)
            AgentMemory(
                positiveRules = json.optJSONArray("pos")?.let { arr -> MutableList(arr.length()) { arr.getString(it) } } ?: mutableListOf(),
                negativeRules = json.optJSONArray("neg")?.let { arr -> MutableList(arr.length()) { arr.getString(it) } } ?: mutableListOf(),
                fewShots = json.optJSONArray("shots")?.let { arr -> MutableList(arr.length()) { arr.getString(it) } } ?: mutableListOf(),
                customPrompt = json.optString("prompt", "")
            )
        } catch (e: Exception) { AgentMemory() }
    }
}
