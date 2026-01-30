package simulation

import ksl.utilities.distributions.Uniform
import ksl.utilities.distributions.fitting.PDFModeler
import ksl.utilities.io.KSLFileUtil
import ksl.utilities.statistic.Statistic
import ksl.utilities.statistic.U01Test
import java.awt.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import javax.swing.*
import javax.swing.border.BevelBorder
import javax.swing.border.EmptyBorder
import javax.swing.border.TitledBorder
import javax.swing.filechooser.FileNameExtensionFilter
import simulation.InputModelStore

class DistributionModeler : JFrame("KSL Distribution Modeler") {

    private val statusArea = JTextArea("Load a data file to begin.")
    private val logArea = JTextArea("")
    private val consoleArea = JTextArea("")

    private val tabbedPane = JTabbedPane()
    private var loadedData: DoubleArray? = null
    private var loadedFile: File? = null

    private val outputDir = File("kslOutput/plotDir")

    private val runHtmlBtn = JButton("Run Distribution Modeling")
    private val runStatsBtn = JButton("Run Statistical Tests")

    private lateinit var closeResultsItem: JMenuItem

    init {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

        defaultCloseOperation = EXIT_ON_CLOSE
        minimumSize = Dimension(1150, 800)
        layout = BorderLayout()
        setLocationRelativeTo(null)

        setupMenuBar()
        add(createToolbar(), BorderLayout.NORTH)
        add(createMainLayout(), BorderLayout.CENTER)

        updateButtons()
    }

    // ───────────────────────── UI BUILD ─────────────────────────

    private fun setupMenuBar() {
        jMenuBar = JMenuBar().apply {
            add(JMenu("File").apply {
                add(JMenuItem("Load Data").apply { addActionListener { importData() } })

                closeResultsItem = JMenuItem("Close Results").apply {
                    isEnabled = false
                    addActionListener { closeResults() }
                }
                add(closeResultsItem)

                addSeparator()
                add(JMenuItem("Exit").apply { addActionListener { dispose() } })
            })
            add(JMenu("View"))
            add(JMenu("Help"))
        }
    }

    private fun createToolbar(): JToolBar =
        JToolBar().apply {
            isFloatable = false
            add(JButton("Load Data").apply { addActionListener { importData() } })
            addSeparator()
            add(runHtmlBtn.apply {
                background = Color(230, 240, 255)
                addActionListener { runHtmlModeling() }
            })
            add(runStatsBtn.apply {
                background = Color(230, 255, 230)
                addActionListener { runStatTests() }
            })
        }

    private fun createMainLayout(): JSplitPane {
        val split = JSplitPane(JSplitPane.VERTICAL_SPLIT)
        split.resizeWeight = 0.65

        val topPanel = JPanel(BorderLayout()).apply {
            border = EmptyBorder(15, 15, 15, 15)
            background = Color.WHITE
            add(JPanel(BorderLayout()).apply {
                border = BevelBorder(BevelBorder.LOWERED)
                add(tabbedPane, BorderLayout.CENTER)
            }, BorderLayout.CENTER)
        }

        val bottomPanel = JPanel(GridLayout(1, 3, 12, 0)).apply {
            border = EmptyBorder(10, 10, 10, 10)
            preferredSize = Dimension(0, 250)
            add(createBox("System Status", statusArea))
            add(createBox("Execution Log", logArea))
            add(createBox("Console Output", consoleArea))
        }

        split.topComponent = topPanel
        split.bottomComponent = bottomPanel
        return split
    }

    private fun createBox(title: String, area: JTextArea): JScrollPane =
        JScrollPane(area).apply {
            area.isEditable = false
            area.font = Font("Segoe UI", Font.PLAIN, 12)
            border = TitledBorder(title)
        }

    // ───────────────────────── LOGIC ─────────────────────────

    private fun importData() {
        val chooser = JFileChooser("src/main/kotlin/ChapterExamples").apply {
            fileFilter = FileNameExtensionFilter("Text Files (*.txt)", "txt")
            isAcceptAllFileFilterUsed = false

        }

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            closeResults()

            loadedFile = chooser.selectedFile
            loadedData = KSLFileUtil.scanToArray(loadedFile!!.toPath())

            loadedFile = chooser.selectedFile
            loadedData = KSLFileUtil.scanToArray(loadedFile!!.toPath())
            // Bridge: publish dataset to Model Executor
            InputModelStore.setData(loadedFile!!.name, loadedData!!)


            statusArea.text = """
                DATA LOADED
                File: ${loadedFile!!.name}
                Records: ${loadedData!!.size}
                
                Shared with Model Executor:
                ${'$'}{InputModelStore.summaryString()}
            """.trimIndent()

            updateButtons()
        }
    }

    private fun updateButtons() {
        val enabled = loadedData != null
        runHtmlBtn.isEnabled = enabled
        runStatsBtn.isEnabled = enabled
    }

    private fun closeResults() {
        tabbedPane.removeAll()
        logArea.text = "Results closed."
        consoleArea.text = ""
        closeResultsItem.isEnabled = false
        //clears the shared data
        InputModelStore.clear()

    }

    private fun runHtmlModeling() {
        val data = loadedData ?: return

        tabbedPane.removeAll()
        logArea.text = "Generating HTML reports..."
        closeResultsItem.isEnabled = true

        val baos = ByteArrayOutputStream()
        val ps = PrintStream(baos)
        val oldOut = System.out
        System.setOut(ps)

        try {
            val modeler = PDFModeler(data)
            val results = modeler.showAllResultsInBrowser()
            modeler.showAllGoodnessOfFitSummariesInBrowser(results)

            loadHtmlTabs()
            consoleArea.text = baos.toString()
            logArea.text = "HTML reports generated successfully."
        } catch (e: Exception) {
            consoleArea.text = "ERROR: ${e.message}"
        } finally {
            System.setOut(oldOut)
        }
    }

    private fun runStatTests() {
        val data = loadedData ?: return
        consoleArea.text = ""
        logArea.text = "Running statistical tests..."

        val baos = ByteArrayOutputStream()
        val ps = PrintStream(baos)
        val oldOut = System.out
        System.setOut(ps)

        try {
            val k = 10
            println("Chi-Squared = ${U01Test.chiSquaredTestStatistic(data, k)}")
            println("KS Statistic = ${Statistic.ksTestStatistic(data, Uniform())}")

            consoleArea.text = baos.toString()
            logArea.text = "Statistical tests complete."
        } finally {
            System.setOut(oldOut)
        }
    }

    private fun loadHtmlTabs() {
        if (!outputDir.exists()) return

        val categories = listOf(
            "Statistical_Summary",
            "Visualization_Summary",
            "Scoring_Summary",
            "GoodnessOfFit_Summary",
            "Goodness_Of_Fit_Summaries"
        )

        for (cat in categories) {
            val file = outputDir.listFiles { _, name ->
                name.contains(cat) && name.endsWith(".html")
            }?.maxByOrNull { it.lastModified() }

            if (file != null) {
                val editor = JEditorPane(file.toURI().toURL()).apply {
                    isEditable = false
                    contentType = "text/html"
                }
                tabbedPane.addTab(cat.replace("_", " "), JScrollPane(editor))
            }
        }
    }
}

fun main() {
    SwingUtilities.invokeLater { DistributionModeler().isVisible = true }
}
