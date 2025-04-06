package com.github.nikola352.aicodecompletionidea.llm

import io.github.ollama4j.utils.Options
import io.github.ollama4j.utils.OptionsBuilder

/** CodeLlama configuration options and prompting logic */
object CodeLlamaLLM : OllamaLLM {
    private const val END_MARKER = "<EOT>"

    override val modelName: String
        get() = "codellama:7b-code"

    override val options: Options by lazy {
        OptionsBuilder()
            .setTemperature(0.4f)
            .setTopK(40)
            .setTopP(0.9f)
            .setRepeatPenalty(1.1f)
            .setStop(END_MARKER)
            .build()
    }

    override fun buildPrompt(prefix: String, suffix: String): String = "<PRE> $prefix <SUF>$suffix <MID>"

    override fun processResponse(response: String): String =
        response.replace(END_MARKER, "").trim(' ', '\t', '\n')
}