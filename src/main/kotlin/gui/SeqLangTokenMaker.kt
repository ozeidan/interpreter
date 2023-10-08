package gui

import SeqLangLexer
import SeqLangLexer.*
import org.antlr.v4.runtime.*
import org.fife.ui.rsyntaxtextarea.AbstractTokenMaker
import org.fife.ui.rsyntaxtextarea.Token
import org.fife.ui.rsyntaxtextarea.TokenMap
import javax.swing.text.Segment

/**
 * Creates the token objects that are expected by the RSyntaxTextArea component.
 * This is achieved by mapping the tokens created by the SeqLangLexer.
 */
class SeqLangTokenMaker : AbstractTokenMaker() {
    // has to match generated SeqLang.tokens file
    private fun mapTokenType(antlrTokenType: Int) : Int {
        val rSyntaxAreaTokenType = when (antlrTokenType) {
            1, 3, 4 -> Token.RESERVED_WORD // var, print, out
            15, 17 -> Token.FUNCTION // map, reduce
            IDENT -> Token.IDENTIFIER
            INTEGER -> Token.LITERAL_NUMBER_DECIMAL_INT
            FLOAT -> Token.LITERAL_NUMBER_FLOAT
            STRING -> Token.LITERAL_STRING_DOUBLE_QUOTE
            UNTERMINATED_STRING -> Token.ERROR_STRING_DOUBLE
            WS -> Token.WHITESPACE
            2, in 5..9, 13, 16 -> Token.OPERATOR // =, operators, comma, ->
            in 10..12, 14 -> Token.SEPARATOR // parens
            else -> {
                throw RuntimeException()
            }
        }

        return rSyntaxAreaTokenType
    }

    override fun getTokenList(text: Segment, startTokenType: Int, startOffset: Int): Token? {
        resetTokenList();

        val lexer = SeqLangLexer(CharStreams.fromString(text.toString()))
        lexer.removeErrorListeners()

        val tokens = lexer.allTokens

        tokens.forEach {
            addToken(
                text,
                text.offset + it.startIndex,
                text.offset + it.stopIndex,
                mapTokenType(it.type),
                startOffset + it.startIndex
            )
        }

        if (tokens.isEmpty()) {
            addNullToken()
        }

        return firstToken
    }

    override fun getWordsToHighlight(): TokenMap {
        return TokenMap()
    }
}