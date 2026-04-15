package com.gymrat.app.auth

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PasswordHasher {
    private const val Iterations = 120_000
    private const val KeyLengthBits = 256
    private const val SaltBytes = 16

    data class HashResult(
        val saltB64: String,
        val hashB64: String
    )

    fun hash(password: String): HashResult {
        val salt = ByteArray(SaltBytes)
        SecureRandom().nextBytes(salt)
        val hash = pbkdf2(password, salt)
        return HashResult(
            saltB64 = Base64.encodeToString(salt, Base64.NO_WRAP),
            hashB64 = Base64.encodeToString(hash, Base64.NO_WRAP)
        )
    }

    fun verify(password: String, saltB64: String, hashB64: String): Boolean {
        val salt = Base64.decode(saltB64, Base64.DEFAULT)
        val expected = Base64.decode(hashB64, Base64.DEFAULT)
        val actual = pbkdf2(password, salt)
        return constantTimeEquals(expected, actual)
    }

    private fun pbkdf2(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, Iterations, KeyLengthBits)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        return factory.generateSecret(spec).encoded
    }

    private fun constantTimeEquals(a: ByteArray, b: ByteArray): Boolean {
        if (a.size != b.size) return false
        var diff = 0
        for (i in a.indices) {
            diff = diff or (a[i].toInt() xor b[i].toInt())
        }
        return diff == 0
    }
}

