package com.github.nikola352.aicodecompletionidea.cache

import com.intellij.openapi.components.Service

@Service(Service.Level.PROJECT)
class CodeCompletionCache {
    private var trie = Trie<Any>()

    /**
     * Retrieves the value associated with the specified key from the cache.
     *
     * @param key The key whose associated value is to be returned.
     * @return The value associated with the specified key or `null` if the key doesn't exist.
     */
    fun get(key: String): String? {
        return trie.find(key)
    }

    fun set(key: String, value: String) {
        trie.insert(key, value)
    }

    fun clear() {
        trie = Trie()
    }
}