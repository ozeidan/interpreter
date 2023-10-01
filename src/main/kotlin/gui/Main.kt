package gui

import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory
import java.awt.EventQueue
import java.util.*
import javax.swing.*


fun main() {
    val atmf = TokenMakerFactory.getDefaultInstance() as AbstractTokenMakerFactory
    atmf.putMapping("text/seqLang", "gui.SeqLangTokenMaker")

    EventQueue.invokeLater {
        Main().isVisible = true
    }
}

class Main : JFrame() {
    private val editor = Editor()

    init  {
        contentPane = editor
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
        setTitle("SeqLand IDE")
        setSize(800, 600)
        jMenuBar = createMenuBar()
    }

    private fun createMenuBar() : JMenuBar {
        val menuBar = JMenuBar()
        val fileMenu = JMenu("File")
        val openFileItem = JMenuItem("Open File...")

        openFileItem.addActionListener {
            val fileChooser = JFileChooser(System.getProperty("user.dir"))
            if(fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                editor.loadFile(fileChooser.selectedFile)
            }
        }

        menuBar.add(fileMenu)
        fileMenu.add(openFileItem)
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
