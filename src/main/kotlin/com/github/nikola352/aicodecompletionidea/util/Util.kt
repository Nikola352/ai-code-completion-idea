package com.github.nikola352.aicodecompletionidea.util

/**
 * Executes a function with retry logic for specific exceptions
 *
 * @param retryCount Maximum number of retry attempts
 * @param shouldRetry Predicate to determine if an exception should trigger a retry
 * @return Result of the block
 */
fun <T> withRetry(retryCount: Int, shouldRetry: (e: Exception) -> Boolean, fn: () -> T): T {
    var lastException: Exception? = null
    for (i in 0..retryCount) {
        try {
            return fn()
        } catch (e: Exception) {
            if (!shouldRetry(e)) {
                throw e
            }
            lastException = e
        }
    }
    throw lastException ?: IllegalStateException("Retry failed with no exception recorded")
}