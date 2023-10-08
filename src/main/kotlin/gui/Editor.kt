package gui

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea
import org.fife.ui.rsyntaxtextarea.RSyntaxTextAreaEditorKit.DecreaseFontSizeAction
import org.fife.ui.rsyntaxtextarea.RSyntaxTextAreaEditorKit.IncreaseFontSizeAction
import org.fife.ui.rtextarea.RTextScrollPane
import java.awt.*
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.io.File
import java.io.FileReader
import javax.swing.*



class Editor : JPanel() {
    private val editorArea: RSyntaxTextArea = createTextArea()
    private val outputArea = OutputArea()

    private var interpretationWorker : InterpretationWorker? = null

    init {
        layout = BorderLayout()
        editorArea.syntaxEditingStyle = "text/seqLang"
        editorArea.addKeyListener(InterpretationKeyListener(this))
        editorArea.animateBracketMatching = false

        val editorScrollPane = RTextScrollPane(editorArea, true)
        outputArea.minimumSize = Dimension(20, 100)
        editorScrollPane.minimumSize = Dimension(20, 100)

        val verticalSplit = JSplitPane(JSplitPane.VERTICAL_SPLIT, editorScrollPane, outputArea)
        verticalSplit.resizeWeight = 1.0
        add(verticalSplit)
    }


    private fun createTextArea(): RSyntaxTextArea {
        val textArea = RSyntaxTextArea(10, 80)
        textArea.requestFocusInWindow()
        textArea.markOccurrences = true

        fun addHotkey(keyStroke: KeyStroke, action: Action, actionMapKey: String) {
            textArea.inputMap.put(keyStroke, actionMapKey)
            textArea.actionMap.put(actionMapKey, action)
        }

        addHotkey(KeyStroke.getKeyStroke(
            KeyEvent.VK_MINUS, InputEvent.CTRL_DOWN_MASK),
            DecreaseFontSizeAction(),
            "decreaseFontSize")
        addHotkey(KeyStroke.getKeyStroke(
            KeyEvent.VK_PLUS, InputEvent.CTRL_DOWN_MASK),
            IncreaseFontSizeAction(),
            "increaseFontSize")
        addHotkey(KeyStroke.getKeyStroke(
            KeyEvent.VK_EQUALS, InputEvent.CTRL_DOWN_MASK or InputEvent.SHIFT_DOWN_MASK),
            IncreaseFontSizeAction(),
            "increaseFontSize")

        return textArea
    }

    fun loadFile(file: File) {
        editorArea.read(FileReader(file), null)
        editorArea.setCaretPosition(0)
        editorArea.discardAllEdits()
        editorArea.clearVirtualTexts()
        executeProgramAndShowOutput(editorArea.text)
    }

    fun getText() : String {
        return editorArea.text
    }

    private fun executeProgramAndShowOutput(program: String) {
        val newWorker = InterpretationWorker(program) { result ->
            when (result) {
                is InterpretationResult.Success -> {
                    outputArea.setOutput(result.output)
                    editorArea.clearVirtualTexts()
                }

                is InterpretationResult.Error -> {
                    outputArea.setError(result.error.toString())
                    editorArea.virtualTexts =
                        mapOf((result.error.lineNumber - 1 to result.error.message))
                }
            }
        }

        interpretationWorker?.cancel(true)
        interpretationWorker = newWorker
        newWorker.execute()
        editorArea.clearVirtualTexts()
        outputArea.setExecuting()
    }

    private class InterpretationKeyListener(val editor: Editor) : KeyListener {
        private val interpretationDebouncer = Debouncer(500)

        override fun keyTyped(e: KeyEvent) {
            interpretationDebouncer.debounce { editor.executeProgramAndShowOutput(editor.editorArea.text) }
        }

        override fun keyPressed(e: KeyEvent?) {
        }

        override fun keyReleased(e: KeyEvent?) {
        }
    }
}
