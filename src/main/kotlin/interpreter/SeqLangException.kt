package interpreter

/**
 * A SeqLangException is thrown when a SeqLang program contains an error
 */
class SeqLangException(override val message: String, val lineNumber: Int) : Exception("error on line $lineNumber: $message") {
    override fun toString(): String {
        return super.message!!
    }
}