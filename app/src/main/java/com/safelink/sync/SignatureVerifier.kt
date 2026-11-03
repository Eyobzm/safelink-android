package com.safelink.sync

import java.security.KeyFactory
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import java.util.Base64

/**
 * FR-SYNC-03: verify Ed25519 signature before applying rules.
 * Ed25519 via java.security needs API 33+ natively; for minSdk 26 add BouncyCastle or Tink
 * (recommended: com.google.crypto.tink). Placeholder shows the contract.
 */
class SignatureVerifier(private val publicKeyB64: String) {
    fun verify(data: ByteArray, signature: ByteArray): Boolean = try {
        val key = KeyFactory.getInstance("Ed25519")
            .generatePublic(X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyB64)))
        Signature.getInstance("Ed25519").run { initVerify(key); update(data); verify(signature) }
    } catch (e: Exception) { false }  // any error = reject the update
}
