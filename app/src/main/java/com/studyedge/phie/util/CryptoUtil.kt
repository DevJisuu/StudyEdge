package com.studyedge.phie.util

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object CryptoUtil {
    private const val ALGORITHM = "AES/CBC/PKCS5Padding"
    private const val KEY = "hgbvrqedlkjvcfgty7ujhdfvcdswerty" // 32 bytes for AES-256
    private const val IV = "hbdfwrthlk987654" // 16 bytes for AES-256-CBC

    fun decrypt(encryptedData: String): String {
        try {
            val cipher = Cipher.getInstance(ALGORITHM)
            val keySpec = SecretKeySpec(KEY.toByteArray(Charsets.UTF_8), "AES")
            val ivSpec = IvParameterSpec(IV.toByteArray(Charsets.UTF_8))
            
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
            val decodedData = Base64.decode(encryptedData, Base64.DEFAULT)
            val decryptedBytes = cipher.doFinal(decodedData)
            
            return String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
} 