package interpreter.ast

data class UnaryLambda(val identifier: String, val expression: ExpressionNode)

data class MappingNode(val sequenceExpression: ExpressionNode, val lambda: UnaryLambda) : ExpressionNode()
