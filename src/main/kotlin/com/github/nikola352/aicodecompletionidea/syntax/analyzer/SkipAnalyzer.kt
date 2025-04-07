package com.github.nikola352.aicodecompletionidea.syntax.analyzer

/**
 * Determines if the caret position, represented by `offset` in the given `String`, should be skipped for code
 * completion suggestions. This decision is based on the syntactical context surrounding the caret,
 * such as specific characters or structures that typically do not require suggestions.
 *
 * @param offset The current position of the caret within the text.
 * @return `true` if the caret position should be skipped for suggestions, `false` otherwise.
 */
fun String.shouldBeSkippedOnPosition(offset: Int): Boolean = with(CaretContext(this, offset)) {
    afterSemicolon()
            || afterLBrace()
            || afterRBrace()
            || beforeLParenthesis()
            || insideIdentifier()
            || insideStringLiteral()
            || insideCharLiteral()
            || insideNumberLiteral()
            || insideComment()
}

private fun CaretContext.afterSemicolon(): Boolean {
    return beforeCaret == ';'
}

private fun CaretContext.afterLBrace(): Boolean {
    return beforeCaret == '{'
}

private fun CaretContext.afterRBrace(): Boolean {
    return beforeCaret == '}'
}

private fun CaretContext.beforeLParenthesis(): Boolean {
    return afterCaret == '('
}

private fun CaretContext.insideIdentifier(): Boolean {
    return beforeCaret.isLetterOrDigit() && afterCaret.isLetterOrDigit()
}

private fun CaretContext.insideStringLiteral(): Boolean {
    val quoteCountBefore = text.substring(0, offset).count { it == '"' } -
            text.substring(0, offset).windowed(2).count { it == "\\\"" } // skip escaped quotes
    return quoteCountBefore % 2 != 0
}

private fun CaretContext.insideCharLiteral(): Boolean {
    val quoteCountBefore = text.substring(0, offset).count { it == '\'' } -
            text.substring(0, offset).windowed(2).count { it == "\\'" }
    return quoteCountBefore % 2 != 0
}

private fun CaretContext.insideNumberLiteral(): Boolean {
    val before = beforeCaret
    val after = afterCaret
    fun Char.isTypeSuffix() = this in "LlFfDd"
    fun Char.isOperator() = this in "+-*/%&|!=<>"

    val isEnd = (before.isDigit() || before.isTypeSuffix()) &&
            (after == ';' || after.isWhitespace() || after.isOperator() || after == ')' || after == ',')

    return isEnd
}

private fun CaretContext.insideComment(): Boolean {
    // Check for single line comment
    val textBeforeCaret = text.substring(0, offset)
    val lastLineStart = textBeforeCaret.lastIndexOf('\n').let { if (it == -1) 0 else it + 1 }
    val lastCommentStart = textBeforeCaret.lastIndexOf("//", offset)

    if (lastCommentStart != -1 && lastCommentStart >= lastLineStart) {
        return true
    }

    // Check for multi-line comment
    val lastMultiCommentStart = textBeforeCaret.lastIndexOf("/*")
    if (lastMultiCommentStart != -1) {
        val lastMultiCommentEnd = text.indexOf("*/", lastMultiCommentStart)
        return lastMultiCommentEnd == -1 || lastMultiCommentEnd >= offset
    }

    return false
}
