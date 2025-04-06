package com.github.nikola352.aicodecompletionidea.llm

import com.github.nikola352.aicodecompletionidea.exception.LLMException

/**
 * A service that prompts an LLM for code completion.
 */
interface LLMCompletionService {
    /**
     * Generates a completion suggestion based on a given prefix and suffix.
     *
     * @param prefix The part of the code before the cursor.
     * @param suffix The part of the code after the cursor.
     * @return The generated completion suggestion.
     */
    @Throws(LLMException::class)
    fun getCompletion(prefix: String, suffix: String = ""): String
}