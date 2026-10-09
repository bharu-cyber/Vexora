package com.example.vexora

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.inputmethodservice.InputMethodService
import android.view.inputmethod.InputMethodManager
import android.net.Uri
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.core.content.ContextCompat

class VexoraKeyboardService : InputMethodService() {

    companion object {
        private const val THEME_CHANGED_ACTION = "com.example.vexora.THEME_CHANGED"

        private const val PHOTO_ZOOM_KEY = "photo_zoom"
        private const val PHOTO_OFFSET_X_KEY = "photo_offset_x"
        private const val PHOTO_OFFSET_Y_KEY = "photo_offset_y"

        private const val PHOTO_IMAGE_ALPHA = 235
        private const val PHOTO_OVERLAY_ALPHA = 35
    }

    private lateinit var keyboardLayout: LinearLayout
    private var numberMode = false
    private var emojiMode = false
    private var emojiCategory = 0
    private var carrierLength = 0

    // Letter keyboard state
    private var shiftEnabled = false
    private var capsLock = false
    private var lastShiftTap = 0L

    // Symbol keyboard state
    private var symbolPage = false

    private val emojiCategories = listOf(
        "😀" to listOf("😀","😃","😄","😁","😆","😅","😂","🤣","🥲","☺️","😊","😇","🙂","🙃","😉","😌","😍","🥰","😘","😗","😙","😚","😋","😛","😝","😜","🤪","🤨","🧐","🤓","😎","🥸","🤩","🥳","🙂‍↔️","😏","😒","🙂‍↕️","😞","😔","😟","😕","🙁","☹️","😣","😖","😫","😩","🥺","😢","😭","😤","😠","😡","🤬","🤯","😳","🥵","🥶","😱","😨","😰","😥","😓","🫣","🤗","🫡","🤔","🫢","🤭","🤫","🤥","😶","😐","😑","😬","🫨","🫠","🙄","😯","😦","😧","😮","😲","🥱","😴","🤤","😪","😮‍💨","😵","🤐","🥴","🤢","🤮","🤧","😷","🤒","🤕","🤑","🤠","😈","👿","👹","👺","🤡","💩","👻","💀","☠️","👽","👾","🤖"),
        "👋" to listOf("👋","🤚","🖐️","✋","🖖","🫱","🫲","🫳","🫴","👌","🤌","🤏","✌️","🤞","🫰","🤟","🤘","🤙","👈","👉","👆","👇","☝️","🫵","👍","👎","✊","👊","🤛","🤜","👏","🙌","🫶","👐","🤲","🤝","🙏","✍️","💅","🤳","💪","🦾","🦿","🦵","🦶","👂","🦻","👃","🧠","🫀","🫁","🦷","🦴","👀","👁️","👅","👄","💋","🩸"),
        "🐶" to listOf("🐶","🐱","🐭","🐹","🐰","🦊","🐻","🐼","🐻‍❄️","🐨","🐯","🦁","🐮","🐷","🐸","🐵","🙈","🙉","🙊","🐒","🐔","🐧","🐦","🐤","🦆","🦅","🦉","🦇","🐺","🐗","🐴","🦄","🐝","🪱","🐛","🦋","🐌","🐞","🐜","🪰","🪲","🦟","🦗","🕷️","🦂","🐢","🐍","🦎","🦖","🦕","🐙","🦑","🦐","🦞","🦀","🐡","🐠","🐟","🐬","🐳","🐋","🦈","🐊","🐅","🐆","🦓","🦍","🦧","🦣","🐘","🦛","🦏","🐪","🐫","🦒","🦘","🦬","🐃","🐂","🐄","🐎","🐖","🐏","🐑","🦙","🐐","🦌","🐕","🐩","🦮","🐈","🐓","🦃","🦚","🦜","🦢","🪿","🦩","🕊️","🐇","🦝","🦨","🦡","🦫","🦦","🦥","🐁","🐀","🐿️","🦔"),
        "🍔" to listOf("🍏","🍎","🍐","🍊","🍋","🍌","🍉","🍇","🍓","🫐","🍈","🍒","🍑","🥭","🍍","🥥","🥝","🍅","🍆","🥑","🥦","🥬","🥒","🌶️","🫑","🌽","🥕","🫒","🧄","🧅","🥔","🍠","🫘","🥐","🥯","🍞","🥖","🥨","🧀","🥚","🍳","🧈","🥞","🧇","🥓","🥩","🍗","🍖","🌭","🍔","🍟","🍕","🫓","🥪","🥙","🧆","🌮","🌯","🫔","🥗","🥘","🫕","🥫","🍝","🍜","🍲","🍛","🍣","🍱","🥟","🦪","🍤","🍙","🍚","🍘","🍥","🥠","🥮","🍢","🍡","🍧","🍨","🍦","🥧","🧁","🍰","🎂","🍮","🍭","🍬","🍫","🍿","🍩","🍪","🌰","🥜","🍯","🥛","🍼","☕","🫖","🍵","🧃","🥤","🧋","🍶","🍺","🍻","🥂","🍷","🥃","🍸","🍹","🧉","🍾"),
        "⚽" to listOf("⚽","🏀","🏈","⚾","🥎","🎾","🏐","🏉","🥏","🎱","🪀","🏓","🏸","🏒","🏑","🥍","🏏","🪃","🥅","⛳","🪁","🛝","🏹","🎣","🤿","🥊","🥋","🎽","🛹","🛼","🛷","⛸️","🥌","🎿","⛷️","🏂","🪂","🏋️","🤼","🤸","⛹️","🤺","🤾","🏌️","🏇","🧘","🏄","🏊","🤽","🚣","🧗","🚴","🚵","🎯","🎮","🕹️","🎲","♟️","🎭","🎨","🧵","🪡","🧶","🪢"),
        "🚗" to listOf("🚗","🚕","🚙","🚌","🚎","🏎️","🚓","🚑","🚒","🚐","🛻","🚚","🚛","🚜","🛵","🏍️","🛺","🚲","🛴","🛹","🛼","🚏","🛣️","🛤️","🛢️","⛽","🚨","🚥","🚦","🛑","🚧","⚓","🛟","⛵","🛶","🚤","🛳️","⛴️","🛥️","🚢","✈️","🛩️","🛫","🛬","🪂","💺","🚁","🚟","🚠","🚡","🛰️","🚀","🛸","🌍","🌎","🌏","🌐","🗺️","🧭","🏔️","⛰️","🌋","🗻","🏕️","🏖️","🏜️","🏝️","🏞️","🏟️","🏛️","🏗️","🧱","🪨","🪵","🛖","🏘️","🏚️","🏠","🏡","🏢","🏣","🏤","🏥","🏦","🏨","🏩","🏪","🏫","🏬","🏭","🏯","🏰","💒","🗼","🗽","⛪","🕌","🛕","🕍","⛩️","🕋","⛲","⛺","🌁","🌃","🏙️","🌄","🌅","🌆","🌇","🌉","♨️","🎠","🛝","🎡","🎢","💈"),
        "❤️" to listOf("❤️","🩷","🧡","💛","💚","💙","🩵","💜","🤎","🖤","🩶","🤍","💔","❤️‍🔥","❤️‍🩹","💕","💞","💓","💗","💖","💘","💝","💟","❣️","💌","💋","💯","💢","💥","💫","💦","💨","🕳️","💬","👁️‍🗨️","🗨️","🗯️","💭","💤","✨","🌟","⭐","🌠","☀️","🌤️","⛅","🌥️","☁️","🌦️","🌧️","⛈️","🌩️","🌨️","❄️","☃️","⛄","🌬️","💧","🔥","🌈","☔","⚡","🌪️","🌊","🎉","🎊","🎈","🎁","🏆","🥇","🥈","🥉","🎖️","🏅","🔔","🔕","🎵","🎶","💡","🔦","🕯️","🪄","🧿","🔮","🪬","🪩","🎀","🎗️","🎟️","🎫","🪅","🪆","🧸","🪅","🪄","📱","💻","⌨️","🖥️","🛡️","🔒","🔓","🔑","🗝️","⚙️","🧰","🧲","💎","📌","📍","✂️","🖊️","📝","📚","📖","📦","📫","📮","📅","📆","🗓️","⏰","⏳","⌛","⏱️","🔋","🔌","💰","💳","💸","🪙","💹","📈","📉","✔️","☑️","❌","❗","❓","‼️","⁉️","⭕","🚫","🔞","♻️","⚕️","⚠️","🚸","🔰","🔱","〽️","©️","®️","™️","#️⃣","*️⃣","0️⃣","1️⃣","2️⃣","3️⃣","4️⃣","5️⃣","6️⃣","7️⃣","8️⃣","9️⃣","🔟","🔠","🔡","🔢","🔣","🔤","🆗","🆕","🆒","🆓","🆙","🆘","🆚","🅰️","🅱️","🆎","🅾️","🆑","🅿️","🈳","🈯","🈹","🈚","🈲","🉐","🈴","🈵","🈶","🈷️","🈸","🈺","🈂️","🌐","💠","🔘","🔴","🟠","🟡","🟢","🔵","🟣","⚫","⚪","🟤","🔺","🔻","🔸","🔹","🔶","🔷","🔳","🔲","▪️","▫️","◾","◽","◼️","◻️","⬛","⬜","🟥","🟧","🟨","🟩","🟦","🟪","🟫","🏁","🚩","🎌","🏴","🏳️","🏳️‍🌈","🏳️‍⚧️"),
        "👨‍👩‍👧‍👦" to listOf("👶","🧒","👦","👧","🧑","👱","👨","🧔","👨‍🦰","👨‍🦱","👨‍🦳","👨‍🦲","👩","👩‍🦰","👩‍🦱","👩‍🦳","👩‍🦲","🧓","👴","👵","🧑‍⚕️","👨‍⚕️","👩‍⚕️","🧑‍💻","👨‍💻","👩‍💻","🧑‍🎓","👨‍🎓","👩‍🎓","🧑‍🏫","👨‍🏫","👩‍🏫","🧑‍🚀","👨‍🚀","👩‍🚀","👮","👮‍♂️","👮‍♀️","🕵️","🕵️‍♂️","🕵️‍♀️","💂","💂‍♂️","💂‍♀️","👷","👷‍♂️","👷‍♀️","🤴","👸","👳","👳‍♂️","👳‍♀️","🧕","🤵","👰","🤰","🫃","🫄","🍼","👨‍👩‍👦","👨‍👩‍👧","👨‍👩‍👧‍👦","👩‍👩‍👦","👨‍👨‍👧","👪"),
        "🎬" to listOf("🎬","🎥","📽️","📺","📷","📸","📹","🎞️","📞","☎️","📱","💻","🖥️","⌨️","🖱️","🖨️","🎙️","🎚️","🎛️","📻","🎤","🎧","🎼","🎹","🥁","🪘","🎷","🎺","🎸","🪕","🎻","🎵","🎶","🎭","🎨","🖌️","🖍️","🧩","🎲","🎯","🎮","🕹️","🃏","🀄","♟️","🎳","🎰","🎪","🎠","🎡","🎢","🎟️","🎫"),
        "🛠️" to listOf("🛠️","🔨","🪓","⛏️","⚒️","🔧","🪛","🔩","⚙️","🗜️","⚖️","🦯","🔗","⛓️","🪝","🧰","🧲","🪜","🧱","🪚","🪣","🧹","🧺","🧻","🪠","🧴","🧷","🧵","🪡","🧶","🔑","🗝️","🔒","🔓","🔐","💡","🔦","🕯️","🔋","🔌","💾","💿","📀","📼","📡","🛰️","📟","📠","🖲️","💳","💰","🧮"),
        "🔣" to listOf("🔣","©️","®️","™️","ℹ️","‼️","⁉️","❗","❕","❓","❔","⭕","❌","❎","✔️","☑️","➕","➖","➗","✖️","🟰","♾️","💲","#️⃣","*️⃣","0️⃣","1️⃣","2️⃣","3️⃣","4️⃣","5️⃣","6️⃣","7️⃣","8️⃣","9️⃣","🔟","🔠","🔡","🔢","🔣","🔤","🔼","🔽","⏪","⏩","⏫","⏬","⬅️","➡️","⬆️","⬇️","↩️","↪️","🔀","🔁","🔂","🔄","🔃","▶️","⏸️","⏹️","⏺️","🔘","🔴","🟠","🟡","🟢","🔵","🟣","⚫","⚪","🟤","🔺","🔻","🔸","🔹","🔶","🔷","🔳","🔲"),
        "🌿" to listOf("🌿","🌱","🌲","🌳","🌴","🌵","🎋","🎍","🍀","🍁","🍂","🍃","🍄","🌾","💐","🌷","🌹","🥀","🌺","🌸","🌼","🌻","🪻","🪷","🪴","🌎","🌍","🌏","🌙","🌚","🌝","🌞","⭐","🌟","✨","🌈","☀️","🌤️","⛅","🌥️","☁️","🌦️","🌧️","⛈️","🌩️","🌨️","❄️","☃️","⛄","🌬️","💨","💧","💦","🔥","🌊","⚡","☄️","🌪️","🌫️","🪐","🌌","🌠"),
        "🏳️" to listOf("🏳️","🏴","🏁","🚩","🎌","🏳️‍🌈","🏳️‍⚧️","🇮🇳","🇺🇸","🇬🇧","🇨🇦","🇦🇺","🇯🇵","🇰🇷","🇨🇳","🇫🇷","🇩🇪","🇮🇹","🇪🇸","🇧🇷","🇷🇺","🇦🇪","🇸🇬","🇲🇾","🇹🇭","🇱🇰","🇳🇵","🇵🇰","🇧🇩","🇳🇿","🇿🇦","🇲🇽","🇦🇷","🇪🇬","🇹🇷","🇸🇦","🇮🇩","🇵🇭","🇻🇳","🇵🇹","🇳🇱","🇸🇪","🇳🇴","🇩🇰","🇫🇮","🇵🇱","🇬🇷","🇮🇪","🇨🇭","🇦🇹","🇧🇪","🇺🇦","🇮🇱","🇵🇸","🇺🇳")
    )

    private val themeChangedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == THEME_CHANGED_ACTION && ::keyboardLayout.isInitialized) {
                refreshKeyboardBackground()
                buildKeyboard()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        ContextCompat.registerReceiver(
            this,
            themeChangedReceiver,
            IntentFilter(THEME_CHANGED_ACTION),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onDestroy() {
        try {
            unregisterReceiver(themeChangedReceiver)
        } catch (_: Exception) {
        }
        super.onDestroy()
    }

    override fun onCreateInputView(): View {
        keyboardLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(5), dp(4), dp(5), dp(5))
        }
        refreshKeyboardBackground()
        buildKeyboard()
        return keyboardLayout
    }

    private fun refreshKeyboardBackground() {
        if (!::keyboardLayout.isInitialized) return

        val palette = VexoraThemeManager.palette(this)
        val theme = VexoraThemeManager.selectedTheme(this)
        val photo = VexoraThemeManager.selectedPhoto(this)

        if (theme == VexoraThemeManager.PHOTO && !photo.isNullOrBlank()) {
            try {
                contentResolver.openInputStream(Uri.parse(photo)).use { stream ->
                    val bitmap = stream?.let { BitmapFactory.decodeStream(it) }

                    if (bitmap != null) {
                        val prefs = getSharedPreferences(
                            VexoraThemeManager.PREFS_NAME,
                            MODE_PRIVATE
                        )

                        val zoom = prefs.getFloat(
                            PHOTO_ZOOM_KEY,
                            1.0f
                        ).coerceIn(1.0f, 3.0f)

                        val offsetX = prefs.getFloat(
                            PHOTO_OFFSET_X_KEY,
                            0f
                        )

                        val offsetY = prefs.getFloat(
                            PHOTO_OFFSET_Y_KEY,
                            0f
                        )

                        val photoDrawable = PhotoBackgroundDrawable(
                            bitmap = bitmap,
                            zoom = zoom,
                            offsetX = offsetX,
                            offsetY = offsetY
                        ).apply {
                            alpha = PHOTO_IMAGE_ALPHA
                        }

                        val overlay = android.graphics.drawable.ColorDrawable(
                            Color.argb(
                                PHOTO_OVERLAY_ALPHA,
                                0,
                                0,
                                0
                            )
                        )

                        keyboardLayout.background = android.graphics.drawable.LayerDrawable(
                            arrayOf(photoDrawable, overlay)
                        )
                        return
                    }
                }
            } catch (_: Exception) {
                // Fall back to the selected palette if the photo cannot be loaded.
            }
        }

        keyboardLayout.setBackgroundColor(palette.background)
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        refreshKeyboardBackground()
        if (::keyboardLayout.isInitialized) buildKeyboard()
        insertPendingMessage()

    }

    private fun buildKeyboard() {
        keyboardLayout.removeAllViews()
        addToolbar()
        addDivider()
        when {
            emojiMode -> buildEmojiKeyboard()
            numberMode -> buildNumberKeyboard()
            else -> buildLetterKeyboard()
        }
    }

    private fun addToolbar() {
        val palette = VexoraThemeManager.palette(this)
        val toolbar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = roundedBackground(Color.argb(235, Color.red(palette.special), Color.green(palette.special), Color.blue(palette.special)), 12, palette.accent)
        }
        addToolbarButton(toolbar, "😀", "Emoji") {
            emojiMode = !emojiMode
            numberMode = false
            buildKeyboard()
        }
        addToolbarButton(toolbar, "🎨", "Themes") { openThemeActivity() }
        addToolbarButton(toolbar, "✨", "Vexora") { openVexora() }
        addToolbarButton(toolbar, "⚙", "Settings") { openKeyboardSettings() }
        keyboardLayout.addView(toolbar, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(42)))
    }

    private fun addDivider() {
        val accent = VexoraThemeManager.palette(this).accent
        val divider = View(this).apply { setBackgroundColor(Color.argb(180, Color.red(accent), Color.green(accent), Color.blue(accent))) }
        keyboardLayout.addView(divider, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(1)))
    }

    private fun buildLetterKeyboard() {
        addLetterRow("qwertyuiop")
        addLetterRow("asdfghjkl")
        addShiftLetterRow("zxcvbnm")
        addBottomRow()
    }

    private fun buildNumberKeyboard() {
        if (!symbolPage) {
            addSymbolRow(arrayOf("1","2","3","4","5","6","7","8","9","0"))
            addSymbolRow(arrayOf("@","#","$","%","&","*","-","+","(",")"))
            addSymbolRow(arrayOf("_","=","/",":",";","!","?","'","\"",","))
        } else {
            addSymbolRow(arrayOf("~","`","|","•","√","π","÷","×","¶","∆"))
            addSymbolRow(arrayOf("€","£","¥","₹","₩","©","®","™","°","±"))
            addSymbolRow(arrayOf("<",">","[","]","{","}","^","\\","%","…"))
        }
        addNumberBottomRow()
    }

    private fun buildEmojiKeyboard() {
        val categoryScroll = HorizontalScrollView(this).apply { isHorizontalScrollBarEnabled = false }
        val categoriesRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        emojiCategories.forEachIndexed { index, category ->
            val selected = index == emojiCategory
            val button = Button(this).apply {
                text = category.first
                textSize = 19f
                setTextColor(Color.WHITE)
                background = roundedBackground(
                    if (selected) VexoraThemeManager.palette(this@VexoraKeyboardService).special else VexoraThemeManager.palette(this@VexoraKeyboardService).key,
                    9,
                    if (selected) VexoraThemeManager.palette(this@VexoraKeyboardService).accent else Color.argb(160, Color.red(VexoraThemeManager.palette(this@VexoraKeyboardService).accent), Color.green(VexoraThemeManager.palette(this@VexoraKeyboardService).accent), Color.blue(VexoraThemeManager.palette(this@VexoraKeyboardService).accent))
                )
                setPadding(0, 0, 0, 0)
                minWidth = 0; minimumWidth = 0; minHeight = 0; minimumHeight = 0
                setOnClickListener { emojiCategory = index; buildKeyboard() }
            }
            categoriesRow.addView(button, LinearLayout.LayoutParams(dp(40), dp(34)).apply { setMargins(dp(2), dp(2), dp(2), dp(2)) })
        }
        categoryScroll.addView(categoriesRow)
        keyboardLayout.addView(categoryScroll, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(38)))

        val emojiScroll = ScrollView(this).apply { isVerticalScrollBarEnabled = true; overScrollMode = View.OVER_SCROLL_IF_CONTENT_SCROLLS }
        val emojiRows = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val emojis = emojiCategories[emojiCategory].second
        val columns = 8
        emojis.chunked(columns).forEach { group ->
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER }
            group.forEach { emoji ->
                val palette = VexoraThemeManager.palette(this@VexoraKeyboardService)
                val button = Button(this).apply {
                    text = emoji
                    textSize = 22f
                    setTextColor(palette.text)
                    background = roundedBackground(
                        palette.key,
                        8,
                        Color.argb(
                            180,
                            Color.red(palette.accent),
                            Color.green(palette.accent),
                            Color.blue(palette.accent)
                        )
                    )
                    setPadding(0, 0, 0, 0)
                    minWidth = 0; minimumWidth = 0; minHeight = 0; minimumHeight = 0
                    stateListAnimator = null
                    setOnClickListener { currentInputConnection?.commitText(emoji, 1) }
                }
                row.addView(button, LinearLayout.LayoutParams(0, dp(40), 1f).apply { setMargins(dp(1), dp(1), dp(1), dp(1)) })
            }
            repeat(columns - group.size) {
                row.addView(View(this), LinearLayout.LayoutParams(0, dp(40), 1f).apply { setMargins(dp(1), dp(1), dp(1), dp(1)) })
            }
            emojiRows.addView(row)
        }
        emojiScroll.addView(emojiRows)
        keyboardLayout.addView(emojiScroll, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(150)))
        addEmojiBottomRow()
    }

    private fun addLetterRow(letters: String) {
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER }
        letters.forEach { letter ->
            val display = if (shiftEnabled || capsLock) letter.uppercase() else letter.toString()
            val button = createKey(display) {
                val output = if (shiftEnabled || capsLock) letter.uppercase() else letter.toString()
                currentInputConnection?.commitText(output, 1)
                if (shiftEnabled && !capsLock) {
                    shiftEnabled = false
                    buildKeyboard()
                }
            }
            row.addView(button, LinearLayout.LayoutParams(0, dp(47), 1f).apply { setMargins(dp(2), dp(2), dp(2), dp(2)) })
        }
        keyboardLayout.addView(row, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(51)))
    }

    private fun addShiftLetterRow(letters: String) {
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER }
        val shiftButton = createKey(if (capsLock) "🔒" else "⇧") {
            val now = System.currentTimeMillis()
            if (capsLock) {
                capsLock = false
                shiftEnabled = false
            } else if (now - lastShiftTap < 400L) {
                capsLock = true
                shiftEnabled = true
            } else {
                shiftEnabled = !shiftEnabled
            }
            lastShiftTap = now
            buildKeyboard()
        }
        row.addView(shiftButton, LinearLayout.LayoutParams(0, dp(47), 1.15f).apply { setMargins(dp(2), dp(2), dp(2), dp(2)) })
        letters.forEach { letter ->
            val display = if (shiftEnabled || capsLock) letter.uppercase() else letter.toString()
            val button = createKey(display) {
                val output = if (shiftEnabled || capsLock) letter.uppercase() else letter.toString()
                currentInputConnection?.commitText(output, 1)
                if (shiftEnabled && !capsLock) {
                    shiftEnabled = false
                    buildKeyboard()
                }
            }
            row.addView(button, LinearLayout.LayoutParams(0, dp(47), 1f).apply { setMargins(dp(2), dp(2), dp(2), dp(2)) })
        }
        keyboardLayout.addView(row, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(51)))
    }

    private fun addSymbolRow(symbols: Array<String>) {
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER }
        symbols.forEach { symbol ->
            val button = createKey(symbol) { currentInputConnection?.commitText(symbol, 1) }
            row.addView(button, LinearLayout.LayoutParams(0, dp(47), 1f).apply { setMargins(dp(2), dp(2), dp(2), dp(2)) })
        }
        keyboardLayout.addView(row, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(51)))
    }

    private fun addBottomRow() {
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER }
        addBottomButton(row, "123", 1f) { numberMode = true; symbolPage = false; emojiMode = false; buildKeyboard() }
        addBottomButton(row, "🌐", 1f) { switchKeyboard() }
        addBottomButton(row, "SPACE", 4f) { currentInputConnection?.commitText(" ", 1) }
        addBottomButton(row, "⌫", 1.2f) { deleteCharacter() }
        keyboardLayout.addView(row, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(52)))
    }

    private fun addNumberBottomRow() {
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER }
        addBottomButton(row, "ABC", 1f) { numberMode = false; symbolPage = false; emojiMode = false; buildKeyboard() }
        addBottomButton(row, if (symbolPage) "123" else "#+=", 1f) { symbolPage = !symbolPage; buildKeyboard() }
        addBottomButton(row, "🌐", 1f) { switchKeyboard() }
        addBottomButton(row, "SPACE", 3f) { currentInputConnection?.commitText(" ", 1) }
        addBottomButton(row, "⌫", 1.2f) { deleteCharacter() }
        keyboardLayout.addView(row, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(52)))
    }

    private fun addEmojiBottomRow() {
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER }
        addBottomButton(row, "ABC", 1f) { emojiMode = false; numberMode = false; symbolPage = false; buildKeyboard() }
        addBottomButton(row, "🌐", 1f) { switchKeyboard() }
        addBottomButton(row, "SPACE", 3f) { currentInputConnection?.commitText(" ", 1) }
        addBottomButton(row, "⌫", 1.2f) { deleteCharacter() }
        keyboardLayout.addView(row, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(52)))
    }

    private fun addToolbarButton(toolbar: LinearLayout, text: String, description: String, action: () -> Unit) {
        val palette = VexoraThemeManager.palette(this)
        val button = Button(this).apply {
            this.text = text
            contentDescription = description
            textSize = 19f
            setTextColor(Color.WHITE)
            background = roundedBackground(Color.argb(70, Color.red(palette.accent), Color.green(palette.accent), Color.blue(palette.accent)), 10, palette.accent)
            setPadding(0, 0, 0, 0)
            minHeight = 0; minimumHeight = 0; minWidth = 0; minimumWidth = 0
            stateListAnimator = null
            setOnClickListener { action() }
        }
        toolbar.addView(button, LinearLayout.LayoutParams(0, dp(38), 1f).apply { setMargins(dp(3), dp(2), dp(3), dp(2)) })
    }

    private fun addBottomButton(row: LinearLayout, text: String, weight: Float, action: () -> Unit) {
        val button = createKey(text, action)
        val params = LinearLayout.LayoutParams(0, dp(47), weight)
        params.setMargins(dp(2), dp(2), dp(2), dp(2))
        row.addView(button, params)
    }

    private fun createKey(text: String, action: () -> Unit): Button {
        val palette = VexoraThemeManager.palette(this)
        val special = text == "SPACE" ||
                text == "⌫" ||
                text == "123" ||
                text == "ABC" ||
                text == "#+=" ||
                text == "🌐" ||
                text == "⇧" ||
                text == "🔒"

        val isPhoto =
            VexoraThemeManager.selectedTheme(this) == VexoraThemeManager.PHOTO

        val keyBackground = if (isPhoto) {
            if (special) {
                Color.argb(
                    photoSpecialKeyAlpha(),
                    15,
                    24,
                    42
                )
            } else {
                Color.argb(
                    photoKeyAlpha(),
                    10,
                    18,
                    32
                )
            }
        } else {
            if (special) palette.special else palette.key
        }

        return Button(this).apply {
            this.text = text
            textSize = when {
                text == "SPACE" -> 12f
                text.length == 1 -> 20f
                else -> 14f
            }
            setTextColor(Color.WHITE)

            val strokeColor = if (special) {
                palette.accent
            } else {
                Color.argb(
                    180,
                    Color.red(palette.accent),
                    Color.green(palette.accent),
                    Color.blue(palette.accent)
                )
            }

            background = roundedBackground(
                keyBackground,
                8,
                strokeColor
            )

            setPadding(0, 0, 0, 0)
            minHeight = 0
            minimumHeight = 0
            minWidth = 0
            minimumWidth = 0
            stateListAnimator = null
            setOnClickListener { action() }
        }
    }

    private fun photoKeyAlpha(): Int {
        val brightness =
            VexoraThemeManager.photoBrightness(this)

        // 0% = very opaque, 100% = very transparent.
        return (225 - ((brightness * 185) / 100))
            .coerceIn(40, 225)
    }

    private fun photoSpecialKeyAlpha(): Int {
        val brightness =
            VexoraThemeManager.photoBrightness(this)

        return (235 - ((brightness * 145) / 100))
            .coerceIn(90, 235)
    }

    private fun paletteForEmojiKey(): Int {
        return VexoraThemeManager.palette(this).key
    }


    private fun openVexora() {
        val connection = currentInputConnection
        val beforeCursor =
            connection?.getTextBeforeCursor(10000, 0)?.toString() ?: ""
        val afterCursor =
            connection?.getTextAfterCursor(10000, 0)?.toString() ?: ""

        val carrierText = beforeCursor + afterCursor
        carrierLength = beforeCursor.length

        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("carrier_text", carrierText)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    private fun openThemeActivity() {
        val intent = Intent(this, ThemeActivity::class.java).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        startActivity(intent)
    }

    private fun switchKeyboard() {
        try {
            val inputMethodManager =
                getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

            inputMethodManager.showInputMethodPicker()
        } catch (_: Exception) {
            // Android could not open the keyboard picker.
        }
    }

    private fun openKeyboardSettings() {
        val intent = Intent(android.provider.Settings.ACTION_INPUT_METHOD_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    private fun deleteCharacter() {
        val connection: InputConnection = currentInputConnection ?: return
        connection.deleteSurroundingText(1, 0)
    }



    private fun insertPendingMessage() {
        val message = VexoraMessageStore.getMessage(this) ?: return
        if (message.isEmpty()) return

        val connection = currentInputConnection ?: return

        if (carrierLength > 0) {
            connection.deleteSurroundingText(carrierLength, 0)
        }

        val inserted = connection.commitText(message, 1)

        if (inserted) {
            VexoraMessageStore.clearMessage(this)
            carrierLength = 0
        }
    }
    private class PhotoBackgroundDrawable(
        private val bitmap: Bitmap,
        private val zoom: Float,
        private val offsetX: Float,
        private val offsetY: Float
    ) : android.graphics.drawable.Drawable() {

        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private var drawableAlpha = 255

        override fun draw(canvas: Canvas) {
            val width = bounds.width().toFloat()
            val height = bounds.height().toFloat()

            if (width <= 0f || height <= 0f) return

            val bitmapWidth = bitmap.width.toFloat()
            val bitmapHeight = bitmap.height.toFloat()

            val baseScale = maxOf(
                width / bitmapWidth,
                height / bitmapHeight
            )

            val finalScale =
                baseScale * zoom.coerceIn(1.0f, 3.0f)

            val scaledWidth = bitmapWidth * finalScale
            val scaledHeight = bitmapHeight * finalScale

            val centerX = (width - scaledWidth) / 2f
            val centerY = (height - scaledHeight) / 2f

            val matrix = Matrix()
            matrix.setScale(finalScale, finalScale)
            matrix.postTranslate(
                centerX + offsetX,
                centerY + offsetY
            )

            paint.alpha = drawableAlpha
            canvas.drawBitmap(bitmap, matrix, paint)
        }

        override fun setAlpha(alpha: Int) {
            drawableAlpha = alpha
            invalidateSelf()
        }

        override fun getAlpha(): Int = drawableAlpha

        override fun setColorFilter(colorFilter: ColorFilter?) {
            paint.colorFilter = colorFilter
            invalidateSelf()
        }

        @Suppress("DEPRECATION")
        override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
    }

    private fun roundedBackground(
        color: Int,
        radius: Int,
        strokeColor: Int
    ): StateListDrawable {
        val normal = GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
            setStroke(dp(1), strokeColor)
        }

        val pressed = GradientDrawable().apply {
            setColor(strokeColor)
            cornerRadius = dp(radius).toFloat()
            setStroke(dp(2), Color.WHITE)
        }

        val focused = GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
            setStroke(dp(2), strokeColor)
        }

        return StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_pressed), pressed)
            addState(intArrayOf(android.R.attr.state_focused), focused)
            addState(intArrayOf(), normal)
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}

