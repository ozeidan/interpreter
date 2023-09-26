package interpreter

import interpreter.ast.ASTNode

fun <T> addLineNumberToExceptions(statementNode: ASTNode.Statement, function: () -> T) : T {
    try {
        return function()
    } catch (e: InterpreterException) {
        throw SeqLangException(e.message ?: "", statementNode.linenumber)
    }
}