package gui

import java.awt.*
import java.awt.event.*
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.border.TitledBorder

fun main() {
    SwingUtilities.invokeLater {
        ModelFrame().isVisible = true
    }
}

class ModelFrame : JFrame("Model Executor") {
    private val progressBar = JProgressBar(0, 20).apply {
        value = 3
        isStringPainted = true
        string = "3/20"
    }
    private val statusLabel = JLabel("Status: Idle")

    init {
        defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        minimumSize = Dimension(1200, 720)
        setLocationRelativeTo(null)

        contentPane.layout = BorderLayout(8, 8)
        (contentPane as JComponent).border = EmptyBorder(8, 8, 8, 8)

        contentPane.add(buildNorthHeader(), BorderLayout.NORTH)

        // Simple tabbed editor area
        val editorTabs = DraggableTabbedPane()
        editorTabs.addTab("Run Set-up", buildRunSetupPanel())
        editorTabs.addTab("Run Control", buildRunControlPanel())
        editorTabs.addTab("Advanced Settings", buildAdvancedSettingsPanel())
        editorTabs.addTab("Blank", buildBlankPanel())

        // Console tabs
        val consoleTabs = JTabbedPane().apply {
            minimumSize = Dimension(0, 80)
            preferredSize = Dimension(0, 180)
            addTab("Console", JScrollPane(JTextArea("Console output...\n").apply {
                isEditable = false
                font = Font(Font.MONOSPACED, Font.PLAIN, 12)
            }))
            addTab("Error", JScrollPane(JTextArea().apply {
                isEditable = false
                font = Font(Font.MONOSPACED, Font.PLAIN, 12)
            }))
        }

        val splitPane = JSplitPane(JSplitPane.VERTICAL_SPLIT, editorTabs, consoleTabs).apply {
            resizeWeight = 0.75
            dividerSize = 6
        }
        contentPane.add(splitPane, BorderLayout.CENTER)
    }

    private fun buildNorthHeader(): JComponent {
        val north = JPanel()
        north.layout = BoxLayout(north, BoxLayout.Y_AXIS)

        val infoBar = JPanel(FlowLayout(FlowLayout.LEFT, 8, 4))
        listOf("Model:", "Label:", "Experiment:", "Time Unit:", "Default Stream:").forEach { label ->
            infoBar.add(JLabel(label))
            infoBar.add(JTextField(8))
        }
        infoBar.add(Box.createHorizontalGlue())
        infoBar.add(JButton("Attach DB..."))

        val menuRow = JPanel(FlowLayout(FlowLayout.LEFT, 8, 4))
        listOf("File", "Edit", "Options", "Help", "Input analyzer", "Output analyzer").forEach { text ->
            menuRow.add(JButton(text))
        }

        north.add(infoBar)
        north.add(menuRow)
        return north
    }

    private fun buildRunSetupPanel(): JPanel {
        val panel = JPanel(GridBagLayout())
        panel.border = TitledBorder("Run Set-up")
        val gbc = GridBagConstraints().apply {
            insets = Insets(4, 8, 4, 8)
            anchor = GridBagConstraints.WEST
        }

        listOf("# Reps", "Rep Length", "Warm-up", "Output dir", "auto CVS").forEachIndexed { i, label ->
            gbc.gridy = i
            gbc.gridx = 0; panel.add(JLabel(label), gbc)
            gbc.gridx = 1; panel.add(JTextField(10), gbc)
        }
        gbc.gridy = 5; gbc.weighty = 1.0; panel.add(Box.createVerticalGlue(), gbc)
        return panel
    }

    private fun buildRunControlPanel(): JPanel {
        val panel = JPanel(GridBagLayout())
        panel.border = TitledBorder("Run Control and Progress")
        val gbc = GridBagConstraints().apply {
            insets = Insets(8, 8, 8, 8)
            fill = GridBagConstraints.HORIZONTAL
            gridx = 0
            weightx = 1.0
        }

        val buttons = JPanel(FlowLayout(FlowLayout.LEFT))
        buttons.add(JButton("Run all"))
        buttons.add(JButton("Step"))
        buttons.add(JButton("Stop"))
        gbc.gridy = 0; panel.add(buttons, gbc)

        val progress = JPanel(FlowLayout(FlowLayout.LEFT))
        progress.add(JLabel("Progress:"))
        progress.add(progressBar)
        gbc.gridy = 1; panel.add(progress, gbc)

        gbc.gridy = 2; panel.add(JButton("Reporter Options..."), gbc)
        gbc.gridy = 3; gbc.weighty = 1.0; panel.add(Box.createVerticalGlue(), gbc)
        gbc.gridy = 4; gbc.weighty = 0.0; panel.add(statusLabel, gbc)
        return panel
    }

    private fun buildAdvancedSettingsPanel(): JPanel {
        val panel = JPanel(GridBagLayout())
        panel.border = TitledBorder("Advanced Settings")
        val gbc = GridBagConstraints().apply {
            insets = Insets(4, 8, 4, 8)
            anchor = GridBagConstraints.WEST
            gridx = 0
        }

        listOf("Batching" to true, "Half-Width Checker" to false, "Run Parameters" to true, "Track Variable Changes" to true)
            .forEachIndexed { i, (label, checked) ->
                gbc.gridy = i
                panel.add(JCheckBox(label, checked), gbc)
            }
        gbc.gridy = 4; gbc.weighty = 1.0; panel.add(Box.createVerticalGlue(), gbc)
        return panel
    }

    private fun buildBlankPanel() = JPanel().apply { border = TitledBorder("Blank") }
}

/**
 * Simple draggable tabs - drag to reorder, drag to edge to split
 */
class DraggableTabbedPane : JPanel(BorderLayout()) {
    private val tabbedPane = JTabbedPane()
    private var dragIndex = -1
    private var splitPane: JSplitPane? = null
    private var secondPane: JTabbedPane? = null

    init {
        add(tabbedPane, BorderLayout.CENTER)

        tabbedPane.addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {
                dragIndex = tabbedPane.indexAtLocation(e.x, e.y)
            }

            override fun mouseReleased(e: MouseEvent) {
                if (dragIndex < 0 || tabbedPane.tabCount <= 1) {
                    dragIndex = -1
                    return
                }

                val p = SwingUtilities.convertPoint(tabbedPane, e.point, this@DraggableTabbedPane)
                val w = this@DraggableTabbedPane.width
                val h = this@DraggableTabbedPane.height
                val edge = 80

                when {
                    p.x < edge -> split(dragIndex, JSplitPane.HORIZONTAL_SPLIT, true)
                    p.x > w - edge -> split(dragIndex, JSplitPane.HORIZONTAL_SPLIT, false)
                    p.y > h - edge -> split(dragIndex, JSplitPane.VERTICAL_SPLIT, false)
                    else -> {
                        // Reorder tabs
                        val targetIndex = tabbedPane.indexAtLocation(e.x, e.y)
                        if (targetIndex >= 0 && targetIndex != dragIndex) {
                            reorderTab(dragIndex, targetIndex)
                        }
                    }
                }
                dragIndex = -1
            }
        })
    }

    fun addTab(title: String, content: Component) {
        tabbedPane.addTab(title, content)
    }

    private fun reorderTab(from: Int, to: Int) {
        val title = tabbedPane.getTitleAt(from)
        val content = tabbedPane.getComponentAt(from)
        tabbedPane.removeTabAt(from)
        val newIndex = if (to > from) to - 1 else to
        tabbedPane.insertTab(title, null, content, null, newIndex)
        tabbedPane.selectedIndex = newIndex
    }

    private fun split(tabIndex: Int, orientation: Int, newFirst: Boolean) {
        if (splitPane != null) return // Already split

        val title = tabbedPane.getTitleAt(tabIndex)
        val content = tabbedPane.getComponentAt(tabIndex)
        tabbedPane.removeTabAt(tabIndex)

        secondPane = JTabbedPane()
        secondPane!!.addTab(title, content)

        remove(tabbedPane)

        splitPane = if (newFirst) {
            JSplitPane(orientation, secondPane, tabbedPane)
        } else {
            JSplitPane(orientation, tabbedPane, secondPane)
        }
        splitPane!!.resizeWeight = 0.5
        splitPane!!.dividerSize = 6

        add(splitPane, BorderLayout.CENTER)
        revalidate()
        repaint()
    }
}