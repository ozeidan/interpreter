package interpreter

import interpreter.ast.*
import java.io.Writer
import java.util.*
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.RecursiveTask
import java.util.stream.Collectors
import java.util.stream.LongStream
import java.util.stream.Stream
import kotlin.math.pow

class Executor(private val writer: Writer, private val forkJoinPool: ForkJoinPool) {
    private val executingASTVisitor = ExecutingASTVisitor()

    fun execute(ast: ASTNode, scope: Map<String, Value> = mapOf()) : Map<String, Value> {
         return forkJoinPool.invoke(object : RecursiveTask<Map<String, Value>>() {
            override fun compute() : Map<String, Value> {
                return executingASTVisitor.visit(ast, ExecutionContext(scope, writer)).scope
            }
        })
    }
}

sealed class Value {
    abstract fun binaryOperation(operator : BinaryOperator, other: Value) : Value
    abstract fun print() : String

    data class Float(val value : Double) : Value() {
        override fun binaryOperation(operator: BinaryOperator, other: Value): Value {
            val otherValue = when (other) {
                is Float -> other.value
                is Integer -> other.value.toDouble()
                else -> {
                    throw Exception()
                }
            }

            return Float(when (operator) {
                BinaryOperator.ADDITION -> value + otherValue
                BinaryOperator.SUBTRACTION -> value - otherValue
                BinaryOperator.MULTIPLICATION -> value * otherValue
                BinaryOperator.DIVISION -> {
                    if (otherValue == 0.0) {
                        throw InterpreterException("attempted division by 0")
                    }
                    value / otherValue
                }
                BinaryOperator.POWER -> value.pow(otherValue)
            })
        }

        override fun print(): String {
            return value.toString()
        }

    }
    data class Integer(val value : Long) : Value() {
        override fun binaryOperation(operator: BinaryOperator, other: Value): Value {
            if (other is Float) {
                return Float(value.toDouble()).binaryOperation(operator, other)
            }

            require(other is Integer)

            val otherValue = other.value
            return when (operator) {
                BinaryOperator.ADDITION -> Integer(value + otherValue)
                BinaryOperator.SUBTRACTION -> Integer(value - otherValue)
                BinaryOperator.MULTIPLICATION -> Integer(value * otherValue)
                BinaryOperator.DIVISION -> {
                    if (otherValue == 0L) {
                        throw InterpreterException("attempted division by 0")
                    }
                    Float(value / otherValue.toDouble())
                }
                BinaryOperator.POWER -> Integer(value.toDouble().pow(other.value.toDouble()).toLong()) // TODO: way to avoid conversion?
            }
        }

        override fun print(): String {
            return value.toString()
        }
    }
    data class Sequence(
        val lowerInclusive: Long,
        val upperInclusive: Long,
        val mappedFunctions : List<(Value) -> Value>)
    : Value() {
        override fun binaryOperation(operator: BinaryOperator, other: Value): Value {
            throw Exception()
        }

        override fun print(): String {
            val elementStrings = buildStream().limit(10).map { it.print() }

            val allStrings = if (upperInclusive - lowerInclusive > 10)
                Stream.concat(elementStrings, Stream.of("..."))
            else elementStrings

            return allStrings.collect(Collectors.joining(", ", "{ ", " }"))
        }

        fun buildStream(): Stream<Value> {
            val baseStream : Stream<Value> =
                LongStream.rangeClosed(lowerInclusive, upperInclusive).boxed().map { Integer(it) }

            val mappedStream = mappedFunctions.fold(baseStream) { stream, mappedFunction ->
                stream.map { mappedFunction.invoke(it) }
            }

            return mappedStream.parallel()
        }
    }
}

private data class ExecutionContext(
    val scope : Map<String, Value>,
    val writer: Writer
)

private class ExecutingASTVisitor : ASTVisitor<ExecutionContext, Value>() {
    private fun checkInterrupted() {
        if (Thread.currentThread().isInterrupted) {
            Thread.currentThread().interrupt()
            throw InterruptedException()
        }
    }

    override fun onVariableDeclarationNodeVisited(
        variableDeclarationNode: VariableDeclarationNode,
        context: ExecutionContext
    ): ExecutionContext {
        return addLineNumberToExceptions(variableDeclarationNode) {
            val evaluationResult = visit(variableDeclarationNode.expression, context).second
            context.copy(scope = context.scope + (variableDeclarationNode.identifier to evaluationResult))
        }
    }

    override fun onVariableAccessNodeVisited(
        variableAccessNode: VariableAccessNode,
        context: ExecutionContext
    ): Pair<ExecutionContext, Value> {
        val value = Optional
            .ofNullable(context.scope[variableAccessNode.identifier])
            .orElseThrow { Exception("access of undeclared variable") }

        return Pair(context, value)
    }

    override fun onPrintExpressionNodeVisited(
        printExpressionNode: PrintExpressionNode,
        context: ExecutionContext
    ): ExecutionContext {
        return addLineNumberToExceptions(printExpressionNode) {
            context.writer.write(visit(printExpressionNode.expression, context).second.print())
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

    override fun onBinOpNodeVisited(binOpNode: BinOpNode, context: ExecutionContext): Pair<ExecutionContext, Value> {
        val leftEvaluationResult = visit(binOpNode.left, context).second
        val rightEvaluationResult = visit(binOpNode.right, context).second

        require(leftEvaluationResult !is Value.Sequence && rightEvaluationResult !is Value.Sequence)

        val evaluationResult = leftEvaluationResult.binaryOperation(binOpNode.operator, rightEvaluationResult)
        return Pair(context, evaluationResult)
    }

    override fun onMappingNodeVisited(mappingNode: MappingNode, context: ExecutionContext): Pair<ExecutionContext, Value> {
        val sequenceEvaluationResult = visit(mappingNode.sequenceExpression, context).second

        require(sequenceEvaluationResult is Value.Sequence)

        val evaluationFun = {
            sequenceValue : Value ->
            checkInterrupted()
            visit(
                mappingNode.lambda.expression,
                context.copy(scope = mapOf(mappingNode.lambda.identifier to sequenceValue))
            ).second
        }

        val newSequence = sequenceEvaluationResult.copy(
            mappedFunctions = sequenceEvaluationResult.mappedFunctions + evaluationFun
        )

        return Pair(context, newSequence)
    }

    override fun onReducingNodeVisited(reducingNode: ReducingNode, context: ExecutionContext): Pair<ExecutionContext, Value> {
        val sequenceEvaluationResult = visit(reducingNode.sequenceExpression, context).second
        val neutralElementEvaluationResult = visit(reducingNode.neutralElementExpression, context).second

        require(sequenceEvaluationResult is Value.Sequence)
        require(neutralElementEvaluationResult !is Value.Sequence)

        val evaluationResult = sequenceEvaluationResult.buildStream().reduce(neutralElementEvaluationResult) { x, y ->
            checkInterrupted()
            visit(
                reducingNode.lambda.expression,
                context.copy(
                    scope = mapOf(
                        (reducingNode.lambda.leftIdentifier to x),
                        (reducingNode.lambda.rightIdentifier to y),
                    )
                )
            ).second
        }

        return Pair(context, evaluationResult)
    }

    override fun onSequenceNodeVisited(sequenceNode: SequenceNode, context: ExecutionContext): Pair<ExecutionContext, Value> {
        val lowerEvaluationResult = visit(sequenceNode.lowerBoundInclusive, context).second
        val upperEvaluationResult = visit(sequenceNode.upperBoundInclusive, context).second

        require(lowerEvaluationResult is Value.Integer && upperEvaluationResult is Value.Integer)

        if (upperEvaluationResult.value < lowerEvaluationResult.value) {
            throw InterpreterException("lower sequence boundary is higher than upper boundary")
        }

        return Pair(context,
            Value.Sequence(
                lowerEvaluationResult.value,
                upperEvaluationResult.value,
                listOf()
        ))
    }

    override fun onIntegerLiteralNodeVisited(
        integerLiteralNode: IntegerLiteralNode,
        context: ExecutionContext
    ): Pair<ExecutionContext, Value> {
        return Pair(context, Value.Integer(integerLiteralNode.value))
    }

    override fun onFloatLiteralNodeVisited(
        floatLiteralNode: FloatLiteralNode,
        context: ExecutionContext
    ): Pair<ExecutionContext, Value> {
        return Pair(context, Value.Float(floatLiteralNode.value))
    }
}