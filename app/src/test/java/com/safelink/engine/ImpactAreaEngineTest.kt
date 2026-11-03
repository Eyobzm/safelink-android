package com.safelink.engine

import org.junit.Assert.*
import org.junit.Test

class ImpactAreaEngineTest {
    private val engine = ImpactAreaEngine()

    @Test fun deletePathIsHardBlocked() {                       // AC-02
        val v = engine.evaluate("https://example.com/delete-video?id=1", "org.telegram.messenger")
        assertTrue(v.hardBlock); assertTrue(ReasonCode.IMP_DELETE_PATH in v.reasons)
    }

    @Test fun encodedDeletePathIsStillBlocked() {               // FR-IMP-09
        assertTrue(engine.evaluate("https://example.com/%64elete-video", null).hardBlock)
    }

    @Test fun deviceLinkFromUnknownSourceBlocked() {            // AC-03
        assertTrue(engine.evaluate("tg://login?token=abc", null).hardBlock)
    }

    @Test fun deviceLinkFromKnownSourceNotHardBlocked() {
        assertFalse(engine.evaluate("tg://login?token=abc", "org.telegram.messenger").hardBlock)
    }

    @Test fun lowTrustTldIsSoftFlagOnly() {
        val v = engine.evaluate("https://shop.xyz/home", null)
        assertFalse(v.hardBlock); assertTrue(ReasonCode.DOM_LOW_TRUST_TLD in v.reasons)
    }

    @Test fun brandInUnrelatedSubdomainIsLookalike() {
        assertTrue(DomainValidator.isLookalike("telegram.login.evil-site.com"))
    }

    @Test fun normalSiteIsClean() {
        assertTrue(engine.evaluate("https://www.wikipedia.org/wiki/Cat", null).reasons.isEmpty())
    }
}
