package interpreter.ast

import SeqLangLexer
import SeqLangParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ASTConstructorTest {
    private var toTest = ASTConstructor()

    @Test
    fun shouldConstructASTCorrectly() {
        val testProgram = """
        var n = 500
        var seq = map({0, n}, i -> i + 1)
        var someval = 4 * reduce(seq, 0, x y -> x + y)
        print "hello"
        out someval
        """.trimIndent()

        val expectedAST = ProgramNode(listOf(
            VariableDeclarationNode(1, "n", IntegerLiteralNode(500)),
            VariableDeclarationNode(2, "seq",
                MappingNode(SequenceNode(IntegerLiteralNode(0), VariableAccessNode("n")),
                    UnaryLambda("i", BinOpNode(BinaryOperator.ADDITION, VariableAccessNode("i"),
                        IntegerLiteralNode(1))))),
            VariableDeclarationNode(3, "someval",
                BinOpNode(BinaryOperator.MULTIPLICATION, IntegerLiteralNode(4),
                    ReducingNode(VariableAccessNode("seq"), IntegerLiteralNode(0),
                        BinaryLambda("x", "y",
                            BinOpNode(BinaryOperator.ADDITION, VariableAccessNode("x"), VariableAccessNode("y")))
                    ))),
            PrintStringNode(4, "hello"),
            PrintExpressionNode(5, VariableAccessNode("someval"))
        ))

        assertGeneratesAST(testProgram, expectedAST)
    }

    @Test
    fun shouldConstructComplexExpressionCorrectly() {
        val testProgram = "out 2 + 3 * 4 ^ 2 + 7"
        val expectedAST = ProgramNode(listOf(PrintExpressionNode(1,
            BinOpNode(
                BinaryOperator.ADDITION,
                BinOpNode(
                    BinaryOperator.ADDITION,
                    IntegerLiteralNode(2),
                    BinOpNode(
                        BinaryOperator.MULTIPLICATION,
                        IntegerLiteralNode(3),
                        BinOpNode(
                            BinaryOperator.POWER,
                            IntegerLiteralNode(4),
                            IntegerLiteralNode(2)
                        )
                    )
                ),
                IntegerLiteralNode(7)
        ))))

        assertGeneratesAST(testProgram, expectedAST)
    }

    @Test
    fun shouldConstructNumberLiteralsCorrectly() {
        val testProgram = """
            var a = 3
            var b = 3.0
        """.trimIndent()

        val expectedAST = ProgramNode(listOf(
            VariableDeclarationNode(1, "a", IntegerLiteralNode(3)),
            VariableDeclarationNode(2, "b", FloatLiteralNode(3.0))))

        assertGeneratesAST(testProgram, expectedAST)
    }

    private fun assertGeneratesAST(program: String, expected: ASTNode) {
        val input = CharStreams.fromString(program)
        val lexer = SeqLangLexer(input)
        val tokens = CommonTokenStream(lexer)
        val parser = SeqLangParser(tokens)
        val tree = parser.program()
        assertEquals(expected, toTest.constructAST(tree))
    }
}