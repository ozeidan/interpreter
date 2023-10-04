# Interpreter

This repository implements an interpreter and IDE for the language described by the following grammar in pseudo-BNF: 

```
expr ::= expr op expr | (expr) | ident | { expr, expr } |
         number | map(expr, ident -> expr) |
         reduce(expr, expr, ident ident -> expr)
op ::= + | - | * | / | ^
stmt ::= var ident = expr | out expr | print "string"
program ::= stmt | program stmt
```

Here is an example program:
```
var n = 500
var seq = map({0, n}, i -> (-1)^i / (2 * i + 1))
var pi = 4 * reduce(seq, 0, x y -> x + y)
print "pi = "
out pi
```
The evaluation of sequences is lazy, allowing computations on very long sequences without running out of memory. Computations occur in parallel, utilizing all available CPU cores.

The IDE has basic editor features (syntax highlighting, matching parentheses highlighting, etc.) and shows inline error messages. The entered program executes shortly after the user finishes input, and the output appears below the editor. The program is also type-checked before execution, enabling a faster feedback loop.

## Running

Currently, I run the Main files from IntelliJ; one is for the GUI and another for a REPL. Gradle tasks are coming soon.

## Usage

Create an Interpreter instance and call interpret on it, passing your code as an argument. Output prints to stdout by default, or to a `java.io.Writer` if one was passed to the Interpreter constructor. You can also pass a custom `ForkJoinPool `to limit CPU core usage or to cancel a running computation (by calling shutdownNow on `ForkJoinPool`).

## Utilised Libraries

The lexer and parser are generated using [ANTLR](https://www.antlr.org/). The GUI uses Java Swing. The editor is based on my fork of [RSyntaxTextArea](https://github.com/bobbylight/RSyntaxTextArea) that supports inline error messages. This fork is included in this repo and is packaged as a .jar file through Gradle. I plan to upload the fork to GitHub soon but don't plan on merging the changes, as they are rather hacky.

## Technical Overview

ANTLR generates a lexer and parser, using the grammar in `src/main/antlr/SeqLang.g4`. At runtime, the `ASTConstructor` then traverses the parse tree created by the ANTLR parser, creating an AST (`ASTNode`). The AST can be easily traversed by subclassing `ASTVisitor`. This is done by the `SemanticAnalyzer` and the `Executor` to type check and finally execute the program at hand.

# Outdated:
## Outline

The repository will be structured as follows:
* Interpreter (20 estimated hours)

    The various stages of interpretation handle and report errors appropriately, including the position in the input where the error occurred.
    * Lexing and parsing
        
    * Semantic Analysis
        
        The AST is traversed before execution to catch some early type errors, for example operations with incorrect operand types
    * Interpretation

        The given statements are executed and results are printed. The interpreter has the following properties:

        * For testing purposes, the interpreter accepts a `java.io.Writer` and writes the output of the interpret program to it.
        * The interpreter lazily evaluates sequences. That means that creating or mapping a sequence does not immediately compute the values of the new sequence, but instead that happens once the values are needed: either when the sequence is printed via the `out` statement or when the sequence is reduced to a scalar value. Consecutive mappings on a sequence only construct a computational pipeline. This allows for sequences with a very large number of entries, since the interpreter doesn't need to hold all of the entries in memory, but can sequentially compute them as they are printed or combined in a reduction operation.
        * The eventual computation of a sequence's entries is conducted in parallel. Cancellation is supported to enable fast consecutive executions of programs as the user edits the code.

* IDE (6 estimated hours)

    The IDE allows the user to edit code of the language described above. After very change that the user makes, the IDE automatically interprets the code, displays the output in the output window and displays errors in the code editor if any occur.
    * Code Editor
        * Syntax highlighting
        * Displaying of errors in place, next to the lines of code that caused them
    * Output Window
        * Displays the output of the script that's currently open in the text buffer

## Utilized Libraries
TBD
The implementation of the lexer/parser will most likely be generated using ANTLR
I have not decided on which GUI framework to use yet. I will look into importing a text editor component that supports virtual text for the error messages.
