package simulation

import java.awt.*
import javax.swing.*
import javax.swing.border.BevelBorder
import javax.swing.border.EmptyBorder
import javax.swing.border.TitledBorder

class DistributionModeler : JFrame("Distribution Modeler") {

    // Dynamic UI Components
    private val xAxisLabel = JLabel("", SwingConstants.CENTER)
    private val yAxisLabel = JLabel("", SwingConstants.CENTER)
    private val dataSummaryArea = JTextArea("Waiting for data...")
    private val generalSummaryArea = JTextArea("No results yet.")
    private val fitLabel = JLabel("Fit: --%", SwingConstants.CENTER)

    // Panel for the graph so we can swap what's inside
    private val graphDisplayArea = JPanel(BorderLayout())

    init {
        defaultCloseOperation = EXIT_ON_CLOSE
        title = "Distribution Modeler"
        minimumSize = Dimension(1000, 700)
        setLocationRelativeTo(null)
        layout = BorderLayout()

        // 1. Classic Menu Bar
        setupMenuBar()

        // 2. Toolbar (Microsoft/Google vibe)
        add(createToolbar(), BorderLayout.NORTH)

        // 3. Main Split Pane
        val mainSplit = JSplitPane(JSplitPane.VERTICAL_SPLIT)
        mainSplit.resizeWeight = 0.7

        // --- Top Section: Visualization ---
        val visualizationPanel = JPanel(BorderLayout(10, 10))
        visualizationPanel.background = Color.WHITE
        visualizationPanel.border = EmptyBorder(20, 20, 20, 20)

        // Axis Labels (Start empty, updated via code)
        xAxisLabel.font = Font("Arial", Font.ITALIC, 14)
        yAxisLabel.font = Font("Arial", Font.ITALIC, 14)

        graphDisplayArea.background = Color.WHITE
        graphDisplayArea.border = BevelBorder(BevelBorder.LOWERED)

        visualizationPanel.add(yAxisLabel, BorderLayout.WEST)
        visualizationPanel.add(graphDisplayArea, BorderLayout.CENTER)
        visualizationPanel.add(xAxisLabel, BorderLayout.SOUTH)

        mainSplit.topComponent = visualizationPanel

        // --- Bottom Section: Triple Panel Results ---
        val resultsPanel = JPanel(GridLayout(1, 3, 10, 0))
        resultsPanel.border = EmptyBorder(10, 10, 10, 10)
        resultsPanel.preferredSize = Dimension(0, 200)

        resultsPanel.add(createScrollableBox("Data", dataSummaryArea))
        resultsPanel.add(createScrollableBox("Summary", generalSummaryArea)) // Generic title

        val fitPanel = JPanel(GridBagLayout())
        fitPanel.border = TitledBorder("Analysis Result")
        fitLabel.font = Font("Segoe UI", Font.BOLD, 32)
        fitLabel.foreground = Color(0, 51, 153)
        fitPanel.add(fitLabel)
        resultsPanel.add(fitPanel)

        mainSplit.bottomComponent = resultsPanel
        add(mainSplit, BorderLayout.CENTER)
    }

    private fun setupMenuBar() {
        val menuBar = JMenuBar()
        val fileMenu = JMenu("File")
        val importItem = JMenuItem("Import Data...")
        importItem.addActionListener { simulateDataImport() }

        fileMenu.add(JMenuItem("New"))
        fileMenu.add(importItem)
        fileMenu.addSeparator()
        fileMenu.add(JMenuItem("Exit"))

        menuBar.add(fileMenu)
        menuBar.add(JMenu("Edit"))
        menuBar.add(JMenu("View"))
        menuBar.add(JMenu("Fit"))
        menuBar.add(JMenu("Options"))
        menuBar.add(JMenu("Help"))
        jMenuBar = menuBar
    }

    private fun createToolbar(): JToolBar {
        val tb = JToolBar()
        tb.isFloatable = false
        val btns = listOf("New", "Open", "Save", "Run Fit")
        btns.forEach {
            val b = JButton(it)
            b.isFocusable = false
            tb.add(b)
            tb.addSeparator()
        }
        return tb
    }

    private fun createScrollableBox(title: String, textArea: JTextArea): JScrollPane {
        textArea.isEditable = false
        val scroll = JScrollPane(textArea)
        scroll.border = TitledBorder(title)
        return scroll
    }

    /**
     * Logic to update the UI once data is loaded
     */
    private fun updateUI(xName: String, yName: String, dataText: String, summaryText: String, fit: String) {
        xAxisLabel.text = xName
        yAxisLabel.text = "<html><body style='text-align:center'>$yName</body></html>"
        dataSummaryArea.text = dataText
        generalSummaryArea.text = summaryText
        fitLabel.text = "Fit: $fit"

        // Refresh layout
        revalidate()
        repaint()
    }

    private fun simulateDataImport() {
        // In the future, this would call your Results_Analyzer
        updateUI(
            xName = "Observation Range",
            yName = "Frequency<br>(count)",
            dataText = "Source: data_sample.csv\nPoints: 1,500\nType: Continuous",
            summaryText = "Distribution: Normal\nMean: 45.2\nStdDev: 2.1\nVariance: 4.41",
            fit = "94%"
        )
    }
}

fun main() {
    try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()) } catch (e: Exception) {}
    SwingUtilities.invokeLater { DistributionModeler().isVisible = true }
}