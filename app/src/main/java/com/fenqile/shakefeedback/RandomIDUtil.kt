package com.fenqile.shakefeedback

import java.util.*

object RandomIDUtil {
    var chars = charArrayOf(
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'Q', 'W', 'E', 'R', 'T', 'Z', 'U', 'I', 'O', 'P', 'A', 'S', 'D', 'F', 'G', 'H', 'J',
        'K', 'L', 'Y', 'X', 'C', 'V', 'B', 'N', 'M'
    )

    public fun randomString(length: Int): String {

        val stringBuilder = StringBuilder()
        for (i in 0 until length) {
            stringBuilder.append(chars[Random().nextInt(chars.size)])
        }
        return stringBuilder.toString()
    }
}