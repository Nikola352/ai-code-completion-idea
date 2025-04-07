package com.github.nikola352.aicodecompletionidea.provider

import com.github.nikola352.aicodecompletionidea.MAX_PREFIX_LENGTH
import com.github.nikola352.aicodecompletionidea.MAX_SUFFIX_LENGTH
import com.github.nikola352.aicodecompletionidea.cache.CodeCompletionCache
import com.github.nikola352.aicodecompletionidea.llm.LLMCompletionService
import com.github.nikola352.aicodecompletionidea.syntax.analyzer.shouldBeSkippedOnPosition
import com.github.nikola352.aicodecompletionidea.util.splitAtOffset
import com.intellij.codeInsight.inline.completion.InlineCompletionEvent
import com.intellij.codeInsight.inline.completion.InlineCompletionProvider
import com.intellij.codeInsight.inline.completion.InlineCompletionProviderID
import com.intellij.codeInsight.inline.completion.InlineCompletionRequest
import com.intellij.codeInsight.inline.completion.elements.InlineCompletionGrayTextElement
import com.intellij.codeInsight.inline.completion.suggestion.InlineCompletionSingleSuggestion
import com.intellij.codeInsight.inline.completion.suggestion.InlineCompletionSuggestion
import com.intellij.openapi.components.service
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch

class AICodeCompletionProvider : InlineCompletionProvider {
    /** Unique identifier for this inline completion provider. */
    override val id get() = InlineCompletionProviderID("AICodeCompletionIdeaInlineCompletionProvider")

    override suspend fun getSuggestion(request: InlineCompletionRequest): InlineCompletionSuggestion {
        val cacheService = request.editor.project?.service<CodeCompletionCache>()
        val llmService = service<LLMCompletionService>()
        return InlineCompletionSingleSuggestion.build(elements = channelFlow {
            val (prefix, suffix) = request.document.text.splitAtOffsetForContext(request.startOffset)
            var value = cacheService?.get(prefix)
            if(value == null) {
                value = llmService.getCompletion(prefix, suffix)
                cacheService?.set(prefix, value)
            }
            launch {
                trySend(InlineCompletionGrayTextElement(value))
            }
        })
    }

    override fun isEnabled(event: InlineCompletionEvent): Boolean {
        return event is InlineCompletionEvent.DocumentChange && event.editor.run {
            !document.text.shouldBeSkippedOnPosition(caretModel.offset)
        }
    }

    /**
     * Splits the string into prefix and suffix at a given caret offset,
     * truncating the parts based on maximum length policy.
     */
    private fun String.splitAtOffsetForContext(offset: Int): Pair<String, String> {
        val (prefix, suffix) = splitAtOffset(offset)
        return prefix.takeLast(MAX_PREFIX_LENGTH) to suffix.take(MAX_SUFFIX_LENGTH)
    }
}