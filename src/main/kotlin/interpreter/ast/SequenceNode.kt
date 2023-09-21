package interpreter.ast

data class SequenceNode(val lowerBoundInclusive : ExpressionNode, val upperBoundInclusive: ExpressionNode) : ExpressionNode() {
}
