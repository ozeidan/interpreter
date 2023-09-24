package interpreter

import interpreter.ast.StatementNode

fun <T> addLineNumberToExceptions(statementNode: StatementNode, function: () -> T) : T {
    try {
        return function()
    } catch (e: InterpreterException) {
        throw SeqLangException(e.message ?: "", statementNode.linenumber)
    }
}