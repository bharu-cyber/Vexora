package com.example.vexora

import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vexora.crypto.CryptoManager
import com.example.vexora.crypto.HmacManager
import com.example.vexora.crypto.PayloadManager
import com.example.vexora.crypto.ZeroWidthSteganography
import com.example.vexora.ui.theme.VexoraTheme

class DecodeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            VexoraTheme {

                DecodeScreen(
                    onBack = {
                        if (intent.getBooleanExtra("launched_from_keyboard", false)) {
                            finishAffinity()
                        } else {
                            finish()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun DecodeScreen(
    onBack: () -> Unit
) {

    var encodedMessage by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    var result by remember {
        mutableStateOf("")
    }

    var errorMessage by remember {
        mutableStateOf("")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F153A))
            .padding(20.dp),
        verticalArrangement = Arrangement.Top
    ) {

        Text(
            text = "VEXORA DECODE",
            color = Color(0xFFBC92CD),
            fontSize = 28.sp
        )

        Spacer(
            modifier = Modifier.height(25.dp)
        )

        OutlinedTextField(
            value = encodedMessage,
            onValueChange = {
                encodedMessage = it
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Encoded message")
            },
            placeholder = {
                Text("Paste the Vexora message here")
            },
            singleLine = false
        )

        Spacer(
            modifier = Modifier.height(15.dp)
        )

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Password")
            },
            visualTransformation =
                PasswordVisualTransformation(),
            singleLine = true
        )

        Spacer(
            modifier = Modifier.height(25.dp)
        )

        Button(
            onClick = {

                errorMessage = ""
                result = ""

                try {

                    if (encodedMessage.isBlank()) {
                        throw IllegalArgumentException(
                            "Encoded message cannot be empty"
                        )
                    }

                    if (password.isBlank()) {
                        throw IllegalArgumentException(
                            "Password cannot be empty"
                        )
                    }

                    // 1. Extract hidden payload
                    val payload =
                        ZeroWidthSteganography.reveal(
                            encodedMessage
                        )

                    // 2. Read Vexora payload
                    val vexoraPayload =
                        PayloadManager.parsePayload(
                            payload
                        )

                    val encryptedData =
                        vexoraPayload.encryptedData

                    // 3. Get salt
                    val saltBytes =
                        Base64.decode(
                            encryptedData.salt,
                            Base64.NO_WRAP
                        )

                    // 4. Recreate HMAC data
                    val hmacData =
                        encryptedData.salt +
                                "|" +
                                encryptedData.iv +
                                "|" +
                                encryptedData.ciphertext

                    // 5. Verify HMAC
                    val valid =
                        HmacManager.verify(
                            hmacData,
                            password,
                            saltBytes,
                            vexoraPayload.hmac
                        )

                    if (!valid) {
                        throw SecurityException(
                            "Invalid password or modified message"
                        )
                    }

                    // 6. Decrypt secret message
                    val decryptedMessage =
                        CryptoManager.decrypt(
                            encryptedData,
                            password
                        )

                    result = decryptedMessage

                } catch (e: Exception) {

                    errorMessage =
                        e.message ?: "Decoding failed"
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF5F3F96)
            )
        ) {

            Text(
                text = "🔓 DECODE",
                fontSize = 17.sp
            )
        }

        Spacer(
            modifier = Modifier.height(15.dp)
        )

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF844CAD)
            )
        ) {

            Text("← BACK")
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        if (errorMessage.isNotEmpty()) {

            Text(
                text = errorMessage,
                color = Color.Red,
                fontSize = 14.sp
            )
        }

        if (result.isNotEmpty()) {

            Text(
                text = "Secret message:",
                color = Color(0xFFBC92CD),
                fontSize = 18.sp
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = result,
                color = Color.White,
                fontSize = 16.sp
            )
        }
    }
}