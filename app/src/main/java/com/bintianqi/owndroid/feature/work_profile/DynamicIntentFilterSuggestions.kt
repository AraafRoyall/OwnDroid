package com.bintianqi.owndroid.feature.work_profile

import android.content.Intent
import java.lang.reflect.Modifier

internal data class DynamicIntentSuggestion(
    val name: String,
    val value: String
)

internal object DynamicIntentSuggestionCatalog {
    val actions: List<DynamicIntentSuggestion> by lazy { loadIntentConstants("ACTION_") }
    val categories: List<DynamicIntentSuggestion> by lazy { loadIntentConstants("CATEGORY_") }

    val mimeTypes = listOf(
        "Any file" to "*/*",
        "Images" to "image/*",
        "Video" to "video/*",
        "Audio" to "audio/*",
        "Text" to "text/*",
        "Applications" to "application/*",
        "Documents" to "application/pdf",
        "JSON" to "application/json",
        "XML" to "application/xml",
        "ZIP archive" to "application/zip",
        "APK package" to "application/vnd.android.package-archive",
        "Binary file" to "application/octet-stream",
        "Plain text" to "text/plain",
        "HTML" to "text/html",
        "CSV" to "text/csv",
        "JPEG image" to "image/jpeg",
        "PNG image" to "image/png",
        "GIF image" to "image/gif",
        "WebP image" to "image/webp",
        "HEIF image" to "image/heif",
        "HEIC image" to "image/heic",
        "MP4 video" to "video/mp4",
        "3GPP video" to "video/3gpp",
        "MPEG audio" to "audio/mpeg",
        "OGG audio" to "audio/ogg",
        "WAV audio" to "audio/wav",
        "MP4 audio" to "audio/mp4",
        "Email message" to "message/rfc822",
        "vCard contact" to "text/vcard",
        "Calendar" to "text/calendar",
        "EPUB ebook" to "application/epub+zip",
        "GZIP archive" to "application/gzip",
        "TAR archive" to "application/x-tar"
    ).map { DynamicIntentSuggestion(it.first, it.second) }

    private fun loadIntentConstants(prefix: String): List<DynamicIntentSuggestion> {
        return Intent::class.java.fields.asSequence()
            .filter {
                it.name.startsWith(prefix) &&
                    it.type == String::class.java &&
                    Modifier.isStatic(it.modifiers) && Modifier.isFinal(it.modifiers)
            }
            .mapNotNull { field ->
                runCatching {
                    (field.get(null) as? String)?.let {
                        DynamicIntentSuggestion(field.name, it)
                    }
                }.getOrNull()
            }
            .sortedBy { it.name }
            .toList()
    }
}
