package com.github.nikola352.aicodecompletionidea.cache

/**
 * A Trie data structure for in-memory cache of code completions.
 *
 * Keys in the tries are code completion requests and values are completions returned by an LLM.
 *
 * Values are natural extensions of keys, so queries with keys consisting of saved key plus a prefix of the value are supported.
 */
class Trie {

    inner class Node(
        var completion: String? = null,
        val children: MutableMap<Char, Node> = mutableMapOf(),
    )

    private val root: Node = Node()

    /** Inserts a key-value pair into a trie with optional arbitrary additional information. */
    fun insert(key: String, value: String) {
        var node = root
        for (char in key) {
            node = node.children.getOrPut(char) { Node() }
        }
        node.completion = value
    }

    /**
     * Returns a value by a given key, with addition that the provided key can be extended by the value if needed.
     * If the key is extended by the value, only remaining suffix of the value is returned.
     */
    fun find(key: String): String? {
        var lastMatch: Node? = null
        var lastMatchDepth = 0

        var node = root
        for (i in key.indices) {
            val char = key[i]
            node = node.children[char] ?: break
            if (node.completion != null) {
                lastMatch = node
                lastMatchDepth = i
            }
        }

        return lastMatch?.completion?.getMatch(key, lastMatchDepth + 1)
    }

    /**
     * Check if [String] is the partial match of [key] starting from [offset].
     *
     * String matches a key if the substring of [key] starting from [offset] is
     * a *proper prefix* of the string.
     *
     * In that case, the remaining suffix is returned as it is a continuation of the key.
     *
     * @param key Key to check for. This is what the user has typed so far.
     * @param offset Last index in key that matches the tree prefix of this string.
     * @return If the String matches the key, returns the suffix of this string after the matching prefix.
     * Returns null if the key does not match or if the suffix is empty (full string match).
     */
    private fun String.getMatch(key: String, offset: Int): String? {
        if (offset == key.length) return this
        for (i in indices) {
            if (this[i] != key[i + offset]) {
                return null
            }
            if (i + offset >= key.length - 1) {
                return substring(i + 1).takeIf { it.isNotEmpty() }
            }
        }
        return null
    }

    /** Removes a node with the given key from the trie */
    fun remove(key: String) {
        remove(root, key, 0)
    }

    private fun remove(node: Node, key: String, index: Int): Boolean {
        if (index == key.length) {
            if (node.completion != null) {
                node.completion = null
                return node.children.isEmpty()
            }
            return false
        }

        val char = key[index]
        val child = node.children[char] ?: return false

        val shouldDeleteChild = remove(child, key, index + 1)

        if (shouldDeleteChild) {
            node.children.remove(char)
            return node.completion == null && node.children.isEmpty()
        }

        return false
    }
}