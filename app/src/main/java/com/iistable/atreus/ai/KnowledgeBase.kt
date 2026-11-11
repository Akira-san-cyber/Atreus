package com.iistable.atreus.ai
object KnowledgeBase {
    fun getContext(query: String): String? {
        val lower = query.lowercase()
        if (lower.contains("math")) return "Math: Algebra, Calculus, Linear Algebra."
        if (lower.contains("physics")) return "Physics: Mechanics, Thermodynamics."
        if (lower.contains("c++")) return "C++: Pointers, OOP, Memory Management."
        if (lower.contains("python")) return "Python: Data Science, Scripting."
        return null
    }
}
