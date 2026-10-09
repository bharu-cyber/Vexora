package com.example.vexora.crypto

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

data class EncryptedData(
    val salt: String,
    val iv: String,
    val ciphertext: String
)

object CryptoManager {

    private const val AES_KEY_SIZE = 256
    private const val PBKDF2_ITERATIONS = 120_000
    private const val SALT_SIZE = 16
    private const val IV_SIZE = 12
    private const val GCM_TAG_SIZE = 128

    private val secureRandom = SecureRandom()

    private fun generateRandomBytes(
        size: Int
    ): ByteArray {

        return ByteArray(size).also {
            secureRandom.nextBytes(it)
        }
    }

    private fun deriveMasterKey(
        password: String,
        salt: ByteArray
    ): ByteArray {

        val spec = PBEKeySpec(
            password.toCharArray(),
            salt,
            PBKDF2_ITERATIONS,
            AES_KEY_SIZE
        )

        val factory =
            SecretKeyFactory.getInstance(
                "PBKDF2WithHmacSHA256"
            )

        return factory
            .generateSecret(spec)
            .encoded
    }

    private fun deriveAesKey(
        masterKey: ByteArray
    ): SecretKeySpec {

        val aesBytes = masterKey.copyOfRange(
            0,
            32
        )

        return SecretKeySpec(
            aesBytes,
            "AES"
        )
    }

    fun encrypt(
        message: String,
        password: String
    ): EncryptedData {

        require(password.isNotEmpty()) {
            "Password cannot be empty"
        }

        val salt = generateRandomBytes(
            SALT_SIZE
        )

        val iv = generateRandomBytes(
            IV_SIZE
        )

        val masterKey = deriveMasterKey(
            password,
            salt
        )

        val aesKey = deriveAesKey(
            masterKey
        )

        val cipher = Cipher.getInstance(
            "AES/GCM/NoPadding"
        )

        val gcmSpec = GCMParameterSpec(
            GCM_TAG_SIZE,
            iv
        )

        cipher.init(
            Cipher.ENCRYPT_MODE,
            aesKey,
            gcmSpec
        )

        val encrypted =
            cipher.doFinal(
                message.toByteArray(
                    Charsets.UTF_8
                )
            )

        return EncryptedData(
            salt = Base64.encodeToString(
                salt,
                Base64.NO_WRAP
            ),
            iv = Base64.encodeToString(
                iv,
                Base64.NO_WRAP
            ),
            ciphertext = Base64.encodeToString(
                encrypted,
                Base64.NO_WRAP
            )
        )
    }

    fun decrypt(
        data: EncryptedData,
        password: String
    ): String {

        require(password.isNotEmpty()) {
            "Password cannot be empty"
        }

        val salt = Base64.decode(
            data.salt,
            Base64.NO_WRAP
        )

        val iv = Base64.decode(
            data.iv,
            Base64.NO_WRAP
        )

        val ciphertext = Base64.decode(
            data.ciphertext,
            Base64.NO_WRAP
        )

        val masterKey = deriveMasterKey(
            password,
            salt
        )

        val aesKey = deriveAesKey(
            masterKey
        )

        val cipher = Cipher.getInstance(
            "AES/GCM/NoPadding"
        )

        val gcmSpec = GCMParameterSpec(
            GCM_TAG_SIZE,
            iv
        )

        cipher.init(
            Cipher.DECRYPT_MODE,
            aesKey,
            gcmSpec
        )

        val decrypted =
            cipher.doFinal(ciphertext)

        return String(
            decrypted,
            Charsets.UTF_8
        )
    }
}