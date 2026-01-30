package simulation

import gui.ResultsAnalyzerFrame
import simulation.modelexe.runpanel.*
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

    private val editorTabs = JTabbedPane()

    init {
        defaultCloseOperation = EXIT_ON_CLOSE
        minimumSize = Dimension(1200, 750)
        setLocationRelativeTo(null)

        contentPane.layout = BorderLayout(8, 8)
        (contentPane as JComponent).border = EmptyBorder(8, 8, 8, 8)

        contentPane.add(buildHeader(), BorderLayout.NORTH)

        // --- Run Service (basic demo model for now) ---
        val runService = KslRepLoopRunService {
            SimpleKslDemoModel()
        }

        editorTabs.addTab(
            "Run Control",
            RunPanel(runService, consoleArea, errorArea)
        )

        contentPane.add(
            JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                editorTabs,
                buildConsoleTabs()
            ).apply {
                resizeWeight = 0.75
                dividerSize = 6
            },
            BorderLayout.CENTER
        )
    }

    private fun buildConsoleTabs(): JTabbedPane =
        JTabbedPane().apply {
            preferredSize = Dimension(0, 200)
            addTab("Console", JScrollPane(consoleArea))
            addTab("Error", JScrollPane(errorArea))
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

        val attachDbBtn = JButton("Attach DB...")
        infoBar.add(attachDbBtn)

        val menuRow = JPanel(FlowLayout(FlowLayout.LEFT, 8, 4))

        val fileBtn = JButton("File")
        val editBtn = JButton("Edit")
        val optionsBtn = JButton("Options")
        val helpBtn = JButton("Help")

        val inputAnalyzerBtn = JButton("Input Analyzer").apply {
            addActionListener {
                SwingUtilities.invokeLater {
                    DistributionModeler().isVisible = true
                }
            }
        }

        val outputAnalyzerBtn = JButton("Output Analyzer").apply {
            addActionListener {
                SwingUtilities.invokeLater {
                    ResultsAnalyzerFrame().isVisible = true
                }
            }
        }

        listOf(
            fileBtn,
            editBtn,
            optionsBtn,
            helpBtn,
            inputAnalyzerBtn,
            outputAnalyzerBtn
        ).forEach { menuRow.add(it) }

        header.add(infoBar)
        header.add(menuRow)
        return header
    }
}
