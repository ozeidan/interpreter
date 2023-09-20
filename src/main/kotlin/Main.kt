import org.antlr.v4.runtime.*;

fun main() {
    // create a CharStream that reads from standard input
    val input = CharStreams.fromFileName("examples/pi")

    // create a lexer that feeds off of input CharStream
    val lexer = SeqLangLexer(input)

    // create a buffer of tokens pulled from the lexer
    val tokens = CommonTokenStream(lexer)

    // create a parser that feeds off the tokens buffer
    val parser = SeqLangParser(tokens)

    val tree = parser.program()
    println(tree.toStringTree(parser)) // print LISP-style tree
}