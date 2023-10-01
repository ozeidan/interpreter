package interpreter

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.io.CharArrayWriter
import kotlin.test.assertEquals

class InterpreterTest {
    private lateinit var charArrayWriter : CharArrayWriter
    private lateinit var toTest : Interpreter

    @BeforeEach
    fun setup() {
        charArrayWriter = CharArrayWriter()
        toTest = Interpreter(charArrayWriter)
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

        assertProgramOutput(piProgram, "pi = 3.143588659585788")
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

        assertProgramOutput(program, "9.8")
    }

    @Test
    fun shouldShadowCorrectly() {
        val piProgram = """
            var a = 1
            var a = 2
            out a
        """.trimIndent()

        assertProgramOutput(piProgram, "2")
    }

    @Test
    fun shouldFormatSequenceCorrectly() {
        val program = """
            var seq = {1, 4}
            var mapped = map(seq, n -> n ^ 2)
            out mapped
        """.trimIndent()

        assertProgramOutput(program, "{ 1, 4, 9, 16 }")
    }

    @Test
    fun shouldFormatLongSequenceCorrectly() {
        val program = """
            var seq = {1, 15}
            out seq
        """.trimIndent()

        assertProgramOutput(program, "{ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, ... }")
    }

    @Test
    fun shouldConvertSequenceToFloat() {

        val program = """
            var seq = {1, 4}
            var mapped = map(seq, n -> n ^ 2 / 2.0)
            out mapped
        """.trimIndent()

        assertProgramOutput(program, "{ 0.5, 2.0, 4.5, 8.0 }")
    }

    @Test
    fun shouldComputeFloatWhenAddingIntegerAndFloat() {

        val program = """
            out 2 + 3.0
        """.trimIndent()

        assertProgramOutput(program, "5.0")
    }

    @Test
    fun shouldComputeFloatWhenDividingIntegerAndInteger() {

        val program = """
            out 3 / 2
        """.trimIndent()

        assertProgramOutput(program, "1.5")
    }

    @Test
    fun shouldComputeFloatWhenMultiplyingFloatAndFloat() {

        val program = """
            out 3.0 * 2.0
        """.trimIndent()

        assertProgramOutput(program, "6.0")
    }

    @Test
    fun shouldKeepProgramContextBetweenInvocations() {
        val firstStatement = "var a = 5"
        val secondStatement = "out a"
        assertProgramOutput(firstStatement, "")
        assertProgramOutput(secondStatement, "5")
    }

    @Test
    fun shouldFormatFloatProperly() {

        val program = """
            out 3.0
        """.trimIndent()

        assertProgramOutput(program, "3.0")
    }

    @Test
    fun shouldFormatIntegerProperly() {

        val program = """
            out 3
        """.trimIndent()

        assertProgramOutput(program, "3")
    }

    @Test
    fun shouldAddFloatAndIntegerCorrectly() {
        assertProgramOutput(
            """
            out 3.0 + 3
        """.trimIndent(), "6.0"
        )
    }

    @Test
    fun shouldSubtractFloatFromFloatCorrectly() {
        assertProgramOutput(
            """
            out 3.0 - 3.0
        """.trimIndent(), "0.0"
        )
    }

    @Test
    fun shouldApplyPowerOperatorOnFloatCorrectly() {
        assertProgramOutput("""
            out 3.0 ^ 2
        """.trimIndent(), "9.0")
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
    fun shouldThrowOnDivisionByFloatZero() {
        assertThrows(
            """
            out 1 / (10 - 10.0)
            """.trimIndent(),
            1
        )
    }

    @Test
    fun shouldThrowWhenSequenceExpressionIsInvalid() {
        assertThrows(
            """
            out {4, 3}
            """.trimIndent(),
            1
        )

        assertThrows(
            """
            out {4, 4 - 1}
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
    fun shouldThrowWhenSequenceBoundaryIsSequence() {
        assertThrows(
            """
            out {{1, 2}, 3}
            """.trimIndent(),
            1
        )
    }

    @Test
    fun shouldThrowWhenSequenceBoundaryIsFloatingPoint() {
        assertThrows(
            """
            out {1, 3.0}
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

    @Test
    fun shouldCrashWhenAccessingGlobalVariableInLambda() {
        val program = """
            var notAccessible = 3
            var a = {1, 4}
            out map(a, x -> notAccessible + x)
        """.trimIndent()

        assertThrows(program, 3)
    }

    @Test
    fun shouldCrashWhenAccessingGlobalVariableInReducingLambda() {
        val program = """
            var notAccessible = 3
            var a = {1, 4}
            out reduce(a, 1.0, x y -> notAccessible + x + y)
        """.trimIndent()

        assertThrows(program, 3)
    }

    @Test
    fun shouldntUpdateScopeWhenExecutionCrashes() {
        val erroneousStatments = """
            var a = 1
            invalid
        """.trimIndent()

        assertThrows(erroneousStatments, 2)

        val accessingStatements = """
            out a
        """.trimIndent()

        assertThrows(accessingStatements, 1)
    }

    private fun assertThrows(program: String, errorOnLineNumber : Int) {
        val exception = assertThrows<SeqLangException> { toTest.interpret(program) }
        assertEquals(errorOnLineNumber, exception.lineNumber)
    }


    @Test
    fun shouldntCrashWhenComputingSequenceTwice() {
        val program = """
            var a = {1, 4}
            out a
            out a
        """.trimIndent()

        toTest.interpret(program)
    }

}