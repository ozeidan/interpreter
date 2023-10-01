package gui

import javax.swing.Timer;

class Debouncer(private val debounceIntervalMs: Int) {
    private var latestRunnable : Runnable? = null

    fun debounce(runnable: Runnable) {
        latestRunnable = runnable
        // no need to synchronize because swing schedules these on the same event dispatch thread
        val timer = Timer(debounceIntervalMs) {
            if (latestRunnable == runnable) {
                runnable.run()
            }
        }
        timer.isRepeats = false
        timer.start()
    }
}