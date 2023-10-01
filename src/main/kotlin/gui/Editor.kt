package gui

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea
import org.fife.ui.rsyntaxtextarea.RSyntaxTextAreaEditorKit.DecreaseFontSizeAction
import org.fife.ui.rsyntaxtextarea.RSyntaxTextAreaEditorKit.IncreaseFontSizeAction
import org.fife.ui.rtextarea.RTextScrollPane
import java.awt.BorderLayout
import java.awt.Dimension
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

        val editorScrollPane = RTextScrollPane(editorArea, true)
        val outputScrollPane = JScrollPane(outputArea)
        outputScrollPane.minimumSize = Dimension(20, 100)
        editorScrollPane.minimumSize = Dimension(20, 100)
        val verticalSplit = JSplitPane(JSplitPane.VERTICAL_SPLIT, editorScrollPane, outputScrollPane)
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
        executeProgramAndShowResults(editorArea.text)
    }

    private fun executeProgramAndShowResults(program: String) {
        val newWorker = InterpretationWorker(program) { result ->
            when (result) {
                is InterpretationResult.Success -> {
                    outputArea.text = result.output
                    editorArea.clearVirtualTexts()
                }

                is InterpretationResult.Error -> editorArea.virtualTexts =
                    mapOf((result.lineNumber to result.error))
            }
        }

        interpretationWorker?.cancel(true)
        interpretationWorker = newWorker
        newWorker.execute()
    }

    private class InterpretationKeyListener(val editor: Editor) : KeyListener {
        private val interpretationDebouncer = Debouncer(500)

        override fun keyTyped(e: KeyEvent) {
            interpretationDebouncer.debounce { editor.executeProgramAndShowResults(editor.editorArea.text) }
        }

        override fun keyPressed(e: KeyEvent?) {
        }

        override fun keyReleased(e: KeyEvent?) {
        }
    }
}
