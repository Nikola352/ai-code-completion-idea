package com.github.nikola352.aicodecompletionidea.llm

import io.github.ollama4j.utils.Options

/**
 * Encapsulates model-specific prompting logic for an Ollama LLM:
 * 1. Model name
 * 2. Options/parameters
 * 3. Prompt format
 * 4. Response format processing
 */
interface OllamaLLM {
    /** The name or identifier of the Ollama model */
    val modelName: String

    /** Ollama model options/settings to be used when generating a response. */
    val options: Options

    /**
     * Builds an LLM prompt for code completion for cursor position between prefix and suffix.
     *
     * @param prefix The part of the code before the cursor.
     * @param suffix The part of the code after the cursor.
     * @return A string with the full prompt ready to use with the LLM.
     */
    fun buildPrompt(prefix: String, suffix: String): String

    /**
     * Processes string response from the LLM into a usable code block.
     */
    fun processResponse(response: String): String
}