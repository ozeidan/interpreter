package interpreter.ast

import SeqLangBaseVisitor

/**
Class that constructs an AST of type ASTNode from a SeqLang parse tree. The AST is structurally more suited for
semantic analysis and execution of the program, as it directly represents the semantics of the program without
storing unneeded intermediate nodes which are artefacts of parsing.
*/
class ASTConstructor {
    private val programNodeConstructingVisitor = ProgramNodeConstructingVisitor()

    /**
     * Constructs a SeqLang AST.
     *
     * @param ctx: SeqLang parse tree of a program, generated from the ANTLR-generated parser.
     * @return SeqLang AST of the program.
     */
    fun constructAST(ctx: SeqLangParser.ProgramContext): ProgramNode {
        return programNodeConstructingVisitor.visitProgram(ctx)
    }
}


// The visitor's implementation is split into multiple classes, each corresponding to a concrete AST node type.
// This is done to avoid casting.
private class ProgramNodeConstructingVisitor : SeqLangBaseVisitor<ASTNode>()  {
    private val statementNodeConstructingVisitor = StatementNodeConstructingVisitor()

    override fun visitProgram(ctx: SeqLangParser.ProgramContext): ProgramNode {
        return ProgramNode(ctx.stmt().map(statementNodeConstructingVisitor::visit))
    }
}

private class StatementNodeConstructingVisitor : SeqLangBaseVisitor<ASTNode.Statement>() {
    private val expressionConstructor = ExpressionNodeConstructingVisitor()

    override fun visitVarDeclaration(ctx: SeqLangParser.VarDeclarationContext): ASTNode.Statement {
        return VariableDeclarationNode(ctx.start.line, ctx.IDENT().text, expressionConstructor.visit(ctx.expr()))
    }

    override fun visitPrintExpression(ctx: SeqLangParser.PrintExpressionContext): ASTNode.Statement {
        return PrintExpressionNode(ctx.start.line, expressionConstructor.visit(ctx.expr()))
    }

    override fun visitPrintString(ctx: SeqLangParser.PrintStringContext): ASTNode.Statement {
        val text = ctx.STRING().text
        val trimmedText = text.substring(1, text.length - 1)
        return PrintStringNode(ctx.start.line, trimmedText)
    }
}

private class ExpressionNodeConstructingVisitor : SeqLangBaseVisitor<ASTNode.Expression>() {
    override fun visitAddition(ctx: SeqLangParser.AdditionContext): ASTNode.Expression {
        return BinOpNode(BinaryOperator.ADDITION, this.visit(ctx.addExpr()), this.visit(ctx.multExpr()))
    }

    override fun visitSubtraction(ctx: SeqLangParser.SubtractionContext): ASTNode.Expression {
        return BinOpNode(BinaryOperator.SUBTRACTION, this.visit(ctx.addExpr()), this.visit(ctx.multExpr()))
    }

    override fun visitMultiplication(ctx: SeqLangParser.MultiplicationContext): ASTNode.Expression {
        return BinOpNode(BinaryOperator.MULTIPLICATION, this.visit(ctx.multExpr()), this.visit(ctx.powerExpr()))
    }

    override fun visitDivision(ctx: SeqLangParser.DivisionContext): ASTNode.Expression {
        return BinOpNode(BinaryOperator.DIVISION, this.visit(ctx.multExpr()), this.visit(ctx.powerExpr()))
    }

    override fun visitPower(ctx: SeqLangParser.PowerContext): ASTNode.Expression {
        return BinOpNode(BinaryOperator.POWER, this.visit(ctx.baseExpr()), this.visit(ctx.powerExpr()))
    }

    override fun visitParenthesizedExpr(ctx: SeqLangParser.ParenthesizedExprContext): ASTNode.Expression {
        return this.visit(ctx.expr())
    }

    override fun visitIdentifier(ctx: SeqLangParser.IdentifierContext): ASTNode.Expression {
        return VariableAccessNode(ctx.IDENT().text)
    }

    override fun visitSequence(ctx: SeqLangParser.SequenceContext): ASTNode.Expression {
        return SequenceNode(this.visit(ctx.expr(0)), this.visit(ctx.expr(1)))
    }

    override fun visitFloatLiteral(ctx: SeqLangParser.FloatLiteralContext): ASTNode.Expression {
        return FloatLiteralNode(ctx.FLOAT().text.toDouble())
    }

    override fun visitIntegerLiteral(ctx: SeqLangParser.IntegerLiteralContext): ASTNode.Expression {
        return IntegerLiteralNode(ctx.INTEGER().text.toInt())
    }

    override fun visitMapping(ctx: SeqLangParser.MappingContext): ASTNode.Expression {
        return MappingNode(this.visit(ctx.expr(0)), UnaryLambda(ctx.IDENT().text, this.visit(ctx.expr(1))))
    }

    override fun visitReduction(ctx: SeqLangParser.ReductionContext): ASTNode.Expression {
        return ReducingNode(this.visit(ctx.expr(0)), this.visit(ctx.expr(1)),
            BinaryLambda(ctx.IDENT(0).text, ctx.IDENT(1).text, this.visit(ctx.expr(2))))
    }
}