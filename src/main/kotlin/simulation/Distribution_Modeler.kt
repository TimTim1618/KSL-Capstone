package simulation

import java.awt.*
import javax.swing.*
import javax.swing.border.BevelBorder
import javax.swing.border.EmptyBorder
import javax.swing.border.TitledBorder

class DistributionModeler : JFrame("Distribution Modeler") {

    private val xAxisLabel = JLabel("", SwingConstants.CENTER)
    private val yAxisLabel = JLabel("", SwingConstants.CENTER)
    private val dataSummaryArea = JTextArea("Waiting for data...")
    private val generalSummaryArea = JTextArea("No results yet.")
    private val graphDisplayArea = JPanel(BorderLayout())

    init {
        defaultCloseOperation = EXIT_ON_CLOSE
        title = "Distribution Modeler"
        minimumSize = Dimension(1000, 700)
        setLocationRelativeTo(null)
        layout = BorderLayout()

        setupMenuBar()
        add(createToolbar(), BorderLayout.NORTH)

        val mainSplit = JSplitPane(JSplitPane.VERTICAL_SPLIT)
        mainSplit.resizeWeight = 0.7

        val visualizationPanel = JPanel(BorderLayout(10, 10))
        visualizationPanel.background = Color.WHITE
        visualizationPanel.border = EmptyBorder(20, 20, 20, 20)

        xAxisLabel.font = Font("Arial", Font.ITALIC, 14)
        yAxisLabel.font = Font("Arial", Font.ITALIC, 14)

        graphDisplayArea.background = Color.WHITE
        graphDisplayArea.border = BevelBorder(BevelBorder.LOWERED)

        visualizationPanel.add(yAxisLabel, BorderLayout.WEST)
        visualizationPanel.add(graphDisplayArea, BorderLayout.CENTER)
        visualizationPanel.add(xAxisLabel, BorderLayout.SOUTH)

        mainSplit.topComponent = visualizationPanel

        val resultsPanel = JPanel(GridLayout(1, 2, 15, 0))
        resultsPanel.border = EmptyBorder(10, 10, 10, 10)
        resultsPanel.preferredSize = Dimension(0, 200)

        resultsPanel.add(createScrollableBox("Data", dataSummaryArea))
        resultsPanel.add(createScrollableBox("Summary", generalSummaryArea))

        mainSplit.bottomComponent = resultsPanel
        add(mainSplit, BorderLayout.CENTER)
    }

    private fun setupMenuBar() {
        val menuBar = JMenuBar()

        // --- FILE MENU ---
        val fileMenu = JMenu("File")
        fileMenu.add(JMenuItem("New Project"))
        val importItem = JMenuItem("Import Data...")
        importItem.addActionListener { simulateDataImport() }
        fileMenu.add(importItem)
        fileMenu.add(JMenuItem("Save Analysis"))
        fileMenu.addSeparator()
        fileMenu.add(JMenuItem("Exit"))

        // --- EDIT MENU ---
        val editMenu = JMenu("Edit")
        editMenu.add(JMenuItem("Undo"))
        editMenu.add(JMenuItem("Redo"))
        editMenu.addSeparator()
        editMenu.add(JMenuItem("Copy Summary Text"))
        editMenu.add(JMenuItem("Clear All Data"))

        // --- VIEW MENU ---
        val viewMenu = JMenu("View")
        viewMenu.add(JCheckBoxMenuItem("Show Gridlines", true))
        viewMenu.add(JMenuItem("Zoom In"))
        viewMenu.add(JMenuItem("Zoom Out"))
        viewMenu.add(JMenuItem("Reset Graph View"))

        // --- FIT MENU (Analysis Specific) ---
        val fitMenu = JMenu("Fit")
        fitMenu.add(JMenuItem("Run Auto-Fit"))
        fitMenu.add(JMenuItem("Chi-Square Test"))
        fitMenu.add(JMenuItem("Kolmogorov-Smirnov Test"))

        // --- OPTIONS MENU ---
        val optionsMenu = JMenu("Options")
        optionsMenu.add(JMenuItem("Color Settings"))
        optionsMenu.add(JMenuItem("Decimal Precision"))
        optionsMenu.add(JMenuItem("Preferences"))

        // --- HELP MENU ---
        val helpMenu = JMenu("Help")
        helpMenu.add(JMenuItem("Documentation"))
        helpMenu.add(JMenuItem("Keyboard Shortcuts"))
        helpMenu.addSeparator()
        helpMenu.add(JMenuItem("About Distribution Modeler"))

        menuBar.add(fileMenu)
        menuBar.add(editMenu)
        menuBar.add(viewMenu)
        menuBar.add(fitMenu)
        menuBar.add(optionsMenu)
        menuBar.add(helpMenu)
        jMenuBar = menuBar
    }

    private fun createToolbar(): JToolBar {
        val tb = JToolBar()
        tb.isFloatable = false

        // Removed "Open", kept others
        val btns = listOf("New", "Save", "Run Fit")
        btns.forEach {
            val b = JButton(it)
            b.isFocusable = false
            tb.add(b)
            tb.add(Box.createHorizontalStrut(5))
        }
        return tb
    }

    private fun createScrollableBox(title: String, textArea: JTextArea): JScrollPane {
        textArea.isEditable = false
        val scroll = JScrollPane(textArea)
        scroll.border = TitledBorder(title)
        return scroll
    }

    private fun simulateDataImport() {
        // This is where we will eventually put the JFileChooser logic
        xAxisLabel.text = "Intervals"
        yAxisLabel.text = "<html>Count</html>"
        dataSummaryArea.text = "Source: external_data.csv\nObservations: 240\nStatus: Loaded"
        generalSummaryArea.text = "Primary Distribution: Exponential\nMean: 12.5\nVariance: 156.25\nFit Quality: High"
        revalidate()
        repaint()
    }
}

fun main() {
    try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()) } catch (e: Exception) {}
    SwingUtilities.invokeLater { DistributionModeler().isVisible = true }
}