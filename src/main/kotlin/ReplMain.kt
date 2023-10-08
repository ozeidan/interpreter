import interpreter.Interpreter
import interpreter.SeqLangException
import java.io.BufferedWriter
import java.io.OutputStreamWriter

fun main() {
    val outputWriter = object : BufferedWriter(OutputStreamWriter(System.`out`)) {
        var shouldLineBreak = false
        override fun write(str: String) {
            shouldLineBreak = true
            super.write(str)
        }
        fun writeLineBreakIfNecessary() {
            if (shouldLineBreak) {
                shouldLineBreak = false
                super.write("\n")
                super.flush()
            }
        }
    }

    val interpreter = Interpreter(outputWriter)
    println("Welcome to SeqLang!")

    while (true) {
        print("> ")
        val input = readlnOrNull() ?: break
        try {
            interpreter.interpret(input)
            outputWriter.writeLineBreakIfNecessary()
        } catch (e: SeqLangException) {
            println(e.message!!)
        }
    }
}
