package interpreter

import SeqLangLexer
import SeqLangParser
import interpreter.ast.ASTConstructor
import org.antlr.v4.runtime.*
import java.io.OutputStreamWriter
import java.io.Writer

/**
 * Interpreter of SeqLang.
 *
 * @constructor Optionally takes a writer that the output of the interpreted programs is written to.
 * By default, the output is written to stdout.
 */
class SeqLangInterpreter(writer: Writer = OutputStreamWriter(System.out)) {
    var interpretedLines = 0
    val semanticAnalyzer = SemanticAnalyzer()
    /**
     * Interprets the passed statements and prints the output to the specified writer. This function can be called
     * consecutively to interpret more statements while preserving the program context (variable assignments) from
     * previous calls.
     *
     * @throws SeqLangException, which stores the line number on which the error occurred. When the exception is
     * thrown on a consecutive call, the line number will be relative to all statements that were passed to the interpreter.
     */
    fun interpret(statements: String) {
        val input = CharStreams.fromString(statements)
        val lexer = SeqLangLexer(input)
        lexer.addErrorListener(ErrorListener())
        val tokens = CommonTokenStream(lexer)
        val parser = SeqLangParser(tokens)
        parser.addErrorListener(ErrorListener())
        val tree = parser.program()
        val ast = ASTConstructor().constructAST(tree)
        semanticAnalyzer.analyze(ast)
        interpretedLines += statements.length
    }
}

private class ErrorListener : BaseErrorListener() {
    override fun syntaxError(
        recognizer: Recognizer<*, *>?,
        offendingSymbol: Any?,
        line: Int,
        charPositionInLine: Int,
        msg: String?,
        e: RecognitionException?
    ) {
        throw SeqLangException("invalid syntax" + if (msg != null) ": $msg" else "" , line)
    }
}