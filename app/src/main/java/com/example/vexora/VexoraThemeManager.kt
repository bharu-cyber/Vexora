package com.example.vexora

import android.content.Context
import android.graphics.Color
import android.net.Uri

object VexoraThemeManager {

    // =========================================================
    // STORAGE
    // =========================================================

    const val PREFS_NAME = "VexoraThemePrefs"

    const val KEY_THEME = "selected_theme"
    const val KEY_PHOTO = "selected_photo"

    // Photo theme brightness
    const val KEY_PHOTO_BRIGHTNESS = "photo_brightness"

    const val PHOTO = "photo"


    // =========================================================
    // PALETTE
    // =========================================================

    data class Palette(
        val background: Int,
        val key: Int,
        val special: Int,
        val accent: Int,
        val text: Int = Color.WHITE
    )


    // =========================================================
    // THEME STORAGE
    // =========================================================

    fun selectedTheme(context: Context): String {

        return try {

            context.getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            ).getString(
                KEY_THEME,
                "cyber"
            ) ?: "cyber"

        } catch (_: Exception) {

            "cyber"
        }
    }


    // Compatibility function
    fun getTheme(context: Context): String {

        return try {

            selectedTheme(context)

        } catch (_: Exception) {

            "cyber"
        }
    }


    // =========================================================
    // PHOTO STORAGE
    // =========================================================

    fun selectedPhoto(context: Context): String? {

        return try {

            context.getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            ).getString(
                KEY_PHOTO,
                null
            )

        } catch (_: Exception) {

            null
        }
    }


    fun getPhotoUri(context: Context): Uri? {

        val photo = selectedPhoto(context)

        return if (photo.isNullOrEmpty()) {

            null

        } else {

            try {

                Uri.parse(photo)

            } catch (_: Exception) {

                null
            }
        }
    }


    // =========================================================
    // PHOTO BRIGHTNESS
    // =========================================================

    fun photoBrightness(context: Context): Int {

        return context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        ).getInt(
            KEY_PHOTO_BRIGHTNESS,
            70
        ).coerceIn(0, 100)
    }


    // =========================================================
    // THEME PALETTES
    // =========================================================

    fun palette(context: Context): Palette {

        return when (selectedTheme(context)) {

            // -------------------------------------------------
            // CYBER
            // -------------------------------------------------

            "cyber" -> Palette(
                background = Color.rgb(6, 20, 43),
                key = Color.rgb(16, 43, 74),
                special = Color.rgb(21, 60, 106),
                accent = Color.rgb(39, 230, 236)
            )


            // -------------------------------------------------
            // NEON
            // -------------------------------------------------

            "neon" -> Palette(
                background = Color.rgb(12, 7, 30),
                key = Color.rgb(35, 20, 65),
                special = Color.rgb(70, 35, 110),
                accent = Color.rgb(210, 70, 255)
            )


            // -------------------------------------------------
            // PURPLE
            // -------------------------------------------------

            "purple" -> Palette(
                background = Color.rgb(15, 21, 58),
                key = Color.rgb(60, 44, 89),
                special = Color.rgb(90, 60, 120),
                accent = Color.rgb(188, 146, 205)
            )


            // -------------------------------------------------
            // BLUE
            // -------------------------------------------------

            "blue" -> Palette(
                background = Color.rgb(4, 20, 45),
                key = Color.rgb(13, 48, 82),
                special = Color.rgb(21, 76, 115),
                accent = Color.rgb(48, 206, 230)
            )


            // -------------------------------------------------
            // DARK
            // -------------------------------------------------

            "dark" -> Palette(
                background = Color.rgb(10, 10, 14),
                key = Color.rgb(32, 32, 38),
                special = Color.rgb(48, 48, 56),
                accent = Color.rgb(150, 150, 165)
            )


            // -------------------------------------------------
            // MIDNIGHT
            // -------------------------------------------------

            "midnight" -> Palette(
                background = Color.rgb(8, 12, 35),
                key = Color.rgb(27, 30, 65),
                special = Color.rgb(48, 45, 95),
                accent = Color.rgb(125, 115, 235)
            )


            // -------------------------------------------------
            // EMERALD
            // -------------------------------------------------

            "emerald" -> Palette(
                background = Color.rgb(6, 31, 25),
                key = Color.rgb(15, 67, 54),
                special = Color.rgb(25, 96, 75),
                accent = Color.rgb(55, 220, 155)
            )


            // -------------------------------------------------
            // CRIMSON
            // -------------------------------------------------

            "crimson" -> Palette(
                background = Color.rgb(38, 8, 17),
                key = Color.rgb(78, 24, 37),
                special = Color.rgb(112, 35, 52),
                accent = Color.rgb(240, 70, 95)
            )


            // -------------------------------------------------
            // ROSE
            // -------------------------------------------------

            "rose" -> Palette(
                background = Color.rgb(45, 12, 34),
                key = Color.rgb(82, 28, 60),
                special = Color.rgb(115, 42, 83),
                accent = Color.rgb(245, 110, 175)
            )


            // -------------------------------------------------
            // GOLDEN
            // -------------------------------------------------

            "golden" -> Palette(
                background = Color.rgb(43, 29, 8),
                key = Color.rgb(82, 57, 18),
                special = Color.rgb(115, 79, 23),
                accent = Color.rgb(245, 198, 75)
            )


            // -------------------------------------------------
            // OCEAN
            // -------------------------------------------------

            "ocean" -> Palette(
                background = Color.rgb(4, 27, 41),
                key = Color.rgb(14, 65, 82),
                special = Color.rgb(22, 94, 112),
                accent = Color.rgb(55, 205, 230)
            )


            // -------------------------------------------------
            // PHOTO
            // -------------------------------------------------

            PHOTO -> Palette(
                background = Color.rgb(12, 14, 22),
                key = Color.rgb(38, 42, 55),
                special = Color.rgb(62, 68, 86),
                accent = Color.WHITE
            )


            // -------------------------------------------------
            // DEFAULT
            // -------------------------------------------------

            else -> Palette(
                background = Color.rgb(6, 20, 43),
                key = Color.rgb(16, 43, 74),
                special = Color.rgb(21, 60, 106),
                accent = Color.rgb(39, 230, 236)
            )
        }
    }


    // =========================================================
    // COMPATIBILITY FUNCTIONS
    // =========================================================

    fun backgroundColor(context: Context): Int {

        return palette(context).background
    }


    fun keyColor(context: Context): Int {

        return palette(context).key
    }


    fun specialKeyColor(context: Context): Int {

        return palette(context).special
    }


    fun textColor(context: Context): Int {

        return palette(context).text
    }


    fun dividerColor(context: Context): Int {

        val accent = palette(context).accent

        return Color.argb(
            110,
            Color.red(accent),
            Color.green(accent),
            Color.blue(accent)
        )
    }
}