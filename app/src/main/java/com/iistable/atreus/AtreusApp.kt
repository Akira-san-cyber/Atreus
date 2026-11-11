package com.iistable.atreus
import android.app.Application
import com.iistable.atreus.ai.EngineProvider
import com.iistable.atreus.ai.LlmEngine
import com.iistable.atreus.data.TimetableRepository
class AtreusApp : Application() { lateinit var engine: LlmEngine; lateinit var timetable: TimetableRepository; override fun onCreate() { super.onCreate(); timetable = TimetableRepository(this); engine = EngineProvider.create(this) } }
