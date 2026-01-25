package gui

import java.awt.*
import java.awt.Desktop
import java.io.File
import java.net.URI
import java.sql.Connection
import java.sql.DriverManager
import javax.swing.*
import javax.swing.border.EmptyBorder

fun main() {
    SwingUtilities.invokeLater {
        ResultsAnalyzerFrame().isVisible = true
    }
}

class ResultsAnalyzerFrame : JFrame("Results Analyzer") {

    private var connection: Connection? = null
    private var selectedDatabase: File? = null

    init {
        defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        minimumSize = Dimension(1200, 720)
        setLocationRelativeTo(null)

        contentPane.layout = BorderLayout(8, 8)
        (contentPane as JComponent).border = EmptyBorder(8, 8, 8, 8)

        contentPane.add(buildMenuBar(), BorderLayout.NORTH)
        contentPane.add(buildPlotPanel(), BorderLayout.CENTER)
    }

    /* =======================
       Menu Bar
     ======================= */

    private fun buildMenuBar(): JToolBar {
        val toolbar = JToolBar().apply {
            isFloatable = false
            layout = FlowLayout(FlowLayout.LEFT, 10, 6)
        }

        toolbar.add(databaseMenu())
        toolbar.add(simpleButton("SELECT"))
        toolbar.add(simpleButton("COMPARE"))
        toolbar.add(simpleButton("REPORTS"))
        toolbar.add(plotsMenu())
        toolbar.add(simpleButton("SAVE"))
        toolbar.add(simpleButton("SETTINGS"))
        toolbar.add(helpButton())

        return toolbar
    }

    private fun simpleButton(text: String): JButton =
        JButton(text).apply {
            isFocusable = false
            font = font.deriveFont(Font.PLAIN, 15f)
            preferredSize = Dimension(120, 28)
        }

    /* =======================
       DATABASE Dropdown
     ======================= */

    private fun databaseMenu(): JButton {
        val menu = JPopupMenu()

        return JButton("DATABASE").apply {
            isFocusable = false
            font = font.deriveFont(Font.PLAIN, 15f)
            preferredSize = Dimension(140, 28)
            addActionListener {
                // Show popup menu
                menu.show(this, 0, height)

                // Prompt user to select a database file
                val chooser = JFileChooser().apply {
                    dialogTitle = "Select KSL Database"
                    fileSelectionMode = JFileChooser.FILES_ONLY
                }

                if (chooser.showOpenDialog(this@ResultsAnalyzerFrame) == JFileChooser.APPROVE_OPTION) {
                    selectedDatabase = chooser.selectedFile
                    try {
                        connection?.close()
                        connection = DriverManager.getConnection(
                            "jdbc:sqlite:${selectedDatabase!!.absolutePath}"
                        )
                        JOptionPane.showMessageDialog(
                            this@ResultsAnalyzerFrame,
                            "Database loaded successfully:\n${selectedDatabase!!.name}",
                            "Database Loaded",
                            JOptionPane.INFORMATION_MESSAGE
                        )
                    } catch (ex: Exception) {
                        JOptionPane.showMessageDialog(
                            this@ResultsAnalyzerFrame,
                            "Failed to open database:\n${ex.message}",
                            "Database Error",
                            JOptionPane.ERROR_MESSAGE
                        )
                    }
                }
            }
        }
    }

    /* =======================
       PLOTS Dropdown
     ======================= */

    private fun plotsMenu(): JButton {
        val menu = JPopupMenu()

        menu.add(JRadioButtonMenuItem("Histogram"))
        menu.add(JRadioButtonMenuItem("Probability Plot"))
        menu.add(JRadioButtonMenuItem("Density Estimate"))

        return JButton("PLOTS").apply {
            isFocusable = false
            font = font.deriveFont(Font.PLAIN, 15f)
            preferredSize = Dimension(120, 28)
            addActionListener {
                menu.show(this, 0, height)
            }
        }
    }

    /* =======================
       HELP → External Link
     ======================= */

    private fun helpButton(): JButton =
        JButton("HELP").apply {
            isFocusable = false
            font = font.deriveFont(Font.PLAIN, 15f)
            preferredSize = Dimension(120, 28)
            addActionListener {
                try {
                    Desktop.getDesktop().browse(
                        URI("https://rossetti.github.io/KSLBook/")
                    )
                } catch (ex: Exception) {
                    JOptionPane.showMessageDialog(
                        this,
                        "Unable to open help page.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                    )
                }
            }
        }

    /* =======================
       Plot Area + Stats
     ======================= */

    private fun buildPlotPanel(): JComponent {
        val panel = JPanel(BorderLayout())
        panel.border = BorderFactory.createTitledBorder("Results")

        val plotPlaceholder = JPanel().apply {
            preferredSize = Dimension(700, 450)
            background = Color.WHITE
            border = BorderFactory.createLineBorder(Color.DARK_GRAY)
        }

        panel.add(plotPlaceholder, BorderLayout.CENTER)
        panel.add(buildStatsBar(), BorderLayout.SOUTH)

        return panel
    }

    private fun buildStatsBar(): JComponent {
        val stats = JPanel(FlowLayout(FlowLayout.LEFT, 20, 6))
        stats.border = EmptyBorder(6, 6, 6, 6)
        return stats
    }
}
