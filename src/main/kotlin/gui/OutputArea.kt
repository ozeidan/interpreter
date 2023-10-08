package gui

import java.awt.*
import javax.swing.*

class OutputArea : JPanel() {
    private val outputTextArea = JTextArea()
    init {
        this.layout = BoxLayout(this, BoxLayout.Y_AXIS)

        val programOutputLabel = Label("Program Output:", Label.LEFT)
        programOutputLabel.font = Font("Arial", Font.BOLD, 13)
        programOutputLabel.maximumSize = Dimension(Int.MAX_VALUE, 15)

        val outputScrollPane = JScrollPane(outputTextArea)
        outputTextArea.isEditable = false
        outputTextArea.font = Font("Monospaced", Font.PLAIN, 14);


        add(programOutputLabel)
        add(outputScrollPane)
    }


    fun setExecuting() {
        outputTextArea.isEnabled = false
    }

    fun setOutput(output: String) {
        outputTextArea.isEnabled = true
        outputTextArea.text = output
    }

    fun setError(errorMessage: String) {
        outputTextArea.text = errorMessage
        outputTextArea.isEnabled = true
    }
}