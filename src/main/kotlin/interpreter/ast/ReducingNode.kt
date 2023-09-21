package interpreter.ast

data class BinaryLambda(val leftIdentifier: String, val rightIdentifier: String, val expression: ExpressionNode)

data class ReducingNode(val sequenceExpression: ExpressionNode,
                   val neutralElementExpression: ExpressionNode,
                   val lambda: BinaryLambda) : ExpressionNode()
