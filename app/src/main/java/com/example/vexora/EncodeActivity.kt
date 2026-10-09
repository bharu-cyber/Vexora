
package com.example.vexora

import android.os.Bundle
import android.util.Base64
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
import androidx.compose.runtime.mutableIntStateOf
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

class EncodeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val carrierText = intent.getStringExtra("carrier_text") ?: ""

        setContent {
            VexoraTheme {
                EncodeScreen(
                    initialCarrierText = carrierText,
                    onBack = {
                        finish()
                    },
                    onEncoded = { hiddenMessage ->
                        VexoraMessageStore.saveMessage(
                            this@EncodeActivity,
                            hiddenMessage
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun EncodeScreen(
    initialCarrierText: String,
    onBack: () -> Unit,
    onEncoded: (String) -> Unit
) {
    var visibleText by remember {
        mutableStateOf(initialCarrierText)
    }

    var secretMessage by remember {
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

    var hiddenCharacterCount by remember {
        mutableIntStateOf(0)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F153A))
            .padding(20.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "VEXORA ENCODE",
            color = Color(0xFFBC92CD),
            fontSize = 28.sp
        )

        Spacer(modifier = Modifier.height(25.dp))

        // CARRIER TEXT
        OutlinedTextField(
            value = visibleText,
            onValueChange = {
                visibleText = it
                hiddenCharacterCount = 0
                result = ""
                errorMessage = ""
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Carrier Text")
            },
            placeholder = {
                Text("Example: Hi")
            },
            singleLine = false
        )

        Spacer(modifier = Modifier.height(8.dp))


        // SECRET MESSAGE
        OutlinedTextField(
            value = secretMessage,
            onValueChange = {
                secretMessage = it
                hiddenCharacterCount = 0
                result = ""
                errorMessage = ""
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Secret Message")
            },
            placeholder = {
                Text("Enter your hidden message")
            },
            singleLine = false
        )

        Spacer(modifier = Modifier.height(15.dp))

        // PASSWORD
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                hiddenCharacterCount = 0
                result = ""
                errorMessage = ""
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Password")
            },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(25.dp))

        // ENCODE BUTTON
        Button(
            onClick = {
                errorMessage = ""
                result = ""
                hiddenCharacterCount = 0

                try {
                    if (visibleText.isBlank()) {
                        throw IllegalArgumentException(
                            "Carrier text cannot be empty"
                        )
                    }

                    if (secretMessage.isBlank()) {
                        throw IllegalArgumentException(
                            "Secret message cannot be empty"
                        )
                    }

                    if (password.isBlank()) {
                        throw IllegalArgumentException(
                            "Password cannot be empty"
                        )
                    }

                    // AES ENCRYPTION
                    val encryptedData = CryptoManager.encrypt(
                        secretMessage,
                        password
                    )

                    // DECODE SALT
                    val saltBytes = Base64.decode(
                        encryptedData.salt,
                        Base64.NO_WRAP
                    )

                    // HMAC INPUT
                    val hmacData =
                        encryptedData.salt +
                                "|" +
                                encryptedData.iv +
                                "|" +
                                encryptedData.ciphertext

                    // HMAC
                    val hmac = HmacManager.generate(
                        hmacData,
                        password,
                        saltBytes
                    )

                    // CREATE PAYLOAD
                    val payload = PayloadManager.createPayload(
                        encryptedData,
                        hmac
                    )

                    // HIDE PAYLOAD
                    val hiddenMessage = ZeroWidthSteganography.hide(
                        visibleText,
                        payload
                    )

                    // COUNT THE ACTUAL ADDED CHARACTERS
                    hiddenCharacterCount =
                        hiddenMessage.length - visibleText.length

                    // SAVE FOR KEYBOARD
                    onEncoded(hiddenMessage)

                    // FINAL RESULT
                    result = "Encoded message created!"

                } catch (e: Exception) {
                    errorMessage = e.message ?: "Encoding failed"
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF844CAD)
            )
        ) {
            Text(
                text = "🔐 ENCODE",
                fontSize = 17.sp
            )
        }

        Spacer(modifier = Modifier.height(15.dp))

        // RETURN BUTTON
        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF5F3F96)
            )
        ) {
            Text(text = "← RETURN")
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ERROR
        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = Color.Red,
                fontSize = 14.sp
            )
        }

        // RESULT
        if (result.isNotEmpty()) {
            Text(
                text = result,
                color = Color(0xFFBC92CD),
                fontSize = 18.sp
            )
        }
    }
}