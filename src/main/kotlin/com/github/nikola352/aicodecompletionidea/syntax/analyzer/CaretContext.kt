package com.github.nikola352.aicodecompletionidea.syntax.analyzer

/**
 * Extracts information about the context near the caret position.
 */
data class CaretContext(
    val beforeCaret: Char,
    val afterCaret: Char,
    val text: String,
    val offset: Int
) {
    /**
     * Creates a [CaretContext] from the given text document and caret offset.
     *
     * @param text The entire text of the document.
     * @param offset The current position of the caret within the text.
     */
    constructor(text: String, offset: Int) : this(
        beforeCaret = if (offset > 0) text[offset - 1] else '\u0000',
        afterCaret = if (offset < text.length) text[offset] else '\u0000',
        text = text,
        offset = offset
    )
}