package com.iistable.atreus.ai

import android.content.Context

object EngineProvider {
    fun create(context: Context): LlmEngine {
        return createChatEngine(context)
    }

    fun createChatEngine(context: Context): LlmEngine {
        return LlamaEngine(context)
    }

    fun createSelfEvolutionEngine(context: Context, type: ModelType): LlmEngine {
        val modelFile = ModelManager.getFile(context, type)
        return CactusEngine(context, modelFile.absolutePath)
    }
}
