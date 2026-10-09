package com.example.vexora.crypto

import android.util.Base64
import java.nio.ByteBuffer

data class VexoraPayload(
    val encryptedData: EncryptedData,
    val hmac: String
)

object PayloadManager {

    private const val VERSION: Byte = 1

    fun createPayload(
        encryptedData: EncryptedData,
        hmac: String
    ): String {

        val salt =
            Base64.decode(
                encryptedData.salt,
                Base64.NO_WRAP
            )

        val iv =
            Base64.decode(
                encryptedData.iv,
                Base64.NO_WRAP
            )

        val ciphertext =
            Base64.decode(
                encryptedData.ciphertext,
                Base64.NO_WRAP
            )

        val hmacBytes =
            Base64.decode(
                hmac,
                Base64.NO_WRAP
            )

        val buffer =
            ByteBuffer.allocate(
                1 +
                        1 + salt.size +
                        1 + iv.size +
                        4 + ciphertext.size +
                        1 + hmacBytes.size
            )

        // Version
        buffer.put(VERSION)

        // Salt
        buffer.put(salt.size.toByte())
        buffer.put(salt)

        // IV
        buffer.put(iv.size.toByte())
        buffer.put(iv)

        // Ciphertext
        buffer.putInt(ciphertext.size)
        buffer.put(ciphertext)

        // HMAC
        buffer.put(hmacBytes.size.toByte())
        buffer.put(hmacBytes)

        return Base64.encodeToString(
            buffer.array(),
            Base64.NO_WRAP
        )
    }

    fun parsePayload(
        payload: String
    ): VexoraPayload {

        val bytes =
            Base64.decode(
                payload,
                Base64.NO_WRAP
            )

        val buffer =
            ByteBuffer.wrap(bytes)

        // Version
        val version =
            buffer.get()

        require(version == VERSION) {
            "Unsupported Vexora payload version"
        }

        // Salt
        val saltSize =
            buffer.get().toInt() and 0xFF

        require(saltSize > 0) {
            "Invalid salt"
        }

        val salt =
            ByteArray(saltSize)

        buffer.get(salt)

        // IV
        val ivSize =
            buffer.get().toInt() and 0xFF

        require(ivSize > 0) {
            "Invalid IV"
        }

        val iv =
            ByteArray(ivSize)

        buffer.get(iv)

        // Ciphertext
        val ciphertextSize =
            buffer.int

        require(
            ciphertextSize > 0 &&
                    ciphertextSize <= buffer.remaining()
        ) {
            "Invalid ciphertext"
        }

        val ciphertext =
            ByteArray(ciphertextSize)

        buffer.get(ciphertext)

        // HMAC
        val hmacSize =
            buffer.get().toInt() and 0xFF

        require(
            hmacSize > 0 &&
                    hmacSize <= buffer.remaining()
        ) {
            "Invalid HMAC"
        }

        val hmacBytes =
            ByteArray(hmacSize)

        buffer.get(hmacBytes)

        require(!buffer.hasRemaining()) {
            "Invalid Vexora payload"
        }

        return VexoraPayload(

            encryptedData =
                EncryptedData(
                    salt =
                        Base64.encodeToString(
                            salt,
                            Base64.NO_WRAP
                        ),

                    iv =
                        Base64.encodeToString(
                            iv,
                            Base64.NO_WRAP
                        ),

                    ciphertext =
                        Base64.encodeToString(
                            ciphertext,
                            Base64.NO_WRAP
                        )
                ),

            hmac =
                Base64.encodeToString(
                    hmacBytes,
                    Base64.NO_WRAP
                )
        )
    }
}