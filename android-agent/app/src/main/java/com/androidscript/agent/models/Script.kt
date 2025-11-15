package com.androidscript.agent.models

import java.io.File
import java.util.Date

/**
 * Represents an AndroidScript file
 */
data class Script(
    val id: String,
    val name: String,
    val path: String,
    val content: String = "",
    val lastModified: Date = Date(),
    val size: Long = 0
) {
    val file: File
        get() = File(path)

    val exists: Boolean
        get() = file.exists()

    fun load(): String {
        return if (exists) {
            file.readText()
        } else {
            ""
        }
    }

    companion object {
        fun fromFile(file: File): Script {
            return Script(
                id = file.absolutePath,
                name = file.name,
                path = file.absolutePath,
                content = if (file.exists()) file.readText() else "",
                lastModified = Date(file.lastModified()),
                size = file.length()
            )
        }
    }
}
