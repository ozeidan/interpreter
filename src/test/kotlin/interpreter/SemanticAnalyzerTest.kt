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
                    FloatLiteralNode(3.0),
                    FloatLiteralNode(4.0)))))

        assertDoesNotThrow { toTest.analyze(ast) }
    }

    @Test
    fun shouldAllowAdditionOfScalarAndReductionResult() {
        val ast =
            ProgramNode(listOf(PrintExpressionNode(1,
                BinOpNode(BinaryOperator.ADDITION,
                    FloatLiteralNode(3.0),
                    ReducingNode(
                        SequenceNode(IntegerLiteralNode(0), IntegerLiteralNode(3)),
                        FloatLiteralNode(3.0),
                        BinaryLambda(
                            "x",
                            "y",
                            BinOpNode(
                                BinaryOperator.ADDITION,
                                FloatLiteralNode(3.0),
                                FloatLiteralNode(4.0)))
                    )))))

        assertDoesNotThrow { toTest.analyze(ast) }
    }

    @Test
    fun shouldAllowSequenceBoundaryExpressionThatEvaluatesToInteger() {
        val ast =
            ProgramNode(listOf(PrintExpressionNode(1,
                SequenceNode(
                    IntegerLiteralNode(3),
                    ReducingNode(
                        SequenceNode(IntegerLiteralNode(1), IntegerLiteralNode(5)),
                        IntegerLiteralNode(0),
                        BinaryLambda(
                            "x",
                            "y",
                            BinOpNode(
                                BinaryOperator.ADDITION,
                                VariableAccessNode("x"),
                                VariableAccessNode("y")))
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
                        SequenceNode(IntegerLiteralNode(1), IntegerLiteralNode(10)),
                        UnaryLambda(
                            "x",
                            ReducingNode(
                                SequenceNode(IntegerLiteralNode(1), VariableAccessNode("x")),
                                FloatLiteralNode(1.0),
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
                        FloatLiteralNode(0.0),
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
                    FloatLiteralNode(3.0),
                    SequenceNode(
                        FloatLiteralNode(0.0),
                        FloatLiteralNode(3.0))))))

        assertThrows<SeqLangException> { toTest.analyze(ast) }
    }

    @Test
    fun shouldThrowOnAdditionOfMappingResult() {
        val ast =
            ProgramNode(listOf(PrintExpressionNode(1,
                BinOpNode(
                    BinaryOperator.ADDITION,
                    FloatLiteralNode(3.0),
                    MappingNode(
                        SequenceNode(
                            FloatLiteralNode(1.0),
                            FloatLiteralNode(3.0)
                        ),
                        UnaryLambda(
                            "x",
                            BinOpNode(
                                BinaryOperator.ADDITION,
                                VariableAccessNode("x"),
                                FloatLiteralNode(3.0)
                            )
                        ))))))

        assertThrows<SeqLangException> { toTest.analyze(ast) }
    }

    @Test
    fun shouldPropagateTypeInfoCorrectly() {
        val ast =
            ProgramNode(listOf(
                VariableDeclarationNode(1, "a",
                    SequenceNode(FloatLiteralNode(0.0), FloatLiteralNode(3.0))),
                PrintExpressionNode(1,
                    BinOpNode(
                        BinaryOperator.ADDITION,
                        FloatLiteralNode(3.0),
                        VariableAccessNode("a")
            ))))

        assertThrows<SeqLangException> { toTest.analyze(ast) }
    }

    @Test
    fun shouldThrowWhenMappingOverScalar() {
        val ast =
            ProgramNode(listOf(PrintExpressionNode(1,
                MappingNode(
                    FloatLiteralNode(3.0),
                    UnaryLambda(
                        "x",
                        BinOpNode(
                            BinaryOperator.ADDITION,
                            FloatLiteralNode(3.0),
                            FloatLiteralNode(4.0)))
                )
            )))

        assertThrows<SeqLangException> { toTest.analyze(ast) }
    }

    @Test
    fun shouldThrowWhenMappingLambdaResultIsSequence() {
        val ast =
            ProgramNode(listOf(PrintExpressionNode(1,
                MappingNode(
                    SequenceNode(FloatLiteralNode(3.0), FloatLiteralNode(4.0)),
                    UnaryLambda(
                        "x",
                        SequenceNode(FloatLiteralNode(3.0), FloatLiteralNode(4.0)),
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
                    FloatLiteralNode(3.0),
                    FloatLiteralNode(3.0),
                    BinaryLambda(
                        "x",
                        "y",
                        BinOpNode(
                            BinaryOperator.ADDITION,
                            FloatLiteralNode(3.0),
                            FloatLiteralNode(4.0)))
                )
            )))

        assertThrows<SeqLangException> { toTest.analyze(ast) }
    }

    @Test
    fun shouldThrowWhenNeutralElementIsSequence() {
        val ast =
            ProgramNode(listOf(PrintExpressionNode(1,
                ReducingNode(
                    SequenceNode(FloatLiteralNode(3.0), FloatLiteralNode(4.0)),
                    SequenceNode(FloatLiteralNode(3.0), FloatLiteralNode(4.0)),
                    BinaryLambda(
                        "x",
                        "y",
                        BinOpNode(
                            BinaryOperator.ADDITION,
                            FloatLiteralNode(3.0),
                            FloatLiteralNode(4.0)))
                )
            )))

        assertThrows<SeqLangException> { toTest.analyze(ast) }
    }

    @Test
    fun shouldThrowWhenLambdaResultTypeIsSequence() {
        val ast =
            ProgramNode(listOf(PrintExpressionNode(1,
                ReducingNode(
                    SequenceNode(FloatLiteralNode(3.0), FloatLiteralNode(4.0)),
                    SequenceNode(FloatLiteralNode(3.0), FloatLiteralNode(4.0)),
                    BinaryLambda(
                        "x",
                        "y",
                        MappingNode(
                            SequenceNode(
                                FloatLiteralNode(1.0),
                                FloatLiteralNode(2.0)
                            ),
                            UnaryLambda(
                                "x",
                                SequenceNode(
                                    FloatLiteralNode(1.0),
                                    VariableAccessNode("x")))))
                )
            )))

        assertThrows<SeqLangException> { toTest.analyze(ast) }
    }

    @Test
    fun shouldThrowWhenSequenceBoundaryExpressionIsFloat() {
        val ast =
            ProgramNode(listOf(
                PrintExpressionNode(1,
                    SequenceNode(
                        FloatLiteralNode(1.0),
                        FloatLiteralNode(2.0)))))

        assertThrows<SeqLangException> { toTest.analyze(ast) }
    }

    @Test
    fun shouldThrowWhenSequenceBoundaryExpressionEvaluatesToFloat() {
        val ast =
            ProgramNode(listOf(
                PrintExpressionNode(1,
                    SequenceNode(
                        IntegerLiteralNode(1),
                        BinOpNode(BinaryOperator.ADDITION,
                            IntegerLiteralNode(1),
                            FloatLiteralNode(3.5))))))

        assertThrows<SeqLangException> { toTest.analyze(ast) }
    }

    @Test
    fun shouldPropagateReductionExpressionType() {
        val ast =
            ProgramNode(listOf(PrintExpressionNode(1,
                SequenceNode(
                    IntegerLiteralNode(3),
                    ReducingNode(
                        SequenceNode(IntegerLiteralNode(1), IntegerLiteralNode(5)),
                        FloatLiteralNode(0.0), // result of reduction should be float now
                        BinaryLambda(
                            "x",
                            "y",
                            BinOpNode(
                                BinaryOperator.ADDITION,
                                VariableAccessNode("x"),
                                VariableAccessNode("y")))
                    )))))

        assertThrows<SeqLangException> { toTest.analyze(ast) }
    }


    @Test
    fun shouldThrowWhenAccessingUndeclaredVariable() {
        val ast =
            ProgramNode(listOf(
                VariableDeclarationNode(1, "a", FloatLiteralNode(1.0)),
                VariableDeclarationNode(2, "b", VariableAccessNode("c"))))

        assertThrows<SeqLangException> { toTest.analyze(ast) }
    }

    @Test
    fun shouldThrowWhenMapLambdaEvaluatesToSequence() {
        val ast =
            ProgramNode(listOf(
                PrintExpressionNode(1,
                    MappingNode(
                        SequenceNode(
                            IntegerLiteralNode(1),
                            IntegerLiteralNode(4)
                        ),
                        UnaryLambda("x",
                            SequenceNode(
                                VariableAccessNode("x"),
                                IntegerLiteralNode(10)
                            ))
                    )
                )
            ))

        assertThrows<SeqLangException> { toTest.analyze(ast) }
    }

    @Test
    fun shouldThrowWhenReduceLambdaEvaluatesToSequence() {
        val ast =
            ProgramNode(listOf(
                PrintExpressionNode(1,
                    ReducingNode(
                        SequenceNode(
                            IntegerLiteralNode(1),
                            IntegerLiteralNode(4)
                        ),
                        IntegerLiteralNode(1),
                        BinaryLambda("x", "y",
                            SequenceNode(
                                VariableAccessNode("x"),
                                IntegerLiteralNode(10)
                            ))
                    )
                )
            ))

        assertThrows<SeqLangException> { toTest.analyze(ast) }
    }
}
