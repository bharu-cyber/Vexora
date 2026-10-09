package com.example.vexora.crypto

import android.util.Base64
import java.nio.charset.StandardCharsets
import javax.crypto.Mac
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object HmacManager {

    private const val HMAC_ALGORITHM = "HmacSHA256"
    private const val HMAC_KEY_SIZE = 256
    private const val HMAC_ITERATIONS = 120_000

    /*
     * A different purpose string is used for HMAC
     * so the HMAC key is separated from the AES key.
     */
    private const val HMAC_PURPOSE = "VEXORA-HMAC-V1"

    private fun deriveHmacKey(
        password: String,
        salt: ByteArray
    ): ByteArray {

        val purposeSalt =
            salt + HMAC_PURPOSE.toByteArray(
                StandardCharsets.UTF_8
            )

        val spec = PBEKeySpec(
            password.toCharArray(),
            purposeSalt,
            HMAC_ITERATIONS,
            HMAC_KEY_SIZE
        )

        val factory =
            SecretKeyFactory.getInstance(
                "PBKDF2WithHmacSHA256"
            )

        return factory
            .generateSecret(spec)
            .encoded
    }

    fun generate(
        data: String,
        password: String,
        salt: ByteArray
    ): String {

        val hmacKey = deriveHmacKey(
            password,
            salt
        )

        val mac = Mac.getInstance(
            HMAC_ALGORITHM
        )

        mac.init(
            SecretKeySpec(
                hmacKey,
                HMAC_ALGORITHM
            )
        )

        val result = mac.doFinal(
            data.toByteArray(
                StandardCharsets.UTF_8
            )
        )

        return Base64.encodeToString(
            result,
            Base64.NO_WRAP
        )
    }

    fun verify(
        data: String,
        password: String,
        salt: ByteArray,
        expectedHmac: String
    ): Boolean {

        val actualHmac = generate(
            data,
            password,
            salt
        )

        return constantTimeEquals(
            actualHmac,
            expectedHmac
        )
    }

    private fun constantTimeEquals(
        first: String,
        second: String
    ): Boolean {

        val firstBytes =
            first.toByteArray(
                StandardCharsets.UTF_8
            )

        val secondBytes =
            second.toByteArray(
                StandardCharsets.UTF_8
            )

        if (firstBytes.size != secondBytes.size) {
            return false
        }

        var result = 0

        for (i in firstBytes.indices) {

            result = result or (
                    firstBytes[i].toInt()
                            xor secondBytes[i].toInt()
                    )
        }

        return result == 0
    }
}