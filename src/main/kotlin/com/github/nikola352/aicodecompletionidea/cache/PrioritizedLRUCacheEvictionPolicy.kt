package com.github.nikola352.aicodecompletionidea.cache

import kotlin.math.max
import kotlin.math.roundToInt

/**
 * A secondary cache eviction policy that emulates LRU with priority-based eviction.
 *
 * This class is designed to work alongside a primary cache that has no eviction policy.
 * It only tracks keys (not values) to determine which items should be evicted from the primary cache.
 *
 * The eviction policy works as follows:
 * 1. Maintains an LRU queue of keys similar to a standard LRU cache
 * 2. When capacity is reached, instead of evicting just the least recently used item:
 *    a. Examines a portion of the least recently used items (the "eviction set")
 *    b. From this set, evicts the item with the lowest priority
 *
 * The size of the eviction set is determined by [priorityEvictionThreshold], which specifies
 * what fraction of the total cache size should be considered for eviction (e.g., 0.1 means 10%).
 *
 * @param K the type of keys maintained by this cache (should match primary cache key type)
 * @param capacity Maximum number of keys to track before evictions occur
 * @param priorityEvictionThreshold Fraction of cache (0-1) to consider for eviction
 * @throws IllegalArgumentException if capacity ≤ 0 or threshold is outside (0,1) range
 */
class PrioritizedLRUCacheEvictionPolicy<K>(
    private val capacity: Int,
    private val priorityEvictionThreshold: Float
) {
    /** Linked list node */
    private inner class Node<K>(
        val key: K?,
        var priority: Int,
        var previous: Node<K>? = null,
        var next: Node<K>? = null,
    )

    private var size = 0
    private val evictionSetSize get() = max(1, (size * priorityEvictionThreshold).roundToInt())

    private val cache = HashMap<K, Node<K>>()

    // Linked list head and tail
    private val head = Node<K>(null, 0)
    private val tail = Node<K>(null, 0)

    init {
        require(capacity > 0) { "Capacity must be positive" }
        require(priorityEvictionThreshold > 0 && priorityEvictionThreshold < 1) { "Eviction threshold must be between 0 and 1" }
        head.next = tail
        tail.previous = head
    }

    /**
     * Updates the key's position in the LRU queue to mark it as recently accessed.
     * Should be called when the key is successfully retrieved from the primary cache.
     */
    fun markAccessed(key: K) {
        cache[key]?.let {
            moveToHead(it)
        }
    }

    /**
     * Registers the access of the key and updates the eviction state parameters.
     *
     * If the eviction condition is met, determines which element to evict, removes it and returns its key.
     *
     * @return The key of the **evicted** node, or null if no eviction happened.
     */
    fun putAndEvictIfNeeded(key: K, priority: Int = 0): K? {
        val node = cache[key]

        if (node == null) {
            // If key doesn't exist, create new node
            val newNode = Node(key, priority)
            cache[key] = newNode
            addNode(newNode)
            size++

            if (size > capacity) {
                return evict()?.key
            }
        } else {
            // Update the priority and move to head
            node.priority = priority
            moveToHead(node)
        }

        return null
    }

    /** Add node to linked list after head */
    private fun addNode(node: Node<K>) {
        node.previous = head
        node.next = head.next

        head.next?.previous = node
        head.next = node
    }

    /** Remove node from linked list */
    private fun removeNode(node: Node<K>) {
        val previous = node.previous
        val next = node.next

        previous?.next = next
        next?.previous = previous
    }

    /** Move node from in linked list from current position to head */
    private fun moveToHead(node: Node<K>) {
        removeNode(node)
        addNode(node)
    }

    /**
     * Evicts the element with the least priority amongst the top [evictionSetSize] least recently used elements.
     */
    private fun evict(): Node<K>? {
        var minNode: Node<K>? = null

        var node = tail
        var count = 0
        while (count < evictionSetSize && node.previous != tail) {
            node = node.previous ?: break
            count++
            if (minNode == null || node.priority < minNode.priority) {
                minNode = node
            }
        }

        return minNode?.also { removeNode(it) }
    }
}