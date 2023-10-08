package interpreter

/**
 * A SeqLangException is thrown when a SeqLang program contains an error
 */
class SeqLangException(message: String, val lineNumber: Int) : Exception(message) {
    override fun toString(): String {
        return "error on line $lineNumber: $message"
    }
}