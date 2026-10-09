package com.example.vexora.crypto

object ZeroWidthSteganography {

    // Each invisible character stores 2 bits
    private const val DATA_00 = '\u200B'
    private const val DATA_01 = '\u200C'
    private const val DATA_10 = '\u2062'
    private const val DATA_11 = '\u2063'

    // Message boundaries
    private const val START_MARKER = '\u200D'
    private const val END_MARKER = '\u2060'

    fun hide(
        visibleText: String,
        secretPayload: String
    ): String {

        val payloadBytes =
            secretPayload.toByteArray(Charsets.UTF_8)

        val hiddenText = buildString {

            append(START_MARKER)

            for (byte in payloadBytes) {

                val value =
                    byte.toInt() and 0xFF

                // First 2 bits
                append(
                    when ((value shr 6) and 0x03) {
                        0 -> DATA_00
                        1 -> DATA_01
                        2 -> DATA_10
                        else -> DATA_11
                    }
                )

                // Second 2 bits
                append(
                    when ((value shr 4) and 0x03) {
                        0 -> DATA_00
                        1 -> DATA_01
                        2 -> DATA_10
                        else -> DATA_11
                    }
                )

                // Third 2 bits
                append(
                    when ((value shr 2) and 0x03) {
                        0 -> DATA_00
                        1 -> DATA_01
                        2 -> DATA_10
                        else -> DATA_11
                    }
                )

                // Last 2 bits
                append(
                    when (value and 0x03) {
                        0 -> DATA_00
                        1 -> DATA_01
                        2 -> DATA_10
                        else -> DATA_11
                    }
                )
            }

            append(END_MARKER)
        }

        return visibleText + hiddenText
    }

    fun reveal(
        encodedText: String
    ): String {

        val start =
            encodedText.indexOf(START_MARKER)

        require(start != -1) {
            "No Vexora hidden message found"
        }

        val end =
            encodedText.indexOf(
                END_MARKER,
                start + 1
            )

        require(end != -1) {
            "Vexora hidden message is incomplete"
        }

        val hiddenData =
            encodedText.substring(
                start + 1,
                end
            )

        require(hiddenData.isNotEmpty()) {
            "Invalid Vexora payload"
        }

        require(hiddenData.length % 4 == 0) {
            "Invalid Vexora hidden data"
        }

        val bytes =
            ByteArray(
                hiddenData.length / 4
            )

        for (i in bytes.indices) {

            var value = 0

            for (part in 0 until 4) {

                val bits =
                    when (
                        hiddenData[i * 4 + part]
                    ) {
                        DATA_00 -> 0
                        DATA_01 -> 1
                        DATA_10 -> 2
                        DATA_11 -> 3

                        else -> {
                            throw IllegalArgumentException(
                                "Invalid Vexora hidden data"
                            )
                        }
                    }

                value =
                    (value shl 2) or bits
            }

            bytes[i] = value.toByte()
        }

        return String(
            bytes,
            Charsets.UTF_8
        )
    }
}