package com.github.nikola352.aicodecompletionidea

import com.github.nikola352.aicodecompletionidea.cache.PrioritizedLRUCacheEvictionPolicy
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class PrioritizedLRUCacheEvictionPolicyTest : BasePlatformTestCase() {

    fun testInitialization() {
        // Test valid initialization
        val policy = PrioritizedLRUCacheEvictionPolicy<String>(10, 0.2f)
        assertNotNull(policy)

        // Test invalid capacity
        try {
            PrioritizedLRUCacheEvictionPolicy<String>(0, 0.2f)
            fail("Should throw IllegalArgumentException for capacity <= 0")
        } catch (_: IllegalArgumentException) {
        }

        // Test invalid threshold
        try {
            PrioritizedLRUCacheEvictionPolicy<String>(10, 0f)
            fail("Should throw IllegalArgumentException for threshold <= 0")
        } catch (_: IllegalArgumentException) {
        }
        try {
            PrioritizedLRUCacheEvictionPolicy<String>(10, 1f)
            fail("Should throw IllegalArgumentException for threshold >= 1")
        } catch (_: IllegalArgumentException) {
        }
    }

    fun testBasicOperations() {
        val policy = PrioritizedLRUCacheEvictionPolicy<String>(4, 0.5f)

        // Test putAndEvictIfNeeded with no eviction
        assertNull(policy.putAndEvictIfNeeded("key1", 1))
        assertNull(policy.putAndEvictIfNeeded("key2", 2))
        assertNull(policy.putAndEvictIfNeeded("key3", 3))
        assertNull(policy.putAndEvictIfNeeded("key4", 4))

        // Test markAccessed
        policy.markAccessed("key2") // Should move key2 to front

        // Test eviction when capacity is exceeded
        val evicted = policy.putAndEvictIfNeeded("key5", 5)
        assertNotNull(evicted)
        // With threshold=0.5, eviction set size is 2 (4*0.5)
        // Among key1 (priority 1) and key3 (priority 3), key1 should be evicted
        assertEquals("key1", evicted)
    }

    fun testEvictionPriorityLogic() {
        val policy = PrioritizedLRUCacheEvictionPolicy<String>(5, 0.4f) // eviction set size = 2

        // Insert keys with different priorities
        assertNull(policy.putAndEvictIfNeeded("key1", 5))
        assertNull(policy.putAndEvictIfNeeded("key2", 3))
        assertNull(policy.putAndEvictIfNeeded("key3", 1))
        assertNull(policy.putAndEvictIfNeeded("key4", 4))
        assertNull(policy.putAndEvictIfNeeded("key5", 2))

        // Access some keys to change their LRU position
        policy.markAccessed("key1")
        policy.markAccessed("key3")

        // Trigger eviction (should look at 2 least recently used)
        val evicted = policy.putAndEvictIfNeeded("key6", 6)
        // Among the 2 least recently used (key2 and key4), the one with the lowest priority should be evicted
        assertEquals("key2", evicted) // key2 has priority 3, which is lower than key4 (4) and key5 (2)
    }

    fun testPriorityUpdate() {
        val policy = PrioritizedLRUCacheEvictionPolicy<String>(2, 0.9f)

        assertNull(policy.putAndEvictIfNeeded("key1", 1))
        assertNull(policy.putAndEvictIfNeeded("key2", 2))

        // Update priority of key1
        assertNull(policy.putAndEvictIfNeeded("key1", 5))

        // Trigger eviction - should evict key2 (priority 2) instead of key1 (priority 5)
        val evicted = policy.putAndEvictIfNeeded("key3", 3)
        assertEquals("key2", evicted)
    }

    fun testRepeatedAccess() {
        val policy = PrioritizedLRUCacheEvictionPolicy<String>(4, 0.3f)

        assertNull(policy.putAndEvictIfNeeded("key1", 1))
        assertNull(policy.putAndEvictIfNeeded("key2", 2))
        assertNull(policy.putAndEvictIfNeeded("key3", 3))
        assertNull(policy.putAndEvictIfNeeded("key4", 4))

        // Repeatedly access key1 - it should stay at the head
        repeat(5) {
            policy.markAccessed("key1")
        }

        // Add new key - should evict from the LRU portion (key2 or key3)
        val evicted = policy.putAndEvictIfNeeded("key5", 5)
        assertTrue(evicted != null && evicted != "key1")
    }
}