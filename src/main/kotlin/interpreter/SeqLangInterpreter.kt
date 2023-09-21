package interpreter

import java.io.OutputStreamWriter
import java.io.Writer

/**
 * Interpreter of SeqLang.
 *
 * @constructor Optionally takes a writer that the output of the interpreted programs is written to.
 * By default, the output is written to stdout.
 */
class SeqLangInterpreter(writer: Writer = OutputStreamWriter(System.out)) {
    /**
     * Interprets the passed statements and prints the output to the specified writer. This function can be called
     * consecutively to interpret more statements while preserving the program context (variable assignments) from
     * previous calls.
     *
     * @throws InterpreterException, which stores the line number on which the error occurred. When the exception is
     * thrown on a consecutive call, the line number will be relative to all statements that were passed to the interpreter.
     */
    fun interpret(statements: String) {

    }
}