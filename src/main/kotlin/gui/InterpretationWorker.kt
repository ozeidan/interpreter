package gui

import interpreter.Interpreter
import interpreter.SeqLangException
import java.io.CharArrayWriter
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.RecursiveTask
import javax.swing.SwingWorker

sealed class InterpretationResult {
    data class Success(val output: String) : InterpretationResult()
    data class Error(val error: SeqLangException) : InterpretationResult()
}

class InterpretationWorker(
    val program: String,
    val callBack: (InterpretationResult) -> Unit
) : SwingWorker<InterpretationResult, Unit>() {
    private val forkJoinPool = ForkJoinPool(Runtime.getRuntime().availableProcessors() - 1)

    override fun doInBackground(): InterpretationResult {
        return forkJoinPool.invoke(object : RecursiveTask<InterpretationResult>() {
            override fun compute() : InterpretationResult {
                val charArrayWriter = CharArrayWriter()
                val interpreter = Interpreter(charArrayWriter)
                return try {
                    interpreter.interpret(program)
                    InterpretationResult.Success(charArrayWriter.toString())
                } catch (e: SeqLangException) {
                    InterpretationResult.Error(e)
                }
            }
        })
    }

    override fun done() {
        if (!isCancelled) {
            callBack(get())
        } else {
            forkJoinPool.shutdownNow()
        }
    }
}