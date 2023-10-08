package gui

import java.awt.event.ActionEvent
import java.awt.event.KeyEvent
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.*
import javax.swing.*

class RootPane : JFrame() {
    private val editor = Editor()
    private var openFile : File? = null

    private val saveFileItem = JMenuItem("Save")

    init {
        contentPane = editor
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
        setTitle("SeqLand IDE")
        setSize(800, 600)
        jMenuBar = createMenuBar()
    }

    private fun saveFile(fileName: File) {
        try {
            val writer = BufferedWriter(FileWriter(fileName))
            writer.write(editor.getText())
            writer.close()
        } catch(ignored: IOException) {
        }
    }

    private fun setOpenFile(file: File) {
        openFile = file
        setTitle("SeqLand IDE [${file.name}]")
        saveFileItem.isEnabled = true
    }

    private fun createMenuBar() : JMenuBar {
        val menuBar = JMenuBar()
        menuBar.add(createFileMenu())
        menuBar.add(createInterpreterMenu())
        return menuBar
    }

    private fun createFileMenu() : JMenu {
        val fileMenu = JMenu("File")
        val openFileItem = JMenuItem("Open...")
        val saveFileAsItem = JMenuItem("Save As...")

        saveFileItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        openFileItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
        saveFileAsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK or ActionEvent.SHIFT_MASK));

        openFileItem.addActionListener {
            val fileChooser = JFileChooser(System.getProperty("user.dir"))
            if(fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                editor.loadFile(fileChooser.selectedFile)
                setOpenFile(fileChooser.selectedFile)
            }
        }

        saveFileItem.addActionListener {
            saveFile(openFile!!)
        }

        saveFileAsItem.addActionListener {
            val fileChooser = JFileChooser(System.getProperty("user.dir"))
            if(fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                saveFile(fileChooser.selectedFile)
                setOpenFile(fileChooser.selectedFile)
            }
        }

        fileMenu.add(openFileItem)

        saveFileItem.isEnabled = false
        fileMenu.add(saveFileItem)
        fileMenu.add(saveFileAsItem)
        return fileMenu
    }

    private fun createInterpreterMenu() : JMenu {
        val interpreterMenu = JMenu("Interpreter")
        val executeCodeItem = JMenuItem("Execute Code")
        val automaticExecutionItem = JCheckBoxMenuItem("Automatic Execution")
        automaticExecutionItem.state = true

        executeCodeItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));
        automaticExecutionItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E,
            ActionEvent.CTRL_MASK or ActionEvent.SHIFT_MASK));

        executeCodeItem.addActionListener {
            editor.execute()
        }

        automaticExecutionItem.addActionListener {
            editor.executeAutomatically = automaticExecutionItem.state
        }

        interpreterMenu.add(executeCodeItem)
        interpreterMenu.add(automaticExecutionItem)

        return interpreterMenu
    }
}
