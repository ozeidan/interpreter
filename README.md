# Interpreter

This repository implements an interpreter and IDE for the language described by the following grammer in pseudo-BNF: 

```
expr ::= expr op expr | (expr) | ident | { expr, expr } |
         number | map(expr, ident -> expr) |
         reduce(expr, expr, ident ident -> expr)
op ::= + | - | * | / | ^
stmt ::= var ident = expr | out expr | print "string"
program ::= stmt | program stmt
```

## Outline

The repository will be structured as follows:
* Interpreter (20 estimated hours)

    The various stages of interpretation handle and report errors appropriately, including the position in the input where the error occured.
    * Lexing and parsing
        
    * Semantic Analysis
        
        The AST is traversed before execution to catch some early type errors, for example operations with incorrect operand types
    * Interpretation

        The given statments are executed and results are printed. The interpreter has the following properties:

        * For testing purposes, the interpreter accepts a `java.io.Writer` and writes the output of the interpret program to it.
        * The interpreter lazily evaluates sequences. That means that creating or mapping a sequence does not immediately compute the values of the new sequence, but instead that happens once the values are needed: either when the sequence is printed via the `out` statement or when the sequence is reduced to a scalar value. Consecutive mappings on a sequence only construct a computational pipeline. This allows for sequences with a very large number of entries, since the interpreter doesn't need to hold all of the entries in memory, but can sequentially compute them as they are printed or combined in a reduction operation.
        * The eventual computation of a sequence's entries is conducted in parallel. Cancellation is supported to enable fast consecutive executions of programs as the user edits the code.

* IDE (6 estimated hours)

    The IDE allows the user to edit code of the language described above. After very change that the user makes, the IDE automatically interprets the code, displays the output in the output window and displays errors in the code editor if any occur.
    * Code Editor
        * Syntax highting
        * Displaying of errors in place, next to the lines of code that caused them
    * Output Window
        * Displays the output of the script that's currently open in the text buffer

## Utilized Libraries
TBD
The implementation of the lexer/parser will most likely be generated using ANTLR
I have not decided on which GUI framework to use yet. I will look into importing a text editor component that supports virtual text for the error messages.
