package com.example.vexora

import android.content.Context

object VexoraMessageStore {

    private const val PREFS_NAME = "VexoraPrefs"
    private const val KEY_ENCODED_MESSAGE = "encoded_message"

    fun saveMessage(
        context: Context,
        message: String
    ) {
        context.applicationContext
            .getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )
            .edit()
            .putString(
                KEY_ENCODED_MESSAGE,
                message
            )
            .apply()
    }

    fun getMessage(
        context: Context
    ): String? {

        return context.applicationContext
            .getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )
            .getString(
                KEY_ENCODED_MESSAGE,
                null
            )
    }

    fun clearMessage(
        context: Context
    ) {
        context.applicationContext
            .getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )
            .edit()
            .remove(KEY_ENCODED_MESSAGE)
            .apply()
    }
}