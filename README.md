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

Here is an example program:
```
var n = 500
var seq = map({0, n}, i -> (-1)^i / (2 * i + 1))
var pi = 4 * reduce(seq, 0, x y -> x + y)
print "pi = "
out pi
```
The evaluation of sequences happens lazily, which allows for computations on very long sequences without running out of memory. The computations always happen in parallel, utilising all availables CPU cores.

The IDE has basic editor features (syntax highlighting, highlighting of matching parantheses, etc.) and shows in-line error messages. The entered program always gets executed shortly after the user has finished entering it and the ouput gets shown in the panel below the editor. The program is also type-checked before execution which catches all type errors without having to run potentially long operations, enabling a faster feedback loop.

## Running

Currently I'm running the Main files from IntelliJ, there's one for the GUI and one for a REPL. Gradle tasks coming soon.

## Usage

Create a `Interpreter` instance and call `interpret` on it, passing your code as an argument. The output gets printed to stdout by default, or to a `java.io.Writer`, if one was passed to `Interpreter`s constructor. It's also possible to pass a custom `ForkJoinPool`, to limit the number of utilised CPU cores or to cancel a running computation (by calling `shutdownNow` on `ForkJoinPool`).

## Utilised Libraries

The lexer and parser were generated using [ANTLR](https://www.antlr.org/).
The GUI is built with Java Swing. The editor is basd on a fork of [RSyntaxTextArea](https://github.com/bobbylight/RSyntaxTextArea) I created that support virtual text for showing in-line error messages. The fork is included in this repo, packaged as a .jar file which gets included through gradle. I will upload the fork to Github soon but don't plan to get the changes merged as they are rather hacky and don't account for a bunch of editor features this project doesn't use.

## Technical Overview

ANTLR generates a lexer and parser, using the grammar in `src/main/antlr/SeqLang.g4`. At runtime, the `ASTConstructor` then traverses the parse tree created by the ANTLR parser, creating an AST (`ASTNode`). The AST can be easily traversed by subclassing `ASTVisitor`. This is done by the `SemanticAnalyzer` and the `Executor` to type check and finally execute the program at hand.

# Outdated:
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
