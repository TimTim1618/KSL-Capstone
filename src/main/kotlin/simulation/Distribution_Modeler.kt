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
import java.awt.Desktop
import java.net.URI


class DistributionModeler : JFrame("KSL Distribution Modeler") {

    private val statusArea = JTextArea("Load a data file to begin.")
    private val logArea = JTextArea("")
    private val consoleArea = JTextArea("")

    private var loadedData: DoubleArray? = null
    private var loadedFile: File? = null
    private var modelerResults: PDFModeler? = null

    private val outputDir = File("kslOutput/plotDir")

    // Menu items that should be enabled/disabled based on data state
    private lateinit var resultsMenu: JMenu
    private lateinit var runStatsTestItem: JMenuItem

    init {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

        defaultCloseOperation = EXIT_ON_CLOSE
        minimumSize = Dimension(1150, 600)
        layout = BorderLayout()
        setLocationRelativeTo(null)

        setupMenuBar()
        add(createToolbar(), BorderLayout.NORTH)
        add(createMainLayout(), BorderLayout.CENTER)

        updateMenuState()
    }

    // ───────────────────────── UI BUILD ─────────────────────────

    private fun setupMenuBar() {
        jMenuBar = JMenuBar().apply {
            // FILE MENU
            add(JMenu("File").apply {
                add(JMenuItem("Load Data...").apply {
                    addActionListener { importData() }
                    accelerator = KeyStroke.getKeyStroke("control O")
                })

                addSeparator()

                add(JMenuItem("Clear Data").apply {
                    addActionListener { clearData() }
                    accelerator = KeyStroke.getKeyStroke("control W")
                })

                add(JMenuItem("Clear Old Reports").apply {
                    addActionListener { clearOldReports() }
                    toolTipText = "Delete all previously generated HTML reports"
                })

                addSeparator()

                add(JMenuItem("Exit").apply {
                    addActionListener { dispose() }
                    accelerator = KeyStroke.getKeyStroke("control Q")
                })
            })

            // ANALYSIS MENU (browser-based)
            add(JMenu("Analysis").apply {
                add(JMenuItem("Run...").apply {
                    addActionListener { showAnalysisSelector() }
                    accelerator = KeyStroke.getKeyStroke("control R")
                })
            })

            // TESTS MENU (console-based)
            add(JMenu("Tests").apply {
                runStatsTestItem = JMenuItem("Chi-Squared & K-S Tests").apply {
                    addActionListener { runStatTests() }
                    accelerator = KeyStroke.getKeyStroke("control T")
                }
                add(runStatsTestItem)

                // Placeholder for future test types
                // add(JMenuItem("Other Test Type").apply {
                //     addActionListener { runOtherTest() }
                // })
            })

            // RESULTS MENU (dynamically populated based on generated files)
            resultsMenu = JMenu("Results").apply {
                add(JMenuItem("Refresh Report List").apply {
                    addActionListener { refreshResultsMenu() }
                })
                addSeparator()
                // Reports will be added dynamically
            }
            add(resultsMenu)

            // HELP MENU
            add(JMenu("Help").apply {
                add(JMenuItem("Textbook").apply {
                    addActionListener {
                        openLink("https://rossetti.github.io/KSLBook/")
                    }
                })

                add(JMenuItem("GitHub").apply {
                    addActionListener {
                        openLink("https://github.com/TimTim1618/KSL-Capstone")
                    }
                })

                add(JMenuItem("User Guide").apply {
                    addActionListener {
                        openPdf("src/main/kotlin/docs/UserGuide.pdf")
                    }
                })
            })
        }
    }

    private fun openLink(url: String) {
        try {
            Desktop.getDesktop().browse(URI(url))
        } catch (e: Exception) {
            JOptionPane.showMessageDialog(
                this,
                "Unable to open link:\n$url",
                "Error",
                JOptionPane.ERROR_MESSAGE
            )
        }
    }

    private fun openPdf(path: String) {
        try {
            Desktop.getDesktop().open(File(path))
        } catch (e: Exception) {
            JOptionPane.showMessageDialog(
                this,
                "Could not open user guide.",
                "Error",
                JOptionPane.ERROR_MESSAGE
            )
        }
    }

    private fun createToolbar(): JToolBar =
        JToolBar().apply {
            isFloatable = false

            add(JButton("Load Data").apply {
                addActionListener { importData() }
                icon = UIManager.getIcon("FileView.directoryIcon")
            })

            addSeparator()

            add(JButton("Run Analysis").apply {
                background = Color(230, 240, 255)
                addActionListener { showAnalysisSelector() }
                toolTipText = "Select and run an analysis"
            })
        }

    private fun createMainLayout(): JPanel {
        return JPanel(BorderLayout()).apply {
            border = EmptyBorder(10, 10, 10, 10)

            // Main content area - just the three text areas
            add(JPanel(GridLayout(1, 3, 12, 0)).apply {
                preferredSize = Dimension(0, 400)
                add(createBox("System Status", statusArea))
                add(createBox("Execution Log", logArea))
                add(createBox("Console Output", consoleArea))
            }, BorderLayout.CENTER)
        }
    }

    private fun createBox(title: String, area: JTextArea): JScrollPane =
        JScrollPane(area).apply {
            area.isEditable = false
            area.font = Font("Segoe UI", Font.PLAIN, 12)
            area.lineWrap = true
            area.wrapStyleWord = true
            border = TitledBorder(title)
        }

    // ───────────────────────── LOGIC ─────────────────────────

    private fun showAnalysisSelector() {
        if (loadedData == null) {
            JOptionPane.showMessageDialog(
                this,
                "Please load data first.",
                "No Data",
                JOptionPane.WARNING_MESSAGE
            )
            return
        }

        val analysisTypes = arrayOf(
            "Distribution Modeling",
            // Add more analysis types here as you create them
            // "Other Analysis Type",
            // "Another Analysis Type"
        )

        val selected = JOptionPane.showInputDialog(
            this,
            "Select an analysis to run:",
            "Run Analysis",
            JOptionPane.QUESTION_MESSAGE,
            null,
            analysisTypes,
            analysisTypes[0]
        )

        when (selected) {
            "Distribution Modeling" -> runDistributionModeling()
            // Add more cases here for other analysis types
            // "Other Analysis Type" -> runOtherAnalysis()
        }
    }

    private fun importData() {
        val chooser = JFileChooser("src/main/kotlin/ChapterExamples").apply {
            fileFilter = FileNameExtensionFilter("Text Files (*.txt)", "txt")
            isAcceptAllFileFilterUsed = false
        }

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            loadedFile = chooser.selectedFile

            try {
                loadedData = KSLFileUtil.scanToArray(loadedFile!!.toPath())
                modelerResults = null // Clear previous results

                // Update status
                statusArea.text = """
                    DATA LOADED
                    File: ${loadedFile!!.name}
                    Records: ${loadedData!!.size}
                    
                    Ready for analysis.
                    Use Analysis menu to run modeling or tests.
                """.trimIndent()

                logArea.text = "Loaded: ${loadedFile!!.name} (${loadedData!!.size} observations)"
                consoleArea.text = ""

                // Bridge: publish dataset to Model Executor
                InputModelStore.setData(loadedFile!!.name, loadedData!!)

            } catch (e: Exception) {
                JOptionPane.showMessageDialog(
                    this,
                    "Error loading file: ${e.message}",
                    "Load Error",
                    JOptionPane.ERROR_MESSAGE
                )
                statusArea.text = "Error loading file: ${e.message}"
            }

            updateMenuState()
        }
    }

    private fun clearData() {
        loadedData = null
        loadedFile = null
        modelerResults = null
        InputModelStore.clear()

        statusArea.text = "Data cleared. Load a data file to begin."
        logArea.text = ""
        consoleArea.text = ""

        updateMenuState()
    }

    private fun clearOldReports() {
        if (!outputDir.exists()) {
            logArea.text = "No reports directory found."
            return
        }

        val htmlFiles = outputDir.listFiles { _, name -> name.endsWith(".html") } ?: emptyArray()

        if (htmlFiles.isEmpty()) {
            logArea.text = "No reports to clear."
            return
        }

        val result = JOptionPane.showConfirmDialog(
            this,
            "Delete ${htmlFiles.size} old report file(s)?",
            "Clear Old Reports",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        )

        if (result == JOptionPane.YES_OPTION) {
            var deletedCount = 0
            htmlFiles.forEach { file ->
                if (file.delete()) {
                    deletedCount++
                }
            }
            logArea.text = "Deleted $deletedCount report file(s)."
            updateMenuState()
        }
    }

    private fun updateMenuState() {
        val hasData = loadedData != null
        val hasResults = outputDir.exists() && outputDir.listFiles()?.any { it.name.endsWith(".html") } == true

        // Tests menu items need data
        runStatsTestItem.isEnabled = hasData

        // Results menu needs generated reports
        resultsMenu.isEnabled = hasResults

        // Refresh results menu if we have results
        if (hasResults) {
            refreshResultsMenu()
        }
    }

    private fun runDistributionModeling() {
        val data = loadedData ?: return

        // Automatically clear old reports before generating new ones
        if (outputDir.exists()) {
            val oldReports = outputDir.listFiles { _, name -> name.endsWith(".html") } ?: emptyArray()

            if (oldReports.isNotEmpty()) {
                var deletedCount = 0
                oldReports.forEach {
                    if (it.delete()) {
                        deletedCount++
                    }
                }
                logArea.text = "Cleared $deletedCount old report(s).\n"
            }
        }

        logArea.text += "Generating distribution modeling reports...\n"
        consoleArea.text = ""

        val baos = ByteArrayOutputStream()
        val ps = PrintStream(baos)
        val oldOut = System.out
        System.setOut(ps)

        try {
            val modeler = PDFModeler(data)

            // Generate reports WITHOUT opening browsers
            // We'll modify this to not auto-open
            val results = modeler.estimateAndEvaluateScores()

            // Save reports to files without showing in browser
            // Note: You may need to check PDFModeler API for methods that save without opening
            // For now, we'll use the existing methods but mention this needs adjustment
            modeler.showAllResultsInBrowser()  // TODO: Replace with non-opening method if available
            modeler.showAllGoodnessOfFitSummariesInBrowser(results)  // TODO: Replace with non-opening method

            modelerResults = modeler
            consoleArea.text = baos.toString()
            logArea.text += "Reports generated successfully.\n"
            logArea.text += "Use Results menu to view reports."

            // Refresh results menu
            refreshResultsMenu()

        } catch (e: Exception) {
            consoleArea.text = "ERROR: ${e.message}\n${e.stackTraceToString()}"
            logArea.text += "Error during modeling: ${e.message}"
        } finally {
            System.setOut(oldOut)
            updateMenuState()
        }
    }

    private fun refreshResultsMenu() {
        // Remove all items except "Refresh Report List"
        while (resultsMenu.itemCount > 2) {
            resultsMenu.remove(2)
        }

        if (!outputDir.exists()) {
            resultsMenu.add(JMenuItem("(No reports generated yet)").apply { isEnabled = false })
            return
        }

        // Get all HTML files in output directory, sorted by modification time (newest first)
        val htmlFiles = outputDir.listFiles { _, name ->
            name.endsWith(".html")
        }?.sortedByDescending { it.lastModified() } ?: emptyList()

        if (htmlFiles.isEmpty()) {
            resultsMenu.add(JMenuItem("(No reports generated yet)").apply { isEnabled = false })
            return
        }

        // Add "Open All Reports" option
        resultsMenu.add(JMenuItem("Open All Reports").apply {
            addActionListener {
                htmlFiles.forEach { file ->
                    try {
                        Desktop.getDesktop().browse(file.toURI())
                    } catch (e: Exception) {
                        // Continue opening others even if one fails
                    }
                }
                logArea.text = "Opened ${htmlFiles.size} report(s) in browser."
            }
        })

        // Add "Select Reports..." option
        resultsMenu.add(JMenuItem("Select Reports...").apply {
            addActionListener { showDynamicReportSelector(htmlFiles) }
        })

        resultsMenu.addSeparator()

        // Group files by their base name (removing timestamp)
        val groupedFiles = htmlFiles.groupBy { file ->
            // Extract meaningful name (remove timestamp suffix)
            file.nameWithoutExtension.replace(Regex("\\d+$"), "").trimEnd('_')
        }

        // Add menu items for each group
        groupedFiles.forEach { (baseName, files) ->
            val displayName = baseName.replace("_", " ")

            if (files.size == 1) {
                // Single file - direct menu item
                resultsMenu.add(JMenuItem(displayName).apply {
                    addActionListener { openHtmlFile(files[0]) }
                })
            } else {
                // Multiple files - create submenu with timestamps
                val submenu = JMenu(displayName)
                files.forEach { file ->
                    val timestamp = java.text.SimpleDateFormat("HH:mm:ss").format(file.lastModified())
                    submenu.add(JMenuItem("Generated at $timestamp").apply {
                        addActionListener { openHtmlFile(file) }
                    })
                }
                resultsMenu.add(submenu)
            }
        }
    }

    private fun showDynamicReportSelector(htmlFiles: List<File>) {
        val panel = JPanel(GridLayout(0, 1, 5, 5)).apply {
            border = EmptyBorder(10, 10, 10, 10)
        }

        val checkboxes = htmlFiles.map { file ->
            val displayName = file.nameWithoutExtension.replace("_", " ")
            JCheckBox(displayName, false).also { panel.add(it) }
        }

        val scrollPane = JScrollPane(panel).apply {
            preferredSize = Dimension(400, 300)
        }

        val result = JOptionPane.showConfirmDialog(
            this,
            scrollPane,
            "Select Reports to Open",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        )

        if (result == JOptionPane.OK_OPTION) {
            var openedCount = 0
            logArea.text = "Opening selected reports...\n"

            checkboxes.forEachIndexed { index, checkbox ->
                if (checkbox.isSelected) {
                    openHtmlFile(htmlFiles[index])
                    openedCount++
                }
            }

            if (openedCount > 0) {
                logArea.text += "Opened $openedCount report(s) in browser."
            } else {
                logArea.text += "No reports selected."
            }
        }
    }

    private fun openHtmlFile(file: File) {
        try {
            Desktop.getDesktop().browse(file.toURI())
        } catch (e: Exception) {
            logArea.text = "Error opening ${file.name}: ${e.message}"
        }
    }

    private fun runStatTests() {
        val data = loadedData ?: return
        consoleArea.text = ""
        logArea.text = "Running Chi-Squared & K-S tests..."

        val baos = ByteArrayOutputStream()
        val ps = PrintStream(baos)
        val oldOut = System.out
        System.setOut(ps)

        try {
            // Example B.3: 1-D Chi-Squared Test
            var k = 10
            val chiSquaredTestStatistic = U01Test.chiSquaredTestStatistic(data, k)
            var chiDist = ksl.utilities.distributions.ChiSquaredDistribution(k - 1.0)
            var pValue = chiDist.complementaryCDF(chiSquaredTestStatistic)

            println("Example B.3")
            println("1-D Chi-Squared Test Statistic = $chiSquaredTestStatistic")
            println("P-Value = $pValue")
            println()

            // Example B.4: 2-D Chi-Squared Test
            k = 4
            val chi2D = U01Test.chiSquaredSerial2DTestStatistic(data, k)
            val dof = k * k - 1
            chiDist = ksl.utilities.distributions.ChiSquaredDistribution(dof.toDouble())
            pValue = chiDist.complementaryCDF(chi2D)
            println("Example B.4")
            println("2-D Chi-Squared Test Statistic = $chi2D")
            println("dof = $dof")
            println("P-Value = $pValue")
            println()

            // Example B.5: Kolmogorov-Smirnov Test
            val ks = Statistic.ksTestStatistic(data, Uniform())
            pValue = ksl.utilities.distributions.KolmogorovSmirnovDist.complementaryCDF(data.size, ks)
            println("Example B.5")
            println("K-S Test Statistic = $ks")
            println("P-Value = $pValue")
            println()

            consoleArea.text = baos.toString()
            logArea.text = "Chi-Squared & K-S tests completed successfully."
        } catch (e: Exception) {
            consoleArea.text = "ERROR: ${e.message}\n${e.stackTraceToString()}"
            logArea.text = "Error during statistical tests: ${e.message}"
        } finally {
            System.setOut(oldOut)
        }
    }
}

fun main() {
    SwingUtilities.invokeLater { DistributionModeler().isVisible = true }
}