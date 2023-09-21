package interpreter

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.CharArrayWriter
import kotlin.test.assertEquals

class SeqLangInterpreterTest {
    private lateinit var charArrayWriter : CharArrayWriter
    private lateinit var toTest : SeqLangInterpreter

    @BeforeEach
    fun setup() {
        charArrayWriter = CharArrayWriter()
        toTest = SeqLangInterpreter(charArrayWriter)
    }

    @Test
    fun shouldWriteOutputCorrectly() {
        val piProgram = """
            var n = 500
            var seq = map({0, n}, i -> (-1)^i / (2 * i + 1))
            var pi = 4 * reduce(seq, 0, x y -> x + y)
            print "pi = "
            out pi
        """.trimIndent()

        assertProgramOutput(piProgram, "3.143588659585788")
    }

    @Test
    fun shouldFormatSequenceCorrectly() {
        val piProgram = """
            var seq = {1, 4}
            var mapped = map(seq, n -> n ^ 2)
            out mapped
        """.trimIndent()

        assertProgramOutput(piProgram, "{ 1, 4, 9, 16 }")
    }

    private fun assertProgramOutput(program: String, output: String) {
        toTest.interpret(program)
        assertEquals(output, charArrayWriter.toString())
    }

    @Test
    fun shouldThrowWhenAccessingUndeclaredVariable() {
        assertThrows(
            """
            var n = a
            """.trimIndent(),
            0
        )
    }

    @Test
    fun shouldThrowOnDivisionByZero() {
        assertThrows(
            """
            out 1 / 0
            """.trimIndent(),
            0
        )
    }

    @Test
    fun shouldThrowWhenAddingSequences() {
        // TODO: maybe this will be supported
        assertThrows(
            """
            var first = {0, 5}
            var second = {0, 6}
            out first + second
            """.trimIndent(),
            2
        )
    }

    @Test
    fun shouldThrowWhenSubtractingScalarFromSequence() {
        assertThrows(
            """
            var b = reduce({1, 4}, 0, x y -> x + y)
            var second = {0, 6} + b
            out first + second
            """.trimIndent(),
            1
        )
    }

    @Test
    fun shouldThrowWhenUsingMapOnScalar() {
        assertThrows(
            """
            var n = 3
            out map(n, x -> x + 1)
            """.trimIndent(),
            1
        )
    }

    @Test
    fun shouldThrowWhenPassingSequenceAsNeutralElement() {
        assertThrows(
            """
            var n = {0, 5}
            out reduce({5, 7}, n, x y -> x * y)
            """.trimIndent(),
            1
        )
    }

    @Test
    fun shouldThrowWhenInvalidSyntax() {
        assertThrows(
            """
            out {0 5}
            """.trimIndent(),
            0
        )
    }

    @Test
    fun shouldThrowWhenInvalidSyntaxWithCorrectLineNumber() {
        assertThrows(
            """
            var n = 500
            var seq = map({0, n}, i -> (-1)^i / (2 * i + 1))
            var pi = 4 * reduce(seq, 0, x y -> x + y)
            print "pi = ";
            out pi
            """.trimIndent(),
            3
        )
    }

    private fun assertThrows(program: String, errorOnLineNumber : Int) {
        val exception = assertThrows<InterpreterException> { toTest.interpret(program) }
        assertEquals(errorOnLineNumber, exception.lineNumber)
    }
}