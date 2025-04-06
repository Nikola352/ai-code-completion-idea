package com.github.nikola352.aicodecompletionidea.llm

import com.github.nikola352.aicodecompletionidea.OLLAMA_HOST
import com.github.nikola352.aicodecompletionidea.exception.LLMException
import com.github.nikola352.aicodecompletionidea.util.withRetry
import com.intellij.openapi.diagnostic.Logger
import io.github.ollama4j.OllamaAPI
import java.net.http.HttpTimeoutException

/**
 * Implementation of [LLMCompletionService] that uses Ollama API to generate code completions.
 */
class OllamaCompletionService : LLMCompletionService {
    private val logger = Logger.getInstance(OllamaCompletionService::class.java)

    companion object {
        private const val REQUEST_TIMEOUT: Long = 50 // seconds
        private const val RETRY_COUNT = 3
    }

    /** LLM configuration used for suggestion generation */
    // TODO: Add support for changing models
    private val llm: OllamaLLM = CodeLlamaLLM

    /**
     * Generates a completion suggestion based on a given prefix and suffix
     * by querying the Ollama API.
     *
     * @param prefix The part of the code before the cursor.
     * @param suffix The part of the code after the cursor.
     * @return The generated completion suggestion.
     */
    override fun getCompletion(prefix: String, suffix: String): String {
        if (!isAvailable) {
            logger.error("Ollama server not available")
            throw LLMException("Ollama server not available")
        }

        return withRetry(RETRY_COUNT, { e -> e is HttpTimeoutException }) {
            OllamaAPI(OLLAMA_HOST)
                .apply { setRequestTimeoutSeconds(REQUEST_TIMEOUT) }
                .generate(
                    llm.modelName,
                    llm.buildPrompt(prefix, suffix),
                    false,
                    llm.options
                )
        }.response.let { llm.processResponse(it) }
    }

    /** Checks if the Ollama API is reachable for code generation by pinging it. */
    private val isAvailable: Boolean
        get() {
            return try {
                OllamaAPI(OLLAMA_HOST).ping()
            } catch (e: Exception) {
                false
            }
        }
}