package com.iistable.atreus.ai

import com.github.difflib.DiffUtils
import com.github.difflib.UnifiedDiffUtils
import com.github.difflib.patch.Patch
import java.io.File

object PatchApplier {
    fun apply(patchContent: String, targetFile: File, workspaceRoot: File): Result<Unit> {
        return try {
            val canonicalFile = targetFile.canonicalFile
            if (!canonicalFile.path.startsWith(workspaceRoot.canonicalPath)) {
                return Result.failure(Exception("Security: Path traversal detected."))
            }

            if (!targetFile.exists()) {
                targetFile.parentFile?.mkdirs()
                targetFile.writeText("")
            }

            val originalLines = targetFile.readLines()
            val patch: Patch<String> = UnifiedDiffUtils.parseUnifiedDiff(patchContent.lines())
            val patchedLines = DiffUtils.patch(originalLines, patch)
            targetFile.writeText(patchedLines.joinToString("\n"))

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
