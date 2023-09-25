package interpreter.ast

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class DefaultASTVisitorTest {
    private class FloatLiteralSummingVisitor : ASTVisitor<Double> {
        override fun onFloatLiteralNodeVisited(floatLiteralNode: FloatLiteralNode, context: Double): Double {
            return context + floatLiteralNode.value
        }
    }
    private class FloatLiteralListCreatingVisitor : ASTVisitor<List<Double>> {
        override fun onFloatLiteralNodeVisited(
            floatLiteralNode: FloatLiteralNode,
            context: List<Double>
        ): List<Double> {
            return context + floatLiteralNode.value
        }
    }

    private val testProgram = ProgramNode(listOf(
        PrintExpressionNode(1,
            BinOpNode(
                BinaryOperator.ADDITION,
                FloatLiteralNode(8.0),
                FloatLiteralNode(7.0)
            )
        ),
        VariableDeclarationNode(2, "x",
            MappingNode(
                SequenceLiteralNode(FloatLiteralNode(5.0), FloatLiteralNode(3.0)),
                UnaryLambda("i",
                    ReducingNode(
                        SequenceLiteralNode(FloatLiteralNode(1.0), FloatLiteralNode(7.0)),
                        FloatLiteralNode(1.0),
                        BinaryLambda("a", "b", FloatLiteralNode(3.0))
            ))
        ))
    ))

    @Test
    fun shouldPassThroughContextCorrectly() {
        val sum = FloatLiteralSummingVisitor().visit(testProgram, 0.0)
        assertEquals(35.0, sum)
    }

    @Test
    fun shouldVisitNodesInCorrectOrder() {
        val expectedLiteralOrder = listOf(8.0, 7.0, 5.0, 3.0, 1.0, 7.0, 1.0, 3.0)
        val actualLiteralOrder = FloatLiteralListCreatingVisitor().visit(testProgram, listOf())
        assertEquals(expectedLiteralOrder, actualLiteralOrder)
    }
}