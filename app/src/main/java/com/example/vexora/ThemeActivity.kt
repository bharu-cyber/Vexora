
package com.example.vexora

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class ThemeActivity : Activity() {

    companion object {
        private const val PICKER_REQUEST = 1001
        private const val PHOTO_THEME_REQUEST = 1002

        private const val THEME_CHANGED_ACTION =
            "com.example.vexora.THEME_CHANGED"
    }

    private data class ThemeOption(
        val title: String,
        val description: String,
        val id: String,
        val background: Int,
        val accent: Int
    )

    private val themes = listOf(
        ThemeOption(
            "🛡️  Cyber Security",
            "Navy • electric cyan • blue",
            "cyber",
            Color.rgb(6, 20, 43),
            Color.rgb(39, 230, 236)
        ),
        ThemeOption(
            "⚡  Neon Cyber",
            "Black • neon purple",
            "neon",
            Color.rgb(12, 7, 30),
            Color.rgb(210, 70, 255)
        ),
        ThemeOption(
            "💜  Vexora Purple",
            "Indigo • purple • lavender",
            "purple",
            Color.rgb(15, 21, 58),
            Color.rgb(188, 146, 205)
        ),
        ThemeOption(
            "🌐  Cyber Blue",
            "Deep blue • cyan",
            "blue",
            Color.rgb(4, 20, 45),
            Color.rgb(48, 206, 230)
        ),
        ThemeOption(
            "🌑  Dark",
            "Charcoal • gray",
            "dark",
            Color.rgb(10, 10, 14),
            Color.rgb(150, 150, 165)
        ),
        ThemeOption(
            "🌌  Midnight",
            "Deep navy • violet",
            "midnight",
            Color.rgb(8, 12, 35),
            Color.rgb(125, 115, 235)
        ),
        ThemeOption(
            "🌿  Emerald",
            "Deep green • emerald",
            "emerald",
            Color.rgb(6, 31, 25),
            Color.rgb(55, 220, 155)
        ),
        ThemeOption(
            "🔥  Crimson",
            "Dark red • crimson",
            "crimson",
            Color.rgb(38, 8, 17),
            Color.rgb(240, 70, 95)
        ),
        ThemeOption(
            "🌸  Rose",
            "Dark rose • pink",
            "rose",
            Color.rgb(45, 12, 34),
            Color.rgb(245, 110, 175)
        ),
        ThemeOption(
            "☀️  Golden",
            "Dark gold • warm amber",
            "golden",
            Color.rgb(43, 29, 8),
            Color.rgb(245, 198, 75)
        ),
        ThemeOption(
            "🌊  Ocean",
            "Deep ocean • aqua",
            "ocean",
            Color.rgb(4, 27, 41),
            Color.rgb(55, 205, 230)
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildPage()
    }

    override fun onResume() {
        super.onResume()

        // Refresh the selected-theme indicator when returning
        // from the photo-theme screen.
        if (isFinishing) return

        if (contentViewReady) {
            buildPage()
        }
    }

    private var contentViewReady = false

    private fun buildPage() {
        val selected = VexoraThemeManager.selectedTheme(this)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(10), dp(14), dp(14))
            setBackgroundColor(Color.rgb(5, 8, 18))
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val back = Button(this).apply {
            text = "‹"
            textSize = 36f
            setTextColor(Color.WHITE)
            contentDescription = "Back"
            background = shape(
                Color.rgb(22, 28, 48),
                16,
                Color.rgb(39, 230, 236)
            )
            setOnClickListener { finish() }
        }

        header.addView(
            back,
            LinearLayout.LayoutParams(dp(54), dp(52))
        )

        val title = TextView(this).apply {
            text = "Vexora Themes"
            textSize = 24f
            setTextColor(Color.WHITE)
            setPadding(dp(12), 0, 0, 0)
        }

        header.addView(
            title,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        root.addView(header)

        val subtitle = TextView(this).apply {
            text = "Choose a keyboard style or use your own photo."
            textSize = 14f
            setTextColor(Color.LTGRAY)
            setPadding(dp(4), dp(10), dp(4), dp(12))
        }

        root.addView(subtitle)

        val scroll = ScrollView(this).apply {
            isFillViewport = false
            clipToPadding = false
        }

        val list = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        themes.forEach { option ->
            addThemeCard(
                list,
                option,
                selected == option.id
            )
        }

        val gallery = Button(this).apply {
            text = if (selected == "photo") {
                "✓  Gallery Photo Background"
            } else {
                "🖼️  Gallery Photo Background"
            }

            textSize = 16f
            setTextColor(Color.WHITE)
            background = shape(
                if (selected == "photo") {
                    Color.rgb(65, 45, 90)
                } else {
                    Color.rgb(43, 31, 75)
                },
                16,
                if (selected == "photo") {
                    Color.WHITE
                } else {
                    Color.rgb(188, 146, 205)
                }
            )

            setOnClickListener {
                openGallery()
            }
        }

        list.addView(
            gallery,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(58)
            ).apply {
                setMargins(0, dp(12), 0, dp(12))
            }
        )

        val note = TextView(this).apply {
            text =
                "Themes apply to the toolbar, keys, special keys and emoji panel."
            textSize = 13f
            setTextColor(Color.LTGRAY)
            setPadding(dp(4), dp(2), dp(4), dp(10))
        }

        list.addView(note)
        scroll.addView(list)

        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        setContentView(root)
        contentViewReady = true
    }

    private fun addThemeCard(
        container: LinearLayout,
        option: ThemeOption,
        selected: Boolean
    ) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(18), dp(10), dp(18), dp(10))

            background = shape(
                if (selected) {
                    mix(option.background, option.accent, 0.18f)
                } else {
                    option.background
                },
                18,
                if (selected) Color.WHITE else option.accent
            )

            contentDescription = "${option.title}, ${option.description}"
            isClickable = true
            isFocusable = true

            setOnClickListener {
                saveTheme(option.id, null)
            }
        }

        val name = TextView(this).apply {
            text = if (selected) {
                "✓  ${option.title}"
            } else {
                option.title
            }

            textSize = 19f
            setTextColor(Color.WHITE)
        }

        val detail = TextView(this).apply {
            text = option.description
            textSize = 13f
            setTextColor(option.accent)
            setPadding(0, dp(4), 0, 0)
        }

        card.addView(name)
        card.addView(detail)

        container.addView(
            card,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(86)
            ).apply {
                setMargins(0, dp(5), 0, dp(5))
            }
        )
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
            addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
            )
        }

        try {
            startActivityForResult(intent, PICKER_REQUEST)
        } catch (_: Exception) {
            Toast.makeText(
                this,
                "Unable to open the photo gallery.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    @Deprecated("Uses the compatible document-picker result callback")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != RESULT_OK) return

        when (requestCode) {
            PICKER_REQUEST -> {
                val uri: Uri = data?.data ?: return

                try {
                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: SecurityException) {
                    // The URI may still be usable for this session.
                } catch (_: Exception) {
                    // Continue and let PhotoThemeActivity validate it.
                }

                val intent = Intent(
                    this,
                    PhotoThemeActivity::class.java
                ).apply {
                    putExtra(
                        PhotoThemeActivity.EXTRA_PHOTO_URI,
                        uri.toString()
                    )
                }

                try {
                    startActivityForResult(
                        intent,
                        PHOTO_THEME_REQUEST
                    )
                } catch (_: Exception) {
                    Toast.makeText(
                        this,
                        "Unable to open photo theme settings.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            PHOTO_THEME_REQUEST -> {
                if (resultCode == RESULT_OK) {
                    setResult(RESULT_OK)
                }

                // Rebuild on resume to reflect the selected photo.
            }
        }
    }

    private fun saveTheme(
        theme: String,
        photoUri: String?
    ) {
        getSharedPreferences(
            VexoraThemeManager.PREFS_NAME,
            MODE_PRIVATE
        ).edit().apply {
            putString(
                VexoraThemeManager.KEY_THEME,
                theme
            )

            if (photoUri != null) {
                putString(
                    VexoraThemeManager.KEY_PHOTO,
                    photoUri
                )
            }
        }.apply()

        sendBroadcast(
            Intent(THEME_CHANGED_ACTION).apply {
                setPackage(packageName)
            }
        )

        setResult(RESULT_OK)
        finish()
    }

    private fun shape(
        color: Int,
        radius: Int,
        stroke: Int
    ): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
            setStroke(dp(2), stroke)
        }
    }

    private fun mix(
        first: Int,
        second: Int,
        amount: Float
    ): Int {
        val safeAmount = amount.coerceIn(0f, 1f)

        val r = (
                Color.red(first) * (1f - safeAmount) +
                        Color.red(second) * safeAmount
                ).toInt()

        val g = (
                Color.green(first) * (1f - safeAmount) +
                        Color.green(second) * safeAmount
                ).toInt()

        val b = (
                Color.blue(first) * (1f - safeAmount) +
                        Color.blue(second) * safeAmount
                ).toInt()

        return Color.rgb(r, g, b)
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}