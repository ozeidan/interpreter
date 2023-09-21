package interpreter.ast

data class ProgramNode(val statements: List<StatementNode>) : ASTNode() {
}
