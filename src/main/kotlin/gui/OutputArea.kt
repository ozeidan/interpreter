package gui

import java.awt.*
import javax.swing.*

class OutputArea : JPanel() {
    private val outputTextArea = JTextArea()
    private val infoLabel = Label("(executing)")
    init {
        infoLabel.isVisible = false
        this.layout = BoxLayout(this, BoxLayout.Y_AXIS)

        val outputScrollPane = JScrollPane(outputTextArea)
        outputTextArea.isEditable = false
        outputTextArea.font = Font("Monospaced", Font.PLAIN, 14);

        val headerPanel = JPanel()
        headerPanel.layout = BoxLayout(headerPanel, BoxLayout.X_AXIS)

        val programOutputLabel = Label("Program Output:", Label.LEFT)
        programOutputLabel.font = Font("Arial", Font.BOLD, 13)
        programOutputLabel.maximumSize = Dimension(80, Int.MAX_VALUE)
        infoLabel.maximumSize = Dimension(80, Int.MAX_VALUE)

        headerPanel.add(programOutputLabel)
        headerPanel.add(infoLabel)
        headerPanel.add(Box.createHorizontalGlue())

        headerPanel.maximumSize = Dimension(Int.MAX_VALUE, 15)
        headerPanel.border = null

        add(headerPanel)
        add(outputScrollPane)
    }


    fun setExecuting() {
        outputTextArea.isEnabled = false
        infoLabel.isVisible = true
        infoLabel.text = "(executing)"
        revalidate()
    }

    fun setOutput(output: String) {
        outputTextArea.text = output
        outputTextArea.isEnabled = true
        infoLabel.isVisible = false
        revalidate()
    }

    fun setError() {
        outputTextArea.isEnabled = true
        infoLabel.isVisible = true
        infoLabel.text = "(outdated)"
        revalidate()
    }
}