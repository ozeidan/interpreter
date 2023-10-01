grammar SeqLang;

// Parser Rules

program: stmt (stmt)* EOF ;

stmt
    : 'var' IDENT '=' expr   # VarDeclaration
    | 'out' expr             # PrintExpression
    | 'print' STRING         # PrintString
    ;

expr: addExpr;

addExpr
    : addExpr '+' multExpr   # Addition
    | addExpr '-' multExpr   # Subtraction
    | multExpr               # ToMultExpr
    ;

multExpr
    : multExpr '*' powerExpr  # Multiplication
    | multExpr '/' powerExpr  # Division
    | powerExpr               # ToPowerExpr
    ;

powerExpr
    : baseExpr '^' powerExpr  # Power
    | baseExpr                # ToBaseExpr
    ;

baseExpr
    : '(' expr ')'            # ParenthesizedExpr
    | IDENT                   # Identifier
    | '{' expr ',' expr '}'   # Sequence
    | INTEGER                 # IntegerLiteral
    | FLOAT                   # FloatLiteral
    | 'map' '(' expr ',' IDENT '->' expr ')' # Mapping
    | 'reduce' '(' expr ',' expr ',' IDENT IDENT '->' expr ')' # Reduction
    ;

// Lexer Rules

IDENT: [a-zA-Z]+ ;
INTEGER: '-'?[0-9]+ ;
FLOAT: '-'?[0-9]+ ('.' [0-9]+)? ;
STRING: '"' ~["\r\n]* '"' ;
UNTERMINATED_STRING :  '"' ('\\' ["\\] | ~["\\\r\n])* ;
WS: [ \t\r\n]+ -> channel(HIDDEN);
