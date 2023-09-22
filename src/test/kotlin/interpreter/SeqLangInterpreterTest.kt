package interpreter

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
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
    fun shouldCalculatePiCorrectly() {
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
    @Disabled
    fun shouldEvaluateLargeSequences() {
        val piProgram = """
            var n = 50000000
            var seq = map({0, n}, i -> (-1)^i / (2 * i + 1))
            var pi = 4 * reduce(seq, 0, x y -> x + y)
            print "pi = "
            out pi
        """.trimIndent()
        toTest.interpret(piProgram)
    }

    @Test
    fun shouldApplyOperatorsInCorrectOrder() {
        val program = "out 2 * 4 + 3 ^ 2 / 5"

        assertProgramOutput(program, "12.5")
    }

    @Test
    fun shouldShadowCorrectly() {
        val piProgram = """
            var a = 1
            var a = 2
            out a
        """.trimIndent()

        assertProgramOutput(piProgram, "2.0")
    }

    @Test
    fun shouldFormatSequenceCorrectly() {
        val program = """
            var seq = {1, 4}
            var mapped = map(seq, n -> n ^ 2)
            out mapped
        """.trimIndent()

        assertProgramOutput(program, "{ 1.0, 4.0, 9.0, 16.0 }")
    }

    @Test
    fun shouldKeepProgramContextBetweenInvocations() {
        val firstStatement = "var a = 5"
        val secondStatement = "out a"
        assertProgramOutput(firstStatement, "")
        assertProgramOutput(secondStatement, "5.0")
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
            1
        )
    }

    @Test
    fun shouldThrowOnDivisionByZero() {
        assertThrows(
            """
            out 1 / 0
            """.trimIndent(),
            1
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
            3
        )
    }

    @Test
    fun shouldThrowWhenSubtractingScalarFromSequence() {
        assertThrows(
            """
            var scalar = reduce({1, 4}, 0, x y -> x + y)
            var shouldntCompute = {0, 6} + scalar
            """.trimIndent(),
            2
        )
    }

    @Test
    fun shouldThrowWhenUsingMapOnScalar() {
        assertThrows(
            """
            var n = 3
            out map(n, x -> x + 1)
            """.trimIndent(),
            2
        )
    }

    @Test
    fun shouldThrowWhenPassingSequenceAsNeutralElement() {
        assertThrows(
            """
            var n = {0, 5}
            out reduce({5, 7}, n, x y -> x * y)
            """.trimIndent(),
            2
        )
    }

    @Test
    fun shouldThrowWhenInvalidSyntax() {
        assertThrows(
            """
            out {0 5}
            """.trimIndent(),
            1
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
            4
        )
    }

    @Test
    fun shouldReportCorrectLineNumberOnConsecutiveInvocations() {
        val correctStatement = "var n = 1"
        val erroneousStatement = "out b"
        assertDoesNotThrow { toTest.interpret(correctStatement) }
        assertThrows(erroneousStatement, 2)
    }

    private fun assertThrows(program: String, errorOnLineNumber : Int) {
        val exception = assertThrows<SeqLangException> { toTest.interpret(program) }
        assertEquals(errorOnLineNumber, exception.lineNumber)
    }
}