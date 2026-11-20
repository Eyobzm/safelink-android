package com.safelink.sync

import android.os.Build
import android.util.Base64
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import org.bouncycastle.crypto.signers.Ed25519Signer
import java.security.KeyFactory
import java.security.Signature
import java.security.spec.X509EncodedKeySpec

/**
 * FR-SYNC-03. API 33+ can verify an X.509 Ed25519 key with the platform.
 * Older devices use Bouncy Castle and a raw 32-byte public key (or the last 32 bytes of an X.509 key).
 */
class SignatureVerifier(private val publicKeyB64: String) {
    fun verify(data: ByteArray, signature: ByteArray): Boolean {
        if (publicKeyB64.isBlank() || publicKeyB64 == "REPLACE_ME") return false
        if (signature.size != 64) return false
        if (Build.VERSION.SDK_INT >= 33) {
            try {
                if (verifyPlatform(data, signature)) return true
            } catch (_: Exception) {
                // Raw keys are not X.509. Fall through to Bouncy Castle.
            }
        }
        return try {
            verifyBouncyCastle(data, signature)
        } catch (_: Exception) {
            false
        }
    }

    private fun verifyPlatform(data: ByteArray, signature: ByteArray): Boolean {
        val key = KeyFactory.getInstance("Ed25519")
            .generatePublic(X509EncodedKeySpec(Base64.decode(publicKeyB64, Base64.DEFAULT)))
        return Signature.getInstance("Ed25519").run {
            initVerify(key)
            update(data)
            verify(signature)
        }
    }

    private fun verifyBouncyCastle(data: ByteArray, signature: ByteArray): Boolean {
        val verifier = Ed25519Signer()
        verifier.init(false, Ed25519PublicKeyParameters(rawPublicKey(), 0))
        verifier.update(data, 0, data.size)
        return verifier.verifySignature(signature)
    }

    private fun rawPublicKey(): ByteArray {
        val decoded = Base64.decode(publicKeyB64, Base64.DEFAULT)
        return if (decoded.size == 32) decoded else decoded.copyOfRange(decoded.size - 32, decoded.size)
    }
}
