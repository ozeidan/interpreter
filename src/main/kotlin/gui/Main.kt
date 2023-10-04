package gui

import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory
import java.awt.EventQueue

fun main() {
    val atmf = TokenMakerFactory.getDefaultInstance() as AbstractTokenMakerFactory
    atmf.putMapping("text/seqLang", "gui.SeqLangTokenMaker")

    EventQueue.invokeLater {
        RootPane().isVisible = true
    }
}
