package com.safelink.engine

import com.safelink.data.UrlPrivacy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RuleAndPrivacyTest {
    @Test fun storedRuleReplacesDefaultWithSameId() {
        val replacement = Rule(1, RuleType.DELETE_PATH, Regex("custom-wipe"), ReasonCode.IMP_DELETE_PATH)
        val merged = mergeRules(DefaultRules.all, listOf(replacement), emptySet())
        assertEquals(Regex("custom-wipe").pattern, merged.first { it.id == 1L }.regex.pattern)
        assertTrue(merged.any { it.id == 2L })
    }

    @Test fun disabledDefaultIsRemoved() {
        val merged = mergeRules(DefaultRules.all, emptyList(), setOf(1L))
        assertFalse(merged.any { it.id == 1L })
        assertEquals(2, merged.size)
    }

    @Test fun allowListMatchesSubdomainsOnly() {
        assertTrue(AllowListMatcher.matches("login.example.com", listOf("example.com")))
        assertFalse(AllowListMatcher.matches("example.com.evil.test", listOf("example.com")))
        assertEquals("example.com", AllowListMatcher.normalizeDomain("https://www.Example.com/a?x=1"))
        assertNull(AllowListMatcher.normalizeDomain("not a domain"))
    }

    @Test fun stripQueryDropsTokens() {
        assertEquals(
            "https://example.com/path",
            UrlPrivacy.stripQuery("https://example.com/path?token=secret#frag"),
        )
    }
}
