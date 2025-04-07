package com.github.nikola352.aicodecompletionidea.provider

import com.github.nikola352.aicodecompletionidea.llm.LLMCompletionService
import com.github.nikola352.aicodecompletionidea.syntax.analyzer.shouldBeSkippedOnPosition
import com.github.nikola352.aicodecompletionidea.util.splitUsingOffset
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
        val llmService = service<LLMCompletionService>()
        return InlineCompletionSingleSuggestion.build(elements = channelFlow {
            val (prefix, suffix) = request.document.text.splitUsingOffset(request.startOffset)
            launch {
                trySend(
                    InlineCompletionGrayTextElement(
                        llmService.getCompletion(prefix, suffix)
                    )
                )
            }
        })
    }

    override fun isEnabled(event: InlineCompletionEvent): Boolean {
        return event is InlineCompletionEvent.DocumentChange && event.editor.run {
            !document.text.shouldBeSkippedOnPosition(caretModel.offset)
        }
    }
}