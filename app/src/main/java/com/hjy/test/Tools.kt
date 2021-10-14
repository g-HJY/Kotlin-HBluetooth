package com.hjy.test

import kotlin.experimental.and

/**
 * Created by _H_JY on 2019/3/7.
 */
object Tools {
    fun bytesToHexString(src: ByteArray?): String? {
        val stringBuilder = StringBuilder("")
        if (src == null || src.isEmpty()) {
            return null
        }
        for (i in src.indices) {
            val v: Byte = src[i] and 0xFF.toByte()
            val hv = Integer.toHexString(v.toInt())
            if (hv.length < 2) {
                stringBuilder.append(0)
            }
            stringBuilder.append("$hv ")
        }
        return stringBuilder.toString().toUpperCase()
    }
}