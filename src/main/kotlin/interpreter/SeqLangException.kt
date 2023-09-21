package interpreter

/**
 * A SeqLangException is thrown when a SeqLang program contains an error
 */
class SeqLangException(errorMessage: String, val lineNumber: Int) : Exception("error on line $lineNumber: $errorMessage")