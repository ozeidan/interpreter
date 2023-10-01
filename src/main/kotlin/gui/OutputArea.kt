package gui

import java.awt.Font
import javax.swing.JTextArea

class OutputArea : JTextArea() {
    init {
        this.isEditable = false
        this.font = Font("Monospaced", Font.PLAIN, 14);
    }
}