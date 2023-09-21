package interpreter.ast

data class PrintStringNode(val lineNumber: Int, val string: String) : StatementNode(lineNumber) {
}
