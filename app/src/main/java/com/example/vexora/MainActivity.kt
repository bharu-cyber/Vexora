
package com.example.vexora

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vexora.ui.theme.VexoraTheme
import kotlinx.coroutines.delay

private val SpaceDark = Color(0xFF030017)
private val NeonPurple = Color(0xFFBF65FF)
private val NeonBlue = Color(0xFF40A9FF)
private val SoftText = Color(0xFFD2C7EA)
private val GlassSurface = Color(0xE6100B31)
private val CardWhite = Color(0xFFF7F4FF)
private val MutedText = Color(0xFFC2B8DA)

private const val SETTINGS_PREFS = "vexora_settings"
private const val LANGUAGE_KEY = "language"
private const val ENCODE_REQUEST_CODE = 1001

private enum class HomePage {
    HOME,
    HOW_IT_WORKS,
    SETTINGS
}

class MainActivity : ComponentActivity() {

    private var launchedFromKeyboard = false

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Vexora)
        super.onCreate(savedInstanceState)

        val carrierText = intent.getStringExtra("carrier_text") ?: ""
        launchedFromKeyboard = intent.hasExtra("carrier_text")

        val preferences: SharedPreferences =
            getSharedPreferences(SETTINGS_PREFS, MODE_PRIVATE)

        setContent {
            VexoraTheme {
                var showSplash by remember {
                    mutableStateOf(!launchedFromKeyboard)
                }
                var currentPage by remember {
                    mutableStateOf(HomePage.HOME)
                }

                var language by remember {
                    mutableStateOf(
                        preferences.getString(LANGUAGE_KEY, "en") ?: "en"
                    )
                }

                val isTamil = language == "ta"

                LaunchedEffect(Unit) {
                    delay(2500)
                    showSplash = false
                }

                BackHandler(
                    enabled = !showSplash &&
                            currentPage != HomePage.HOME
                ) {
                    currentPage = HomePage.HOME
                }

                if (showSplash) {
                    VexoraSplashScreen(isTamil = isTamil)
                } else {
                    when (currentPage) {
                        HomePage.HOME -> {
                            if (launchedFromKeyboard) {
                                KeyboardVexoraHomeScreen(
                                    isTamil = isTamil,
                                    onEncode = {
                                        val screen = Intent(
                                            this@MainActivity,
                                            EncodeActivity::class.java
                                        ).apply {
                                            putExtra("carrier_text", carrierText)
                                        }

                                        @Suppress("DEPRECATION")
                                        startActivityForResult(
                                            screen,
                                            ENCODE_REQUEST_CODE
                                        )
                                    },
                                    onDecode = {
                                        val screen = Intent(
                                            this@MainActivity,
                                            DecodeActivity::class.java
                                        ).apply {
                                            putExtra("launched_from_keyboard", true)
                                        }
                                        startActivity(screen)
                                    }
                                )
                            } else {
                                VexoraHomeScreen(
                                    isTamil = isTamil,
                                    onEncode = {
                                        val screen = Intent(
                                            this@MainActivity,
                                            EncodeActivity::class.java
                                        ).apply {
                                            putExtra("carrier_text", carrierText)
                                        }

                                        if (launchedFromKeyboard) {
                                            @Suppress("DEPRECATION")
                                            startActivityForResult(
                                                screen,
                                                ENCODE_REQUEST_CODE
                                            )
                                        } else {
                                            startActivity(screen)
                                        }
                                    },
                                    onDecode = {
                                        val screen = Intent(
                                            this@MainActivity,
                                            DecodeActivity::class.java
                                        )

                                        if (launchedFromKeyboard) {
                                            screen.putExtra("launched_from_keyboard", true)
                                        }

                                        startActivity(screen)
                                    },
                                    onThemes = {
                                        startActivity(
                                            Intent(
                                                this@MainActivity,
                                                ThemeActivity::class.java
                                            )
                                        )
                                    },
                                    onSettings = {
                                        currentPage = HomePage.SETTINGS
                                    },
                                    onHowItWorks = {
                                        currentPage = HomePage.HOW_IT_WORKS
                                    }
                                )
                            }
                        }

                        HomePage.HOW_IT_WORKS -> {
                            HowItWorksScreen(
                                isTamil = isTamil,
                                onBack = {
                                    currentPage = HomePage.HOME
                                }
                            )
                        }

                        HomePage.SETTINGS -> {
                            SettingsScreen(
                                isTamil = isTamil,
                                onLanguageChange = { newLanguage ->
                                    preferences.edit()
                                        .putString(LANGUAGE_KEY, newLanguage)
                                        .apply()
                                    language = newLanguage
                                },
                                onBack = {
                                    currentPage = HomePage.HOME
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    @Deprecated("Use Activity Result APIs")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (
            requestCode == ENCODE_REQUEST_CODE &&
            launchedFromKeyboard
        ) {
            finish()
        }
    }
}

@Composable
private fun GalaxyBackground() {
    Image(
        painter = painterResource(R.drawable.bg_galaxy),
        contentDescription = null,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
    )
}

/* -------------------- SPLASH SCREEN -------------------- */

@Composable
fun VexoraSplashScreen(isTamil: Boolean = false) {
    val transition = rememberInfiniteTransition(label = "splashProgress")

    val progress by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1400,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "progress"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SpaceDark)
    ) {
        GalaxyBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.weight(1f))

            Image(
                painter = painterResource(R.drawable.vexora_logo_new),
                contentDescription = "Vexora logo",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(Modifier.height(22.dp))

            Text(
                text = if (isTamil) "உங்கள் வார்த்தைகளை மறைக்கவும்"
                else "HIDE YOUR WORDS",
                color = Color.White,
                fontSize = 14.sp,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            Text(
                text = if (isTamil) "வெளிப்படையான உரைக்குள்"
                else "IN PLAIN SIGHT",
                color = Color(0xFFD09BFF),
                fontSize = 14.sp,
                letterSpacing = 3.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.weight(1f))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth(0.55f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(10.dp)),
                color = NeonPurple,
                trackColor = Color(0xFF35245E)
            )

            Spacer(Modifier.height(14.dp))

            Text(
                text = if (isTamil)
                    "Vexora விசைப்பலகை தயாராகிறது..."
                else
                    "Initializing secure keyboard...",
                color = Color(0xFFC1B4E1),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(12.dp))
        }
    }
}

/* -------------------- HOME SCREEN -------------------- */

@Composable
private fun KeyboardVexoraHomeScreen(
    isTamil: Boolean,
    onEncode: () -> Unit,
    onDecode: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SpaceDark)
    ) {
        GalaxyBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text(
                text = "VEXORA",
                color = Color.White,
                fontSize = 27.sp,
                letterSpacing = 4.sp,
                fontWeight = FontWeight.ExtraLight
            )

            Text(
                text = if (isTamil) "உங்கள் செய்தி கருவிகள்"
                else "YOUR MESSAGE TOOLS",
                color = Color(0xFFB5A4DA),
                fontSize = 10.sp,
                letterSpacing = 2.sp
            )

            VexoraActionCard(
                title = if (isTamil) "மறையாக்கம்" else "Encode",
                description = if (isTamil)
                    "உங்கள் ரகசிய செய்தியை மறைக்கவும்."
                else "Hide your secret message inside normal text.",
                icon = "⌑",
                colors = listOf(
                    Color(0xE82A174A),
                    Color(0xF20D0928)
                ),
                glow = NeonPurple,
                onClick = onEncode
            )

            VexoraActionCard(
                title = if (isTamil) "மறைவுநீக்கம்" else "Decode",
                description = if (isTamil)
                    "மறைக்கப்பட்ட செய்தியை வெளிப்படுத்தவும்."
                else "Reveal a hidden message from text.",
                icon = "◎",
                colors = listOf(
                    Color(0xE817315B),
                    Color(0xF207142B)
                ),
                glow = NeonBlue,
                onClick = onDecode
            )
        }
    }
}
@Composable
fun VexoraHomeScreen(
    isTamil: Boolean,
    onEncode: () -> Unit,
    onDecode: () -> Unit,
    onThemes: () -> Unit,
    onSettings: () -> Unit,
    onHowItWorks: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SpaceDark)
    ) {
        GalaxyBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .verticalScroll(rememberScrollState())
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    top = 24.dp,
                    bottom = 20.dp
                ),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.vexora_logo_new),
                    contentDescription = "Vexora logo",
                    modifier = Modifier.size(62.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(Modifier.size(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "VEXORA",
                        style = TextStyle(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.White,
                                    Color(0xFFDDFBFF),
                                    Color(0xFF42DFFF)
                                )
                            )
                        ),
                        fontSize = 27.sp,
                        letterSpacing = 4.sp,
                        fontWeight = FontWeight.ExtraLight
                    )

                    Spacer(Modifier.height(3.dp))

                    Text(
                        text = if (isTamil)
                            "ரகசிய செய்திகள், அன்றாடம்"
                        else
                            "Secret Messages, Everyday",
                        color = MutedText,
                        fontSize = 11.sp,
                        letterSpacing = 0.3.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = CircleShape,
                            ambientColor = NeonPurple.copy(alpha = 0.16f),
                            spotColor = NeonPurple.copy(alpha = 0.24f)
                        )
                        .clip(CircleShape)
                        .background(Color(0xCC17112F))
                        .border(
                            width = 1.dp,
                            color = NeonPurple.copy(alpha = 0.48f),
                            shape = CircleShape
                        )
                        .clickable { onSettings() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⚙",
                        color = Color(0xFFE9D4FF),
                        fontSize = 23.sp
                    )
                }
            }

            Text(
                text = if (isTamil)
                    "உங்கள் செய்திகளைப் பாதுகாக்கவும்"
                else
                    "YOUR MESSAGE TOOLS",
                color = Color(0xFFB5A4DA),
                fontSize = 10.sp,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 3.dp, top = 1.dp)
            )

            VexoraActionCard(
                title = if (isTamil) "மறையாக்கம்" else "Encode",
                description = if (isTamil)
                    "உங்கள் ரகசிய செய்தியை\nசாதாரண உரைக்குள் மறைக்கவும்."
                else
                    "Hide your secret message\ninside a normal text.",
                icon = "⌑",
                colors = listOf(
                    Color(0xE82A174A),
                    Color(0xF20D0928)
                ),
                glow = NeonPurple,
                onClick = onEncode
            )

            VexoraActionCard(
                title = if (isTamil) "மறைவுநீக்கம்" else "Decode",
                description = if (isTamil)
                    "உரையிலிருந்து மறைக்கப்பட்ட\nசெய்தியை வெளிப்படுத்தவும்."
                else
                    "Reveal hidden messages\nfrom a text.",
                icon = "◎",
                colors = listOf(
                    Color(0xE817315B),
                    Color(0xF207142B)
                ),
                glow = NeonBlue,
                onClick = onDecode
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                VexoraSmallCard(
                    modifier = Modifier.weight(1f),
                    icon = "☷",
                    title = if (isTamil) "எப்படிச்\nசெயல்படுகிறது"
                    else "How it\nworks",
                    color = NeonPurple,
                    onClick = onHowItWorks
                )

                VexoraSmallCard(
                    modifier = Modifier.weight(1f),
                    icon = "◈",
                    title = if (isTamil) "தீம்கள்" else "Themes",
                    color = NeonBlue,
                    onClick = onThemes
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(20.dp),
                        ambientColor = NeonPurple.copy(alpha = 0.08f),
                        spotColor = NeonBlue.copy(alpha = 0.08f)
                    )
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xE8100B29))
                    .border(
                        width = 1.dp,
                        color = Color(0x554B3C78),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(NeonPurple.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "◇",
                            color = Color(0xFFD4A4FF),
                            fontSize = 27.sp
                        )
                    }

                    Spacer(Modifier.size(13.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isTamil)
                                "உங்கள் செய்திகளை கவனமாகக் கையாளுங்கள்"
                            else
                                "Your messages matter",
                            color = CardWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(Modifier.height(5.dp))

                        Text(
                            text = if (isTamil)
                                "தனியுரிமை, செயலியின் செய்தி செயலாக்க முறையைப் பொறுத்தது."
                            else
                                "Privacy depends on how the app processes messages.",
                            color = MutedText,
                            fontSize = 11.sp,
                            lineHeight = 17.sp
                        )
                    }
                }
            }

            Text(
                text = if (isTamil)
                    "VEXORA  •  பாதுகாப்பு  •  தனியுரிமை"
                else
                    "VEXORA  •  SECURITY  •  PRIVACY",
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF9585B9),
                fontSize = 9.sp,
                letterSpacing = 1.2.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

/* -------------------- ENCODE / DECODE CARDS -------------------- */

@Composable
private fun VexoraActionCard(
    title: String,
    description: String,
    icon: String,
    colors: List<Color>,
    glow: Color,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(24.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 10.dp,
                shape = shape,
                ambientColor = glow.copy(alpha = 0.10f),
                spotColor = glow.copy(alpha = 0.18f)
            )
            .clip(shape)
            .background(Brush.linearGradient(colors))
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        glow.copy(alpha = 0.82f),
                        glow.copy(alpha = 0.30f),
                        glow.copy(alpha = 0.58f)
                    )
                ),
                shape = shape
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(glow.copy(alpha = 0.13f))
                .border(
                    width = 1.dp,
                    color = glow.copy(alpha = 0.38f),
                    shape = RoundedCornerShape(18.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = icon,
                fontSize = 34.sp,
                color = Color.White,
                fontWeight = FontWeight.Light
            )
        }

        Spacer(Modifier.size(15.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.2.sp
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = description,
                color = Color(0xFFD3CDE7),
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        }

        Spacer(Modifier.size(6.dp))

        Text(
            text = "›",
            color = glow,
            fontSize = 34.sp,
            fontWeight = FontWeight.Light
        )
    }
}

/* -------------------- SHORTCUT CARDS -------------------- */

@Composable
private fun VexoraSmallCard(
    modifier: Modifier,
    icon: String,
    title: String,
    color: Color,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)

    Column(
        modifier = modifier
            .height(104.dp)
            .shadow(
                elevation = 5.dp,
                shape = shape,
                ambientColor = color.copy(alpha = 0.06f),
                spotColor = color.copy(alpha = 0.10f)
            )
            .clip(shape)
            .background(Color(0xE810102C))
            .border(
                width = 1.dp,
                color = color.copy(alpha = 0.30f),
                shape = shape
            )
            .clickable { onClick() }
            .padding(horizontal = 5.dp, vertical = 9.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = icon,
                color = color,
                fontSize = 23.sp
            )
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = title,
            color = Color(0xFFE8E2F5),
            fontSize = 10.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}

/* -------------------- SHARED PAGE LAYOUT -------------------- */

@Composable
private fun VexoraPageLayout(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SpaceDark)
    ) {
        GalaxyBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .verticalScroll(rememberScrollState())
                .padding(
                    start = 20.dp,
                    top = 36.dp,
                    end = 20.dp,
                    bottom = 20.dp
                )
        ) {
            Text(
                text = "‹  Back",
                color = NeonBlue,
                fontSize = 16.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onBack() }
                    .padding(vertical = 10.dp, horizontal = 4.dp)
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = title,
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = subtitle,
                color = Color(0xFFC2B3E5),
                fontSize = 14.sp,
                lineHeight = 21.sp
            )

            Spacer(Modifier.height(22.dp))

            content()
        }
    }
}

@Composable
private fun InfoSection(
    number: String,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = GlassSurface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(NeonPurple.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = number,
                        color = NeonPurple,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.size(12.dp))

                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = description,
                color = SoftText,
                fontSize = 14.sp,
                lineHeight = 22.sp
            )
        }
    }
}

/* -------------------- HOW IT WORKS -------------------- */

@Composable
private fun HowItWorksScreen(
    isTamil: Boolean,
    onBack: () -> Unit
) {
    VexoraPageLayout(
        title = if (isTamil) "Vexora எப்படிச் செயல்படுகிறது"
        else "How Vexora Works",
        subtitle = if (isTamil)
            "செய்தியை மறையாக்கம் செய்து, பகிர்ந்து, மீண்டும் வாசிப்பது எப்படி என்பதை அறியுங்கள்."
        else
            "Learn how to encode, share, and decode a hidden message.",
        onBack = onBack
    ) {
        val steps = if (isTamil) {
            listOf(
                Triple(
                    "01",
                    "உங்கள் ரகசியத்தை எழுதுங்கள்",
                    "மறையாக்கம் திரையைத் திறந்து, மறைக்க விரும்பும் செய்தியை உள்ளிடுங்கள்."
                ),
                Triple(
                    "02",
                    "மறையாக்கம் செய்யுங்கள்",
                    "Encode பொத்தானைத் தட்டுங்கள். உங்கள் செயலியில் உள்ள முறைப்படி உரை செயலாக்கப்படும்."
                ),
                Triple(
                    "03",
                    "முடிவை நகலெடுங்கள்",
                    "உருவாக்கப்பட்ட உரையை நகலெடுத்து, மாற்றாமல் வைத்திருங்கள்."
                ),
                Triple(
                    "04",
                    "கவனமாகப் பகிருங்கள்",
                    "தேவையான நபருக்கு உரையை அனுப்புங்கள். மறையாக்கம் மட்டும் முழுப் பாதுகாப்பை உறுதி செய்யாது."
                ),
                Triple(
                    "05",
                    "Decode திறக்கவும்",
                    "Vexora-வைத் திறந்து Decode திரைக்குச் செல்லுங்கள்; பின்னர் உரையை உள்ளிடுங்கள்."
                ),
                Triple(
                    "06",
                    "செய்தியைப் பாருங்கள்",
                    "Decode பொத்தானைத் தட்டுங்கள். சரியான உரையாக இருந்தால் அசல் செய்தி காட்டப்படும்."
                ),
                Triple(
                    "07",
                    "தீம்களைப் பாருங்கள்",
                    "முகப்புக்குத் திரும்பி Themes-ஐத் திறந்து தோற்றத்தை மாற்றுங்கள்."
                ),
                Triple(
                    "08",
                    "தகவலைப் பாதுகாக்கவும்",
                    "செயலியின் பாதுகாப்பு முறையைச் சரிபார்க்கும் வரை கடவுச்சொற்கள் அல்லது வங்கி விவரங்களை உள்ளிட வேண்டாம்."
                )
            )
        } else {
            listOf(
                Triple(
                    "01",
                    "Write your secret",
                    "Open Encode and enter the message you want to keep private."
                ),
                Triple(
                    "02",
                    "Encode the message",
                    "Tap Encode. Vexora processes your message using the method implemented in your app."
                ),
                Triple(
                    "03",
                    "Copy the result",
                    "Copy the generated text and keep it intact so it can be decoded correctly."
                ),
                Triple(
                    "04",
                    "Share carefully",
                    "Send the encoded text to the intended recipient. Encoding alone does not guarantee security."
                ),
                Triple(
                    "05",
                    "Open Decode",
                    "Open Vexora, choose Decode, and enter the encoded text."
                ),
                Triple(
                    "06",
                    "Reveal the message",
                    "Tap Decode. If the text is valid for your app's method, the original message should appear."
                ),
                Triple(
                    "07",
                    "Explore themes",
                    "Return to the home screen and tap Themes to explore appearance options."
                ),
                Triple(
                    "08",
                    "Protect your information",
                    "Avoid passwords and banking details until your app's security method has been reviewed."
                )
            )
        }

        steps.forEach { (number, title, description) ->
            InfoSection(number, title, description)
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = if (isTamil)
                "VEXORA • உங்கள் கட்டுப்பாட்டில்"
            else
                "VEXORA • YOUR MESSAGES, YOUR CONTROL",
            modifier = Modifier.fillMaxWidth(),
            color = NeonPurple,
            fontSize = 11.sp,
            letterSpacing = 1.sp,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(20.dp))
    }
}

/* -------------------- SETTINGS -------------------- */

@Composable
private fun SettingsScreen(
    isTamil: Boolean,
    onLanguageChange: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var showLanguageDialog by remember {
        mutableStateOf(false)
    }

    val versionName = remember {
        try {
            @Suppress("DEPRECATION")
            context.packageManager
                .getPackageInfo(context.packageName, 0)
                .versionName ?: "Unknown"
        } catch (_: PackageManager.NameNotFoundException) {
            "Unknown"
        }
    }

    VexoraPageLayout(
        title = if (isTamil) "அமைப்புகள்" else "Settings",
        subtitle = if (isTamil)
            "Vexora பயன்பாட்டின் தகவல்களும் விருப்பங்களும்."
        else
            "Information and options for your Vexora app.",
        onBack = onBack
    ) {
        InfoSection(
            number = "APP",
            title = if (isTamil) "Vexora பற்றி" else "About Vexora",
            description = if (isTamil)
                "Vexora என்பது ரகசிய செய்திகளுக்கான பயன்பாடு.\nபதிப்பு: $versionName"
            else
                "Vexora is your secret-message app.\nVersion: $versionName"
        )

        SettingsOption(
            icon = "文",
            title = if (isTamil) "மொழி" else "Language",
            description = if (isTamil)
                "தற்போதைய மொழி: தமிழ்"
            else
                "Current language: English",
            onClick = {
                showLanguageDialog = true
            }
        )

        SettingsOption(
            icon = "↗",
            title = if (isTamil)
                "Vexora Keyboard-ஐப் பகிரவும்"
            else
                "Share Vexora Keyboard",
            description = if (isTamil)
                "உங்கள் நண்பர்களுடன் Vexora-வைப் பகிருங்கள்."
            else
                "Share Vexora with your friends.",
            onClick = {
                val message = if (isTamil) {
                    "Vexora-வை முயற்சி செய்து பாருங்கள்!"
                } else {
                    "Check out Vexora, a secret-message app!"
                }

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, message)
                }

                try {
                    context.startActivity(
                        Intent.createChooser(
                            shareIntent,
                            if (isTamil) "Vexora-வைப் பகிரவும்"
                            else "Share Vexora"
                        )
                    )
                } catch (_: Exception) {
                    Toast.makeText(
                        context,
                        if (isTamil)
                            "பகிர முடியவில்லை."
                        else
                            "Unable to share right now.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF45136E),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = if (isTamil)
                    "முகப்புக்குத் திரும்பு"
                else
                    "Back to Home",
                fontSize = 16.sp
            )
        }

        Spacer(Modifier.height(20.dp))
    }

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = {
                showLanguageDialog = false
            },
            title = {
                Text(
                    if (isTamil)
                        "மொழியைத் தேர்ந்தெடுக்கவும்"
                    else
                        "Choose language"
                )
            },
            text = {
                Column {
                    TextButton(
                        onClick = {
                            onLanguageChange("en")
                            showLanguageDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "English",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )
                    }

                    TextButton(
                        onClick = {
                            onLanguageChange("ta")
                            showLanguageDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "தமிழ்",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLanguageDialog = false
                    }
                ) {
                    Text(if (isTamil) "மூடு" else "Close")
                }
            }
        )
    }
}

@Composable
private fun SettingsOption(
    icon: String,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = GlassSurface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(NeonPurple.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = icon,
                    color = NeonPurple,
                    fontSize = 22.sp
                )
            }

            Spacer(Modifier.size(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = description,
                    color = SoftText,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }

            Text(
                text = "›",
                color = NeonBlue,
                fontSize = 28.sp
            )
        }
    }
}