package com.foldersmith.mobile.domain

object FilenameConflictResolver {
    fun safeName(desiredName: String, existingNames: Set<String>): String {
        if (desiredName !in existingNames) return desiredName
        val dotIndex = desiredName.lastIndexOf('.').takeIf { it > 0 }
        val base = dotIndex?.let { desiredName.substring(0, it) } ?: desiredName
        val extension = dotIndex?.let { desiredName.substring(it) }.orEmpty()
        var counter = 1
        while (true) {
            val candidate = "$base ($counter)$extension"
            if (candidate !in existingNames) return candidate
            counter += 1
        }
    }
}
