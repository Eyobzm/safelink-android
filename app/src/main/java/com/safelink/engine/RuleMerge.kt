package com.safelink.engine

/** Stored rules overlay the built-in set. A disabled id removes the built-in rule (FR-IMP-07). */
fun mergeRules(defaults: List<Rule>, stored: List<Rule>, disabledIds: Set<Long>): List<Rule> {
    val map = defaults.associateBy { it.id }.toMutableMap()
    disabledIds.forEach { map.remove(it) }
    stored.forEach { if (it.id !in disabledIds) map[it.id] = it }
    return map.values.sortedBy { it.id }
}
