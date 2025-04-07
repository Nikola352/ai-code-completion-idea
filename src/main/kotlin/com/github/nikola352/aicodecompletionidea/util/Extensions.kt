package com.github.nikola352.aicodecompletionidea.util

/**
 * Splits the document text around the given offset into a prefix and suffix.
 *
 * @param offset The offset around which to split the text.
 * @return A pair consisting of the text before (prefix) and after (suffix) the offset.
 */
fun String.splitAtOffset(offset: Int): Pair<String, String> {
    return substring(0, offset + 1) to substring(offset + 1)
}