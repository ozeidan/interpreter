import interpreter.Interpreter
import interpreter.SeqLangException

fun main() {
    val interpreter = Interpreter()
    println("Welcome to SeqLang!")

    while (true) {
        print("> ")
        val input = readlnOrNull() ?: break
        try {
            interpreter.interpret(input)
            println()
        } catch (e: SeqLangException) {
            println(e.message)
        }
    }
}
