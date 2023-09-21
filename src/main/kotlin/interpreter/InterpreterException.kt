package interpreter

class InterpreterException(errorMessage: String, val lineNumber: Int) : Exception("error on line $lineNumber: message")