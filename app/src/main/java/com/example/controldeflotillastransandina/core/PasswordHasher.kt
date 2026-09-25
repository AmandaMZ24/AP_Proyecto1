package com.example.controldeflotillastransandina.core

import java.security.MessageDigest
import java.security.SecureRandom

object PasswordHasher {

    private val random = SecureRandom()

    fun hash(password: String): String {
        val salt = ByteArray(16).also { random.nextBytes(it) }
        val hexSalt = salt.toHex()
        val digest = MessageDigest.getInstance("SHA-256")
            .digest((hexSalt + password).toByteArray(Charsets.UTF_8))
        return "$hexSalt:${digest.toHex()}"
    }

    fun verify(password: String, stored: String): Boolean {
        val parts = stored.split(":")
        if (parts.size != 2) return false
        val (salt, expected) = parts
        val digest = MessageDigest.getInstance("SHA-256")
            .digest((salt + password).toByteArray(Charsets.UTF_8))
        return digest.toHex() == expected
    }

    private fun ByteArray.toHex() = joinToString("") { "%02x".format(it) }
}