package com.example.kdyaimap.util

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.nio.ByteBuffer
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CryptoHelper @Inject constructor() {

    private val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
        load(null)
    }

    private fun getSecretKey(alias: String): SecretKey {
        val existingKey = keyStore.getEntry(alias, null) as? KeyStore.SecretKeyEntry
        return existingKey?.secretKey ?: generateSecretKey(alias)
    }

    private fun generateSecretKey(alias: String): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val parameterSpec = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .build()
        keyGenerator.init(parameterSpec)
        return keyGenerator.generateKey()
    }

    fun encrypt(data: String, keyAlias: String = KEY_ALIAS_V1): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(keyAlias))
        val iv = cipher.iv
        val encryptedData = cipher.doFinal(data.toByteArray())
        val buffer = ByteBuffer.allocate(4 + iv.size + encryptedData.size)
        buffer.putInt(iv.size)
        buffer.put(iv)
        buffer.put(encryptedData)
        return buffer.array()
    }

    fun decrypt(encryptedData: ByteArray, keyAlias: String = KEY_ALIAS_V1): String {
        val buffer = ByteBuffer.wrap(encryptedData)
        val ivSize = buffer.int
        val iv = ByteArray(ivSize)
        buffer.get(iv)
        val data = ByteArray(buffer.remaining())
        buffer.get(data)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(keyAlias), spec)
        val decryptedData = cipher.doFinal(data)
        return String(decryptedData)
    }

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val KEY_ALIAS_V1 = "campus_nav_key_v1"
    }
}