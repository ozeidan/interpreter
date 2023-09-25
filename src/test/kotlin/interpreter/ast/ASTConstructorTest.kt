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
            VariableDeclarationNode(1, "n", FloatLiteralNode(500.0)),
            VariableDeclarationNode(2, "seq",
                MappingNode(SequenceNode(FloatLiteralNode(0.0), VariableAccessNode("n")),
                    UnaryLambda("i", BinOpNode(BinaryOperator.ADDITION, VariableAccessNode("i"),
                        FloatLiteralNode(1.0))))),
            VariableDeclarationNode(3, "someval",
                BinOpNode(BinaryOperator.MULTIPLICATION, FloatLiteralNode(4.0),
                    ReducingNode(VariableAccessNode("seq"), FloatLiteralNode(0.0),
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
                    FloatLiteralNode(2.0),
                    BinOpNode(
                        BinaryOperator.MULTIPLICATION,
                        FloatLiteralNode(3.0),
                        BinOpNode(
                            BinaryOperator.POWER,
                            FloatLiteralNode(4.0),
                            FloatLiteralNode(2.0)
                        )
                    )
                ),
                FloatLiteralNode(7.0)
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