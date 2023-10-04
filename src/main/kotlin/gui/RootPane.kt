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

        menuBar.add(fileMenu)
        fileMenu.add(openFileItem)

        saveFileItem.isEnabled = false
        fileMenu.add(saveFileItem)
        fileMenu.add(saveFileAsItem)
        return menuBar
    }
}
private fun setLookAndFeelIfExists(className: String, rootPane: Editor) {
    if (!Arrays.stream(UIManager.getInstalledLookAndFeels()).anyMatch { it.name.equals(className) }) {
        return
    }

    UIManager.setLookAndFeel(className);
    SwingUtilities.updateComponentTreeUI(rootPane);
}
