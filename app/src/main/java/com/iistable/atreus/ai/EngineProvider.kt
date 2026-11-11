package com.iistable.atreus.ai
import android.content.Context
object EngineProvider {
    fun create(context: Context): LlmEngine {
        val modelFile = ModelDownloader.getFile(context)
        return CactusEngine(context, modelFile.absolutePath)
    }
}
