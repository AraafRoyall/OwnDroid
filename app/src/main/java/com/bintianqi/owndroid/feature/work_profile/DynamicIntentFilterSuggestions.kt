package com.bintianqi.owndroid.feature.work_profile

import android.content.Intent
import android.provider.AlarmClock
import android.provider.MediaStore
import android.speech.RecognizerIntent

internal data class DynamicIntentSuggestion(
    val name: String,
    val value: String
)

/**
 * Curated app-facing suggestions based on AOSP DefaultCrossProfileIntentFiltersUtils.
 * Do not reflect every Intent constant here: broadcast-only and unrelated system actions
 * are intentionally excluded. Custom values can still be entered in the editor.
 */
internal object DynamicIntentSuggestionCatalog {
    val actions = listOf(
        DynamicIntentSuggestion("ACTION_VIEW", Intent.ACTION_VIEW),
        DynamicIntentSuggestion("ACTION_SEND", Intent.ACTION_SEND),
        DynamicIntentSuggestion("ACTION_SEND_MULTIPLE", Intent.ACTION_SEND_MULTIPLE),
        DynamicIntentSuggestion("ACTION_SENDTO", Intent.ACTION_SENDTO),
        DynamicIntentSuggestion("ACTION_GET_CONTENT", Intent.ACTION_GET_CONTENT),
        DynamicIntentSuggestion("ACTION_OPEN_DOCUMENT", Intent.ACTION_OPEN_DOCUMENT),
        DynamicIntentSuggestion("ACTION_PICK", Intent.ACTION_PICK),
        DynamicIntentSuggestion("ACTION_PICK_IMAGES", MediaStore.ACTION_PICK_IMAGES),
        DynamicIntentSuggestion("ACTION_EDIT", Intent.ACTION_EDIT),
        DynamicIntentSuggestion("ACTION_INSERT", Intent.ACTION_INSERT),
        DynamicIntentSuggestion("ACTION_INSERT_OR_EDIT", Intent.ACTION_INSERT_OR_EDIT),
        DynamicIntentSuggestion("ACTION_DIAL", Intent.ACTION_DIAL),
        DynamicIntentSuggestion("ACTION_CALL_BUTTON", Intent.ACTION_CALL_BUTTON),
        DynamicIntentSuggestion("ACTION_MAIN", Intent.ACTION_MAIN),
        DynamicIntentSuggestion("ACTION_IMAGE_CAPTURE", MediaStore.ACTION_IMAGE_CAPTURE),
        DynamicIntentSuggestion("ACTION_VIDEO_CAPTURE", MediaStore.ACTION_VIDEO_CAPTURE),
        DynamicIntentSuggestion("RECORD_SOUND_ACTION", MediaStore.Audio.Media.RECORD_SOUND_ACTION),
        DynamicIntentSuggestion("ACTION_SET_ALARM", AlarmClock.ACTION_SET_ALARM),
        DynamicIntentSuggestion("ACTION_SHOW_ALARMS", AlarmClock.ACTION_SHOW_ALARMS),
        DynamicIntentSuggestion("ACTION_SET_TIMER", AlarmClock.ACTION_SET_TIMER),
        DynamicIntentSuggestion("ACTION_RECOGNIZE_SPEECH", RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
    )

    val categories = listOf(
        DynamicIntentSuggestion("CATEGORY_DEFAULT", Intent.CATEGORY_DEFAULT),
        DynamicIntentSuggestion("CATEGORY_BROWSABLE", Intent.CATEGORY_BROWSABLE),
        DynamicIntentSuggestion("CATEGORY_OPENABLE", Intent.CATEGORY_OPENABLE),
        DynamicIntentSuggestion("CATEGORY_HOME", Intent.CATEGORY_HOME)
    )

    val mimeTypes = listOf(
        DynamicIntentSuggestion("Any data type", "*/*"),
        DynamicIntentSuggestion("Images", "image/*"),
        DynamicIntentSuggestion("Videos", "video/*"),
        DynamicIntentSuggestion("Phone contact", "vnd.android.cursor.item/phone"),
        DynamicIntentSuggestion("Phone contact (v2)", "vnd.android.cursor.item/phone_v2"),
        DynamicIntentSuggestion("Person", "vnd.android.cursor.item/person"),
        DynamicIntentSuggestion("Call list", "vnd.android.cursor.dir/calls"),
        DynamicIntentSuggestion("Call entry", "vnd.android.cursor.item/calls")
    )
}
