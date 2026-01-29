package simulation

import simulation.modelexe.runpanel.KslReflectiveRunService
import simulation.modelexe.runpanel.RunPanel
import java.awt.*
import javax.swing.*
import javax.swing.border.EmptyBorder

fun main() {
    SwingUtilities.invokeLater { ModelFrame().isVisible = true }
}

class ModelFrame : JFrame("Model Executor") {

    private val consoleArea = JTextArea().apply {
        isEditable = false
        font = Font(Font.MONOSPACED, Font.PLAIN, 12)
    }

    private val errorArea = JTextArea().apply {
        isEditable = false
        font = Font(Font.MONOSPACED, Font.PLAIN, 12)
    }

    init {
        defaultCloseOperation = EXIT_ON_CLOSE
        minimumSize = Dimension(1200, 720)
        setLocationRelativeTo(null)

        contentPane.layout = BorderLayout(8, 8)
        (contentPane as JComponent).border = EmptyBorder(8, 8, 8, 8)

        contentPane.add(buildHeader(), BorderLayout.NORTH)

        //  Hook your model here
        val runService = KslReflectiveRunService(
            buildModel = {
                // TODO: replace this with actual model class construction.
                // Example:
                // return Distribution_Modeler()
                throw IllegalStateException("Hook buildModel() to return your KSL model instance.")
            }
        )

        val editorTabs = JTabbedPane().apply {
            addTab("Run Control", RunPanel(runService, consoleArea, errorArea))
            addTab("Blank", JPanel())
        }

        val consoleTabs = JTabbedPane().apply {
            preferredSize = Dimension(0, 190)
            addTab("Console", JScrollPane(consoleArea))
            addTab("Error", JScrollPane(errorArea))
        }

        contentPane.add(
            JSplitPane(JSplitPane.VERTICAL_SPLIT, editorTabs, consoleTabs).apply {
                resizeWeight = 0.75
                dividerSize = 6
            },
            BorderLayout.CENTER
        )
    }

    private fun buildHeader(): JComponent {
        val header = JPanel()
        header.layout = BoxLayout(header, BoxLayout.Y_AXIS)

        val infoBar = JPanel(FlowLayout(FlowLayout.LEFT, 8, 4))
        listOf("Model:", "Label:", "Experiment:", "Time Unit:", "Default Stream:").forEach {
            infoBar.add(JLabel(it))
            infoBar.add(JTextField(10))
        }
        infoBar.add(Box.createHorizontalGlue())
        infoBar.add(JButton("Attach DB..."))

        val menuRow = JPanel(FlowLayout(FlowLayout.LEFT, 8, 4))
        listOf("File", "Edit", "Options", "Help", "Input analyzer", "Output analyzer").forEach {
            menuRow.add(JButton(it))
        }

        header.add(infoBar)
        header.add(menuRow)
        return header
    }
}
