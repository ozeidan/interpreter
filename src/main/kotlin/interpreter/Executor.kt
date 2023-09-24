package interpreter

import interpreter.ast.*
import java.io.Writer
import java.util.stream.IntStream
import java.util.stream.Stream
import kotlin.math.pow

class Executor(private val writer: Writer) {
    private val executingASTVisitor = ExecutingASTVisitor()

    fun execute(ast: ASTNode, scope: Map<String, Eval> = mapOf()) : Map<String, Eval> {
        return executingASTVisitor.visit(ast, ExecutionContext(scope, null, writer)).scope
    }
}

sealed class Eval {
    abstract fun print() : String

    data class Scalar(val value : Double) : Eval() {
        override fun print(): String {
            return value.toString()
        }
    }
    data class Sequence(val stream : Stream<Double>) : Eval() {
        override fun print(): String {
            // TODO: cleaner?
            val middlePart = stream.map { "$it, " }.reduce("") { a, b -> a + b }
            val trimmed = middlePart.substring(0, middlePart.length - 2)
            return "{ $trimmed }"
        }

    }
}

private data class ExecutionContext(
    val scope : Map<String, Eval>,
    val evaluationResult : Eval?,
    val writer: Writer
)

private class ExecutingASTVisitor : ASTVisitor<ExecutionContext> {
    override fun onVariableDeclarationNodeVisited(
        variableDeclarationNode: VariableDeclarationNode,
        context: ExecutionContext
    ): ExecutionContext {
        return addLineNumberToExceptions(variableDeclarationNode) {
            val (_, evaluationResult) = visit(variableDeclarationNode.expression, context)
            context.copy(scope = context.scope + (variableDeclarationNode.identifier to evaluationResult!!))
        }
    }

    override fun onVariableAccessNodeVisited(
        variableAccessNode: VariableAccessNode,
        context: ExecutionContext
    ): ExecutionContext {
        if (!context.scope.containsKey(variableAccessNode.identifier)) {
            println(variableAccessNode.identifier)
        }

        return context.copy(evaluationResult = context.scope[variableAccessNode.identifier]!!)
    }

    override fun onPrintExpressionNodeVisited(
        printExpressionNode: PrintExpressionNode,
        context: ExecutionContext
    ): ExecutionContext {
        return addLineNumberToExceptions(printExpressionNode) {
            context.writer.write(visit(printExpressionNode.expression, context).evaluationResult!!.print())
            context.writer.flush()
            context
        }
    }

    override fun onPrintStringNodeVisited(
        printStringNode: PrintStringNode,
        context: ExecutionContext
    ): ExecutionContext {
        context.writer.write(printStringNode.string)
        context.writer.flush()
        return context
    }

    override fun onBinOpNodeVisited(binOpNode: BinOpNode, context: ExecutionContext): ExecutionContext {
        val (_, leftEvaluationResult) = visit(binOpNode.left, context)
        val (_, rightEvaluationResult) = visit(binOpNode.right, context)

        require(leftEvaluationResult is Eval.Scalar && rightEvaluationResult is Eval.Scalar)

        val evaluationResult = when (binOpNode.operator) {
            BinaryOperator.ADDITION -> Eval.Scalar(leftEvaluationResult.value + rightEvaluationResult.value)
            BinaryOperator.SUBTRACTION -> Eval.Scalar(leftEvaluationResult.value - rightEvaluationResult.value)
            BinaryOperator.MULTIPLICATION -> Eval.Scalar(leftEvaluationResult.value * rightEvaluationResult.value)
            BinaryOperator.DIVISION -> {
                if (rightEvaluationResult.value == 0.0) {
                    throw InterpreterException("attempted division by 0")
                }
                Eval.Scalar(leftEvaluationResult.value / rightEvaluationResult.value)
            }
            BinaryOperator.POWER -> Eval.Scalar(leftEvaluationResult.value.pow(rightEvaluationResult.value))
        }
        return context.copy(evaluationResult = evaluationResult)
    }

    override fun onMappingNodeVisited(mappingNode: MappingNode, context: ExecutionContext): ExecutionContext {
        val (_, sequenceEvaluationResult) = visit(mappingNode.sequenceExpression, context)

        require(sequenceEvaluationResult is Eval.Sequence)

        val newSequence = Eval.Sequence(sequenceEvaluationResult.stream.map {
            val (_, evaluationResult) = visit(
                mappingNode.lambda.expression,
                context.copy(scope = mapOf((mappingNode.lambda.identifier to Eval.Scalar(it))))
            )

            require(evaluationResult is Eval.Scalar)
            evaluationResult.value
        })

        return context.copy(evaluationResult = newSequence)
    }

    override fun onReducingNodeVisited(reducingNode: ReducingNode, context: ExecutionContext): ExecutionContext {
        val (_, sequenceEvaluationResult) = visit(reducingNode.sequenceExpression, context)
        val (_, neutralElementEvaluationResult) = visit(reducingNode.neutralElementExpression, context)

        require(sequenceEvaluationResult is Eval.Sequence && neutralElementEvaluationResult is Eval.Scalar)

        val evaluationResult = sequenceEvaluationResult.stream.reduce(neutralElementEvaluationResult.value) { x, y ->
            val lambdaResult = visit(
                reducingNode.lambda.expression,
                context.copy(
                    scope = mapOf(
                        (reducingNode.lambda.leftIdentifier to Eval.Scalar(x)),
                        (reducingNode.lambda.rightIdentifier to Eval.Scalar(y)),
                    )
                )
            ).evaluationResult

            (lambdaResult as Eval.Scalar).value
        }

        return context.copy(evaluationResult = Eval.Scalar(evaluationResult))
    }

    override fun onSequenceNodeVisited(sequenceNode: SequenceNode, context: ExecutionContext): ExecutionContext {
        val (_, lowerEvaluationResult) = visit(sequenceNode.lowerBoundInclusive, context)
        val (_, upperEvaluationResult) = visit(sequenceNode.upperBoundInclusive, context)

        require(lowerEvaluationResult is Eval.Scalar && upperEvaluationResult is Eval.Scalar)

        if (upperEvaluationResult.value < lowerEvaluationResult.value) {
            throw InterpreterException("lower sequence boundary is higher than upper boundary")
        }

        val newSequence =
            IntStream.rangeClosed(lowerEvaluationResult.value.toInt(), upperEvaluationResult.value.toInt())
                .asDoubleStream().boxed().parallel()


        return context.copy(evaluationResult = Eval.Sequence(newSequence))
    }

    override fun onNumberLiteralNodeVisited(
        numberLiteralNode: NumberLiteralNode,
        context: ExecutionContext
    ): ExecutionContext {
        return context.copy(evaluationResult = Eval.Scalar(numberLiteralNode.value))
    }
}