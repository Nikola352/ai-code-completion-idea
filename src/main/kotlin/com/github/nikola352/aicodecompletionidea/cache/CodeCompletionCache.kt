package com.github.nikola352.aicodecompletionidea.cache

import com.github.nikola352.aicodecompletionidea.CACHE_CAPACITY
import com.github.nikola352.aicodecompletionidea.PRIORITY_EVICTION_POLICY_THRESHOLD
import com.intellij.openapi.components.Service

/**
 * A project-level service that provides caching for code completion suggestions.
 *
 * This cache combines:
 * - A [Trie] data structure for efficient prefix-based storage and retrieval
 * - A [PrioritizedLRUCacheEvictionPolicy] for managing cache evictions
 *
 * The eviction policy prioritizes keeping longer strings (using length as priority metric)
 * when cache capacity is reached, while maintaining LRU (Least Recently Used) semantics.
 */
@Service(Service.Level.PROJECT)
class CodeCompletionCache {
    private var trie = Trie()
    private var evictionPolicy = PrioritizedLRUCacheEvictionPolicy<String>(
        CACHE_CAPACITY,
        PRIORITY_EVICTION_POLICY_THRESHOLD
    )

    /**
     * Retrieves the value associated with the specified key from the cache.
     *
     * @param key The key whose associated value is to be returned.
     * @return The value associated with the specified key or `null` if the key doesn't exist.
     */
    fun get(key: String): String? {
        return trie.find(key)?.also {
            evictionPolicy.markAccessed(key)
        }
    }

    /** Adds the key-value pair to the cache and evicts a key from cache if the capacity is full. */
    fun set(key: String, value: String) {
        trie.insert(key, value)
        evictionPolicy.putAndEvictIfNeeded(key, key.length)
            ?.let { evictedKey -> trie.remove(evictedKey) }
    }
}