package interpreter.ast

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class DefaultASTVisitorTest {
    private class FloatLiteralSummingVisitor : ASTVisitor<Double, Unit>() {
        override fun onFloatLiteralNodeVisited(floatLiteralNode: FloatLiteralNode, context: Double): Pair<Double, Unit> {
            return Pair(context + floatLiteralNode.value, Unit)
        }

        override fun onIntegerLiteralNodeVisited(
            integerLiteralNode: IntegerLiteralNode,
            context: Double
        ): Pair<Double, Unit> {
            return Pair(this.visitChildren(integerLiteralNode, context), Unit)
        }

        override fun onVariableAccessNodeVisited(
            variableAccessNode: VariableAccessNode,
            context: Double
        ): Pair<Double, Unit> {
            return Pair(this.visitChildren(variableAccessNode, context), Unit)
        }

        override fun onBinOpNodeVisited(binOpNode: BinOpNode, context: Double): Pair<Double, Unit> {
            return Pair(this.visitChildren(binOpNode, context), Unit)
        }

        override fun onMappingNodeVisited(mappingNode: MappingNode, context: Double): Pair<Double, Unit> {
            return Pair(this.visitChildren(mappingNode, context), Unit)
        }

        override fun onReducingNodeVisited(reducingNode: ReducingNode, context: Double): Pair<Double, Unit> {
            return Pair(this.visitChildren(reducingNode, context), Unit)
        }

        override fun onSequenceNodeVisited(sequenceNode: SequenceNode, context: Double): Pair<Double, Unit> {
            return Pair(this.visitChildren(sequenceNode, context), Unit)
        }
    }
    private class FloatLiteralListCreatingVisitor : ASTVisitor<List<Double>, Unit>() {
        override fun onFloatLiteralNodeVisited(
            floatLiteralNode: FloatLiteralNode,
            context: List<Double>
        ): Pair<List<Double>, Unit> {
            return Pair(context + floatLiteralNode.value, Unit)
        }

        override fun onIntegerLiteralNodeVisited(
            integerLiteralNode: IntegerLiteralNode,
            context: List<Double>
        ): Pair<List<Double>, Unit> {
            return Pair(visitChildren(integerLiteralNode, context), Unit)
        }

        override fun onVariableAccessNodeVisited(
            variableAccessNode: VariableAccessNode,
            context: List<Double>
        ): Pair<List<Double>, Unit> {
            return Pair(visitChildren(variableAccessNode, context), Unit)
        }

        override fun onBinOpNodeVisited(binOpNode: BinOpNode, context: List<Double>): Pair<List<Double>, Unit> {
            return Pair(visitChildren(binOpNode, context), Unit)
        }

        override fun onMappingNodeVisited(mappingNode: MappingNode, context: List<Double>): Pair<List<Double>, Unit> {
            return Pair(visitChildren(mappingNode, context), Unit)
        }

        override fun onReducingNodeVisited(
            reducingNode: ReducingNode,
            context: List<Double>
        ): Pair<List<Double>, Unit> {
            return Pair(visitChildren(reducingNode, context), Unit)
        }

        override fun onSequenceNodeVisited(
            sequenceNode: SequenceNode,
            context: List<Double>
        ): Pair<List<Double>, Unit> {
            return Pair(visitChildren(sequenceNode, context), Unit)
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
                SequenceNode(FloatLiteralNode(5.0), FloatLiteralNode(3.0)),
                UnaryLambda("i",
                    ReducingNode(
                        SequenceNode(FloatLiteralNode(1.0), FloatLiteralNode(7.0)),
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