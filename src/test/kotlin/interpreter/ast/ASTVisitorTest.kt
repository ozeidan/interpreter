package interpreter.ast

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class DefaultASTVisitorTest {
    private class NumberLiteralSummingVisitor : ASTVisitor<Double> {
        override fun onNumberLiteralNodeVisited(numberLiteralNode: NumberLiteralNode, context: Double): Double {
            return context + numberLiteralNode.value
        }
    }
    private class NumberLiteralListCreatingVisitor : ASTVisitor<List<Double>> {
        override fun onNumberLiteralNodeVisited(
            numberLiteralNode: NumberLiteralNode,
            context: List<Double>
        ): List<Double> {
            return context + numberLiteralNode.value
        }
    }

    private val testProgram = ProgramNode(listOf(
        PrintExpressionNode(1,
            BinOpNode(
                BinaryOperator.ADDITION,
                NumberLiteralNode(8.0),
                NumberLiteralNode(7.0)
            )
        ),
        VariableDeclarationNode(2, "x",
            MappingNode(
                SequenceNode(NumberLiteralNode(5.0), NumberLiteralNode(3.0)),
                UnaryLambda("i",
                    ReducingNode(
                        SequenceNode(NumberLiteralNode(1.0), NumberLiteralNode(7.0)),
                        NumberLiteralNode(1.0),
                        BinaryLambda("a", "b", NumberLiteralNode(3.0))
            ))
        ))
    ))

    @Test
    fun shouldPassThroughContextCorrectly() {
        val sum = NumberLiteralSummingVisitor().visit(testProgram, 0.0)
        assertEquals(35.0, sum)
    }

    @Test
    fun shouldVisitNodesInCorrectOrder() {
        val expectedLiteralOrder = listOf(8.0, 7.0, 5.0, 3.0, 1.0, 7.0, 1.0, 3.0)
        val actualLiteralOrder = NumberLiteralListCreatingVisitor().visit(testProgram, listOf())
        assertEquals(expectedLiteralOrder, actualLiteralOrder)
    }
}