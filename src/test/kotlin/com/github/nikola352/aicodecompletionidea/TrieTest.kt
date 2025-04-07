package com.github.nikola352.aicodecompletionidea

import com.github.nikola352.aicodecompletionidea.cache.Trie
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class TrieTest : BasePlatformTestCase() {

    fun testInsertAndFindExactMatch() {
        val trie = Trie()
        trie.insert("abc", "123")

        assertEquals("123", trie.find("abc"))
    }

    fun testFindWithPartialValueExtension() {
        val trie = Trie()
        trie.insert("abc", "123")

        assertEquals("23", trie.find("abc1"))
        assertEquals("3", trie.find("abc12"))
        assertEquals(null, trie.find("abc123"))
    }

    fun testFindNonExistentKey() {
        val trie = Trie()
        trie.insert("abc", "123")

        assertEquals(null, trie.find("abcd"))
        assertEquals(null, trie.find("ab"))
        assertEquals(null, trie.find("xyz"))
    }

    fun testMultipleInsertions() {
        val trie = Trie()
        trie.insert("abc", "123")
        trie.insert("abcd", "1234")

        assertEquals("123", trie.find("abc"))
        assertEquals("1234", trie.find("abcd"))
        assertEquals("234", trie.find("abcd1"))
        assertEquals("3", trie.find("abc12"))
    }

    fun testRemoveKey() {
        val trie = Trie()
        trie.insert("abc", "123")
        trie.insert("abcd", "1234")

        assertEquals("123", trie.find("abc"))
        assertEquals("1234", trie.find("abcd"))

        trie.remove("abc")
        assertEquals(null, trie.find("abc"))
        assertEquals("1234", trie.find("abcd"))

        // Verify partial matches still work
        assertEquals("4", trie.find("abcd123"))
    }

    fun testEmptyTrie() {
        val trie = Trie()

        assertEquals(null, trie.find("abc"))
        assertEquals(null, trie.find(""))
    }

    fun testPartialMatchEdgeCases() {
        val trie = Trie()
        trie.insert("abc", "123")

        // Key is longer than stored value
        assertEquals(null, trie.find("abc1234"))

        // Partial match but diverges from stored value
        assertEquals(null, trie.find("abc2"))
    }
}