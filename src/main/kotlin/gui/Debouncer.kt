package gui

import javax.swing.Timer;

/**
 * Debouncer batches calls to debounce together, running only the runnable that was passed to the latest call
 * to debounce, after the intervalsMs milliseconds have passed.
 */
class Debouncer(private val intervalMs: Int) {
    private var latestRunnable : Runnable? = null

    /**
     * Runs runnable after intervalMs milliseconds, if no further calls to debounce are made during that time.
     */
    fun debounce(runnable: Runnable) {
        latestRunnable = runnable
        // no need to synchronize because swing schedules these on the same event dispatch thread
        val timer = Timer(intervalMs) {
            if (latestRunnable == runnable) {
                runnable.run()
            }
        }
        timer.isRepeats = false
        timer.start()
    }
}