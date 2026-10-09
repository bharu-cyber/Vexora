package com.example.vexora

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import kotlin.math.max

class PhotoThemeActivity : Activity() {

    companion object {
        const val EXTRA_PHOTO_URI = "photo_uri"

        private const val PREF_ZOOM = "photo_zoom"
        private const val PREF_OFFSET_X = "photo_offset_x"
        private const val PREF_OFFSET_Y = "photo_offset_y"
    }

    private var photoUri: Uri? = null

    private var brightness = 70

    // User photo adjustments
    private var photoZoom = 1.0f
    private var photoOffsetX = 0f
    private var photoOffsetY = 0f

    private lateinit var preview: ZoomableImageView
    private lateinit var brightnessValue: TextView
    private lateinit var zoomValue: TextView

    private lateinit var keyboardPreview: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uriString = intent.getStringExtra(EXTRA_PHOTO_URI)

        if (uriString.isNullOrBlank()) {
            finish()
            return
        }

        photoUri = Uri.parse(uriString)

        val prefs = getSharedPreferences(
            VexoraThemeManager.PREFS_NAME,
            MODE_PRIVATE
        )

        brightness = prefs.getInt(
            VexoraThemeManager.KEY_PHOTO_BRIGHTNESS,
            70
        ).coerceIn(0, 100)

        photoZoom = prefs.getFloat(
            PREF_ZOOM,
            1.0f
        ).coerceIn(1.0f, 3.0f)

        photoOffsetX = prefs.getFloat(
            PREF_OFFSET_X,
            0f
        )

        photoOffsetY = prefs.getFloat(
            PREF_OFFSET_Y,
            0f
        )

        buildScreen()
    }

    private fun buildScreen() {

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL

            setPadding(
                dp(16),
                dp(14),
                dp(16),
                dp(16)
            )

            setBackgroundColor(
                Color.rgb(5, 8, 18)
            )
        }

        // ---------------------------------------------------------
        // HEADER
        // ---------------------------------------------------------

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val back = Button(this).apply {
            text = "‹"
            textSize = 34f
            setTextColor(Color.WHITE)

            background = rounded(
                Color.rgb(22, 28, 48),
                Color.rgb(39, 230, 236),
                16
            )

            setOnClickListener {
                finish()
            }
        }

        header.addView(
            back,
            LinearLayout.LayoutParams(
                dp(54),
                dp(52)
            )
        )

        val title = TextView(this).apply {
            text = "Photo Theme"
            textSize = 23f
            setTextColor(Color.WHITE)
            setPadding(dp(12), 0, 0, 0)
        }

        header.addView(title)

        root.addView(header)

        val subtitle = TextView(this).apply {
            text = "Pinch to zoom • Drag to move • Adjust brightness"
            textSize = 14f
            setTextColor(Color.LTGRAY)

            setPadding(
                dp(4),
                dp(10),
                dp(4),
                dp(12)
            )
        }

        root.addView(subtitle)

        // ---------------------------------------------------------
        // PHOTO PREVIEW
        // ---------------------------------------------------------

        preview = ZoomableImageView(this)

        preview.setBackgroundColor(
            Color.rgb(12, 14, 22)
        )

        try {
            contentResolver.openInputStream(
                photoUri!!
            ).use { stream ->

                val bitmap =
                    stream?.let {
                        BitmapFactory.decodeStream(it)
                    }

                if (bitmap != null) {
                    preview.setImageBitmap(bitmap)
                }
            }
        } catch (_: Exception) {
        }

        preview.setPhotoValues(
            photoZoom,
            photoOffsetX,
            photoOffsetY
        )

        val previewFrame =
            LinearLayout(this).apply {

                gravity = Gravity.CENTER

                background = rounded(
                    Color.rgb(12, 14, 22),
                    Color.rgb(39, 230, 236),
                    18
                )

                setPadding(
                    dp(3),
                    dp(3),
                    dp(3),
                    dp(3)
                )

                addView(
                    preview,
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(250)
                    )
                )
            }

        root.addView(
            previewFrame,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(256)
            )
        )

        // ---------------------------------------------------------
        // ZOOM INFORMATION
        // ---------------------------------------------------------

        zoomValue = TextView(this).apply {
            text = "Zoom: ${((photoZoom - 1f) * 100f + 100f).toInt()}%"
            textSize = 14f
            setTextColor(
                Color.rgb(39, 230, 236)
            )

            gravity = Gravity.CENTER_HORIZONTAL

            setPadding(
                dp(4),
                dp(8),
                dp(4),
                dp(4)
            )
        }

        root.addView(zoomValue)

        // ---------------------------------------------------------
        // KEYBOARD PREVIEW
        // ---------------------------------------------------------

        val previewLabel = TextView(this).apply {
            text = "Keyboard preview"
            textSize = 15f
            setTextColor(Color.WHITE)

            setPadding(
                dp(4),
                dp(8),
                dp(4),
                dp(6)
            )
        }

        root.addView(previewLabel)

        keyboardPreview =
            LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER

                setPadding(
                    dp(5),
                    dp(5),
                    dp(5),
                    dp(5)
                )

                background = rounded(
                    Color.rgb(10, 12, 18),
                    Color.rgb(39, 230, 236),
                    12
                )
            }

        addPreviewRow(
            keyboardPreview,
            arrayOf(
                "Q", "W", "E", "R", "T",
                "Y", "U", "I", "O", "P"
            )
        )

        addPreviewRow(
            keyboardPreview,
            arrayOf(
                "A", "S", "D", "F", "G",
                "H", "J", "K", "L"
            )
        )

        addPreviewRow(
            keyboardPreview,
            arrayOf(
                "Z", "X", "C", "V",
                "B", "N", "M"
            )
        )

        root.addView(
            keyboardPreview,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(116)
            )
        )

        // ---------------------------------------------------------
        // BRIGHTNESS
        // ---------------------------------------------------------

        val brightnessTitle =
            TextView(this).apply {

                text = "Adjust Brightness"
                textSize = 17f
                setTextColor(Color.WHITE)

                setPadding(
                    dp(4),
                    dp(12),
                    dp(4),
                    dp(2)
                )
            }

        root.addView(brightnessTitle)

        brightnessValue =
            TextView(this).apply {

                text = "$brightness%"
                textSize = 14f

                setTextColor(
                    Color.rgb(39, 230, 236)
                )

                gravity = Gravity.CENTER_HORIZONTAL
            }

        root.addView(
            brightnessValue,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(28)
            )
        )

        val seekBar =
            SeekBar(this).apply {

                max = 100
                progress = brightness

                setOnSeekBarChangeListener(
                    object :
                        SeekBar.OnSeekBarChangeListener {

                        override fun onProgressChanged(
                            seekBar: SeekBar?,
                            progress: Int,
                            fromUser: Boolean
                        ) {
                            brightness = progress

                            brightnessValue.text =
                                "$progress%"

                            updatePreviewKeys()
                        }

                        override fun onStartTrackingTouch(
                            seekBar: SeekBar?
                        ) {
                        }

                        override fun onStopTrackingTouch(
                            seekBar: SeekBar?
                        ) {
                        }
                    }
                )
            }

        root.addView(
            seekBar,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(45)
            )
        )

        val hint = TextView(this).apply {
            text =
                "Higher brightness makes the photo more visible through the keyboard keys."

            textSize = 12f
            setTextColor(Color.LTGRAY)

            setPadding(
                dp(4),
                dp(2),
                dp(4),
                dp(8)
            )
        }

        root.addView(hint)

        // ---------------------------------------------------------
        // SAVE
        // ---------------------------------------------------------

        val save = Button(this).apply {

            text = "✓  Save Photo Theme"
            textSize = 16f
            setTextColor(Color.WHITE)

            background = rounded(
                Color.rgb(25, 96, 105),
                Color.rgb(39, 230, 236),
                16
            )

            setOnClickListener {
                savePhotoTheme()
            }
        }

        root.addView(
            save,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(56)
            )
        )

        setContentView(root)

        updatePreviewKeys()

        preview.onPhotoChanged = {
            photoZoom = preview.getPhotoZoom()
            photoOffsetX = preview.getOffsetX()
            photoOffsetY = preview.getOffsetY()

            updateZoomText()
        }
    }

    // ---------------------------------------------------------
    // KEYBOARD PREVIEW
    // ---------------------------------------------------------

    private fun addPreviewRow(
        parent: LinearLayout,
        letters: Array<String>
    ) {

        val row =
            LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
            }

        letters.forEach { letter ->

            val key =
                TextView(this).apply {

                    text = letter
                    textSize = 12f
                    gravity = Gravity.CENTER
                    setTextColor(Color.WHITE)

                    background = rounded(
                        Color.argb(
                            keyAlpha(),
                            15,
                            20,
                            35
                        ),
                        Color.rgb(
                            39,
                            230,
                            236
                        ),
                        8
                    )
                }

            row.addView(
                key,
                LinearLayout.LayoutParams(
                    0,
                    dp(29),
                    1f
                ).apply {
                    setMargins(
                        dp(2),
                        dp(2),
                        dp(2),
                        dp(2)
                    )
                }
            )
        }

        parent.addView(
            row,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(36)
            )
        )
    }

    private fun updatePreviewKeys() {

        val alpha = keyAlpha()

        for (i in 0 until keyboardPreview.childCount) {

            val row =
                keyboardPreview.getChildAt(i)
                        as? LinearLayout
                    ?: continue

            for (j in 0 until row.childCount) {

                val key =
                    row.getChildAt(j)
                            as? TextView
                        ?: continue

                key.background =
                    rounded(
                        Color.argb(
                            alpha,
                            15,
                            20,
                            35
                        ),
                        Color.rgb(
                            39,
                            230,
                            236
                        ),
                        8
                    )
            }
        }
    }

    private fun keyAlpha(): Int {

        // 0%  = very opaque
        // 100% = very transparent

        return (
                225 -
                        ((brightness * 185) / 100)
                ).coerceIn(40, 225)
    }

    // ---------------------------------------------------------
    // SAVE EVERYTHING
    // ---------------------------------------------------------

    private fun savePhotoTheme() {

        val uri = photoUri ?: return

        // Get the latest values directly from the preview.
        photoZoom = preview.getPhotoZoom()
        photoOffsetX = preview.getOffsetX()
        photoOffsetY = preview.getOffsetY()

        getSharedPreferences(
            VexoraThemeManager.PREFS_NAME,
            MODE_PRIVATE
        )
            .edit()
            .putString(
                VexoraThemeManager.KEY_THEME,
                VexoraThemeManager.PHOTO
            )
            .putString(
                VexoraThemeManager.KEY_PHOTO,
                uri.toString()
            )
            .putInt(
                VexoraThemeManager.KEY_PHOTO_BRIGHTNESS,
                brightness
            )
            .putFloat(
                PREF_ZOOM,
                photoZoom
            )
            .putFloat(
                PREF_OFFSET_X,
                photoOffsetX
            )
            .putFloat(
                PREF_OFFSET_Y,
                photoOffsetY
            )
            .apply()

        sendBroadcast(
            Intent(
                "com.example.vexora.THEME_CHANGED"
            ).apply {
                setPackage(packageName)
            }
        )

        setResult(RESULT_OK)
        finish()
    }

    private fun updateZoomText() {

        val percent =
            (photoZoom * 100f).toInt()

        zoomValue.text =
            "Zoom: $percent%"
    }

    // ---------------------------------------------------------
    // ROUNDED BACKGROUND
    // ---------------------------------------------------------

    private fun rounded(
        color: Int,
        stroke: Int,
        radius: Int
    ): GradientDrawable {

        return GradientDrawable().apply {

            setColor(color)

            cornerRadius =
                dp(radius).toFloat()

            setStroke(
                dp(1),
                stroke
            )
        }
    }

    private fun dp(value: Int): Int =
        (
                value *
                        resources.displayMetrics.density
                ).toInt()

    // =========================================================
    // ZOOM + DRAG IMAGE VIEW
    // =========================================================

    class ZoomableImageView(
        context: android.content.Context
    ) : ImageView(context) {

        private var bitmap: Bitmap? = null

        private var photoZoom = 1.0f
        private var offsetX = 0f
        private var offsetY = 0f

        private var lastX = 0f
        private var lastY = 0f

        private var dragging = false

        private var scaleDetector:
                ScaleGestureDetector

        var onPhotoChanged: (() -> Unit)? = null

        init {

            scaleType = ScaleType.MATRIX

            scaleDetector =
                ScaleGestureDetector(
                    context,
                    object :
                        ScaleGestureDetector
                        .SimpleOnScaleGestureListener() {

                        override fun onScale(
                            detector: ScaleGestureDetector
                        ): Boolean {

                            photoZoom *=
                                detector.scaleFactor

                            photoZoom =
                                photoZoom.coerceIn(
                                    1.0f,
                                    3.0f
                                )

                            updateMatrix()

                            onPhotoChanged?.invoke()

                            return true
                        }
                    }
                )
        }

        override fun setImageBitmap(
            bm: Bitmap?
        ) {
            bitmap = bm
            super.setImageBitmap(bm)

            post {
                updateMatrix()
            }
        }

        fun setPhotoValues(
            zoom: Float,
            x: Float,
            y: Float
        ) {
            photoZoom =
                zoom.coerceIn(
                    1.0f,
                    3.0f
                )

            offsetX = x
            offsetY = y

            post {
                updateMatrix()
            }
        }

        fun getPhotoZoom(): Float =
            photoZoom

        fun getOffsetX(): Float =
            offsetX

        fun getOffsetY(): Float =
            offsetY

        override fun onTouchEvent(
            event: MotionEvent
        ): Boolean {

            scaleDetector.onTouchEvent(event)

            when (event.actionMasked) {

                MotionEvent.ACTION_DOWN -> {

                    lastX = event.x
                    lastY = event.y

                    dragging = true

                    return true
                }

                MotionEvent.ACTION_MOVE -> {

                    if (!scaleDetector.isInProgress &&
                        dragging
                    ) {

                        val dx =
                            event.x - lastX

                        val dy =
                            event.y - lastY

                        offsetX += dx
                        offsetY += dy

                        lastX = event.x
                        lastY = event.y

                        updateMatrix()

                        onPhotoChanged?.invoke()
                    }

                    return true
                }

                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL -> {

                    dragging = false

                    return true
                }
            }

            return true
        }

        private fun updateMatrix() {

            val bm = bitmap ?: return

            if (width <= 0 ||
                height <= 0
            ) {
                return
            }

            val bitmapWidth =
                bm.width.toFloat()

            val bitmapHeight =
                bm.height.toFloat()

            // Cover the entire preview.
            val baseScale =
                max(
                    width / bitmapWidth,
                    height / bitmapHeight
                )

            val finalScale =
                baseScale * photoZoom

            val scaledWidth =
                bitmapWidth * finalScale

            val scaledHeight =
                bitmapHeight * finalScale

            // Center the image first.
            val centerX =
                (width - scaledWidth) / 2f

            val centerY =
                (height - scaledHeight) / 2f

            val matrix =
                Matrix()

            matrix.setScale(
                finalScale,
                finalScale
            )

            matrix.postTranslate(
                centerX + offsetX,
                centerY + offsetY
            )

            imageMatrix = matrix
        }
    }
}