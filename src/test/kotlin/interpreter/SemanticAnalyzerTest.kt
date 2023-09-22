package interpreter

import interpreter.ast.*
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows

class SemanticAnalyzerTest {
    private val toTest = SemanticAnalyzer()

    @Test
    fun shouldAllowValidOperation() {
        val ast =
            ProgramNode(listOf(PrintExpressionNode(1,
                BinOpNode(
                    BinaryOperator.ADDITION,
                    NumberLiteralNode(3.0),
                    NumberLiteralNode(4.0)))))

        assertDoesNotThrow { toTest.analyze(ast) }
    }

    @Test
    fun shouldAllowAdditionOfScalarAndReductionResult() {
        val ast =
            ProgramNode(listOf(PrintExpressionNode(1,
                BinOpNode(BinaryOperator.ADDITION,
                    NumberLiteralNode(3.0),
                    ReducingNode(
                        SequenceNode(NumberLiteralNode(0.0), NumberLiteralNode(3.0)),
                        NumberLiteralNode(3.0),
                        BinaryLambda(
                            "x",
                            "y",
                            BinOpNode(
                                BinaryOperator.ADDITION,
                                NumberLiteralNode(3.0),
                                NumberLiteralNode(4.0)))
                    )))))

        assertDoesNotThrow { toTest.analyze(ast) }
    }

    @Test
    fun shouldAllowComplexValidExpression() {
        // val n = map({1, 10}, x -> reduce({1, x}, 1.0, x y -> x * y))
        // out reduce(n, 0.0, x y -> x + y)
        val ast =
            ProgramNode(listOf(
                VariableDeclarationNode(1,
                    "n",
                    MappingNode(
                        SequenceNode(NumberLiteralNode(1.0), NumberLiteralNode(10.0)),
                        UnaryLambda(
                            "x",
                            ReducingNode(
                                SequenceNode(NumberLiteralNode(1.0), VariableAccessNode("x")),
                                NumberLiteralNode(1.0),
                                BinaryLambda(
                                    "x",
                                    "y",
                                    BinOpNode(
                                        BinaryOperator.MULTIPLICATION,
                                        VariableAccessNode("x"),
                                        VariableAccessNode("y")
                                    ))
                            )
                        )

                    )),
                PrintExpressionNode(1,
                    ReducingNode(
                        VariableAccessNode("n"),
                        NumberLiteralNode(0.0),
                        BinaryLambda(
                            "x",
                            "y",
                            BinOpNode(
                                BinaryOperator.ADDITION,
                                VariableAccessNode("x"),
                                VariableAccessNode("y")
                            )
                        )
                    )
                )
                ))

        assertDoesNotThrow { toTest.analyze(ast) }
    }

    @Test
    fun shouldThrowOnAdditionOfMissmatchingTypes() {
        val ast =
            ProgramNode(listOf(PrintExpressionNode(1,
                BinOpNode(
                    BinaryOperator.ADDITION,
                    NumberLiteralNode(3.0),
                    SequenceNode(
                        NumberLiteralNode(0.0),
                        NumberLiteralNode(3.0))))))

        assertThrows<SeqLangException> { toTest.analyze(ast) }
    }

    @Test
    fun shouldThrowOnAdditionOfMappingResult() {
        val ast =
            ProgramNode(listOf(PrintExpressionNode(1,
                BinOpNode(
                    BinaryOperator.ADDITION,
                    NumberLiteralNode(3.0),
                    MappingNode(
                        SequenceNode(
                            NumberLiteralNode(1.0),
                            NumberLiteralNode(3.0)
                        ),
                        UnaryLambda(
                            "x",
                            BinOpNode(
                                BinaryOperator.ADDITION,
                                VariableAccessNode("x"),
                                NumberLiteralNode(3.0)
                            )
                        ))))))

        assertThrows<SeqLangException> { toTest.analyze(ast) }
    }

    @Test
    fun shouldPropagateTypeInfoCorrectly() {
        val ast =
            ProgramNode(listOf(
                VariableDeclarationNode(1, "a",
                    SequenceNode(NumberLiteralNode(0.0), NumberLiteralNode(3.0))),
                PrintExpressionNode(1,
                    BinOpNode(
                        BinaryOperator.ADDITION,
                        NumberLiteralNode(3.0),
                        VariableAccessNode("a")
            ))))

        assertThrows<SeqLangException> { toTest.analyze(ast) }
    }

    @Test
    fun shouldThrowWhenMappingOverScalar() {
        val ast =
            ProgramNode(listOf(PrintExpressionNode(1,
                MappingNode(
                    NumberLiteralNode(3.0),
                    UnaryLambda(
                        "x",
                        BinOpNode(
                            BinaryOperator.ADDITION,
                            NumberLiteralNode(3.0),
                            NumberLiteralNode(4.0)))
                )
            )))

        assertThrows<SeqLangException> { toTest.analyze(ast) }
    }

    @Test
    fun shouldThrowWhenMappingLambdaResultIsSequence() {
        val ast =
            ProgramNode(listOf(PrintExpressionNode(1,
                MappingNode(
                    SequenceNode(NumberLiteralNode(3.0), NumberLiteralNode(4.0)),
                    UnaryLambda(
                        "x",
                        SequenceNode(NumberLiteralNode(3.0), NumberLiteralNode(4.0)),
                    )
                )
            )))

        assertThrows<SeqLangException> { toTest.analyze(ast) }
    }

    @Test
    fun shouldThrowWhenReducingOverScalar() {
        val ast =
            ProgramNode(listOf(PrintExpressionNode(1,
                ReducingNode(
                    NumberLiteralNode(3.0),
                    NumberLiteralNode(3.0),
                    BinaryLambda(
                        "x",
                        "y",
                        BinOpNode(
                            BinaryOperator.ADDITION,
                            NumberLiteralNode(3.0),
                            NumberLiteralNode(4.0)))
                )
            )))

        assertThrows<SeqLangException> { toTest.analyze(ast) }
    }

    @Test
    fun shouldThrowWhenNeutralElementIsSequence() {
        val ast =
            ProgramNode(listOf(PrintExpressionNode(1,
                ReducingNode(
                    SequenceNode(NumberLiteralNode(3.0), NumberLiteralNode(4.0)),
                    SequenceNode(NumberLiteralNode(3.0), NumberLiteralNode(4.0)),
                    BinaryLambda(
                        "x",
                        "y",
                        BinOpNode(
                            BinaryOperator.ADDITION,
                            NumberLiteralNode(3.0),
                            NumberLiteralNode(4.0)))
                )
            )))

        assertThrows<SeqLangException> { toTest.analyze(ast) }
    }

    @Test
    fun shouldThrowWhenLambdaResultTypeIsSequence() {
        val ast =
            ProgramNode(listOf(PrintExpressionNode(1,
                ReducingNode(
                    SequenceNode(NumberLiteralNode(3.0), NumberLiteralNode(4.0)),
                    SequenceNode(NumberLiteralNode(3.0), NumberLiteralNode(4.0)),
                    BinaryLambda(
                        "x",
                        "y",
                        MappingNode(
                            SequenceNode(
                                NumberLiteralNode(1.0),
                                NumberLiteralNode(2.0)
                            ),
                            UnaryLambda(
                                "x",
                                SequenceNode(
                                    NumberLiteralNode(1.0),
                                    VariableAccessNode("x")))))
                )
            )))

        assertThrows<SeqLangException> { toTest.analyze(ast) }
    }

    @Test
    fun shouldThrowWhenAccessingUndeclaredVariable() {
        val ast =
            ProgramNode(listOf(
                VariableDeclarationNode(1, "a", NumberLiteralNode(1.0)),
                VariableDeclarationNode(2, "b", VariableAccessNode("c"))))

        assertThrows<SeqLangException> { toTest.analyze(ast) }
    }
}
