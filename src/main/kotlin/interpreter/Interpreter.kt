package interpreter

import SeqLangLexer
import SeqLangParser
import interpreter.ast.ASTConstructor
import org.antlr.v4.runtime.*
import java.io.PrintWriter
import java.io.Writer
import java.util.concurrent.ForkJoinPool

/**
 * Interpreter of SeqLang.
 *
 * @constructor Optionally takes a writer that the output of the interpreted programs is written to.
 * By default, the output is written to stdout.
 */
class Interpreter(writer: Writer = PrintWriter(System.out)) : BaseErrorListener() {
    private var interpretedLines = 0

    private val semanticAnalyzer = SemanticAnalyzer()
    private var symbolTable : Map<String, Type> = mapOf()

    private val executor = Executor(writer)
    private var scope : Map<String, Value> = mapOf()

    private val errorListener = ErrorListener()

    /**
     * Interprets the passed statements and prints the output to the specified writer. This function can be called
     * consecutively to interpret more statements while preserving the program context (variable assignments) from
     * previous calls.
     *
     * @throws SeqLangException which stores the line number on which the error occurred. When the exception is
     * thrown on a consecutive call, the line number will be relative to all statements that were passed to the interpreter.
     */
    fun interpret(statements: String) {
        if (statements.isBlank()) {
            return
        }

        val input = CharStreams.fromString(statements)

        val lexer = SeqLangLexer(input)
        lexer.removeErrorListeners()
        lexer.addErrorListener(errorListener)

        val tokens = CommonTokenStream(lexer)

        val parser = SeqLangParser(tokens)
        parser.removeErrorListeners()
        parser.addErrorListener(errorListener)

        val parseTree = parser.program()

        val ast = ASTConstructor().constructAST(parseTree)

        try {
            val newSymbolTable = semanticAnalyzer.analyze(ast, symbolTable)
            val newScope = executor.execute(ast, scope)

            interpretedLines += ast.statements.size
            errorListener.interpretedLines += ast.statements.size

            this.symbolTable = newSymbolTable
            this.scope = newScope
        } catch (e : SeqLangException) {
            // TODO: could do error handling via the listener pattern to have symmetry with lexer + parser
            throw SeqLangException(e.message!!, e.lineNumber + interpretedLines)
        }
    }
}

private class ErrorListener : BaseErrorListener() {
    var interpretedLines = 0
    override fun syntaxError(
        recognizer: Recognizer<*, *>?,
        offendingSymbol: Any?,
        line: Int,
        charPositionInLine: Int,
        msg: String?,
        e: RecognitionException?
    ) {
        throw SeqLangException("invalid syntax" + if (msg != null) ": $msg" else "" , line + interpretedLines)
    }
}