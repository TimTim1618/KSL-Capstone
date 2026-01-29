package gui

import java.awt.*
import java.awt.event.*
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.border.TitledBorder

fun main() {
    SwingUtilities.invokeLater { ModelFrame().isVisible = true }
}

class ModelFrame : JFrame("Model Executor") {
    private val editorTabs = DraggableTabbedPane()

    init {
        defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        minimumSize = Dimension(1200, 720)
        setLocationRelativeTo(null)

        contentPane.layout = BorderLayout(8, 8)
        (contentPane as JComponent).border = EmptyBorder(8, 8, 8, 8)

        contentPane.add(buildHeader(), BorderLayout.NORTH)

        editorTabs.addTab("Run Set-up", buildRunSetupPanel())
        editorTabs.addTab("Run Control", buildRunControlPanel())
        editorTabs.addTab("Advanced Settings", buildAdvancedSettingsPanel())
        editorTabs.addTab("Blank", JPanel().apply { border = TitledBorder("Blank") })

        val console = JTabbedPane().apply {
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

        contentPane.add(JSplitPane(JSplitPane.VERTICAL_SPLIT, editorTabs, console).apply {
            resizeWeight = 0.75
            dividerSize = 6
        }, BorderLayout.CENTER)
    }

    private fun buildHeader(): JComponent {
        val header = JPanel().apply { layout = BoxLayout(this, BoxLayout.Y_AXIS) }

        val infoBar = JPanel(FlowLayout(FlowLayout.LEFT, 8, 4))
        listOf("Model:", "Label:", "Experiment:", "Time Unit:", "Default Stream:").forEach {
            infoBar.add(JLabel(it))
            infoBar.add(JTextField(8))
        }
        infoBar.add(Box.createHorizontalGlue())
        infoBar.add(JButton("Attach DB..."))

        val menuRow = JPanel(FlowLayout(FlowLayout.LEFT, 8, 4))
        listOf("File", "Edit", "Options", "Help", "Input analyzer", "Output analyzer").forEach {
            menuRow.add(JButton(it))
        }
        menuRow.add(JButton("Reset View").apply { addActionListener { editorTabs.resetView() } })

        header.add(infoBar)
        header.add(menuRow)
        return header
    }

    //new run setup layout logic
    private fun buildRunSetupPanel(): JComponent {
        val outer = JPanel(BorderLayout()).apply{
            border = TitledBorder("Run Setup")
        }
        val form = JPanel(GridBagLayout()).apply{
            border = EmptyBorder(18, 18, 18, 18)
        }
        val gbc = GridBagConstraints().apply {
            insets = Insets(10, 10, 10, 10)
            anchor = GridBagConstraints.WEST
            fill = GridBagConstraints.NONE
            weightx = 0.0
            gridy = 0
        }

        fun addRow(label: String, fieldCols: Int = 18) {
            gbc. gridx = 0
            gbc.weightx = 0.0
            gbc.fill = GridBagConstraints.NONE
            gbc.anchor = GridBagConstraints.WEST
            form.add(JLabel(label).apply{ font = font.deriveFont(12f) }, gbc)

            gbc.gridx = 1
            gbc.weightx = 1.0
            gbc.fill = GridBagConstraints.HORIZONTAL
            gbc.anchor = GridBagConstraints.EAST
            form.add(JTextField(fieldCols).apply {
                preferredSize = Dimension(260, 32)
                minimumSize = Dimension(260, 32)
            }, gbc)
            gbc.gridy++
        }
        addRow("Number of Repetitions")
        addRow("Repetition Length")
        addRow("Warm-up")
        addRow("Output distribution")
        addRow("auto CVS")

        gbc.gridx = 0
        gbc.gridwidth = 2
        gbc.weighty = 1.0
        gbc.fill = GridBagConstraints.VERTICAL
        form.add(Box.createVerticalGlue(), gbc)


        val topRight = JPanel(BorderLayout()).apply {
            add(form, BorderLayout.NORTH)
        }
        outer.add(topRight, BorderLayout.WEST)

        return outer
    }


    /*private fun buildRunSetupPanel() = JPanel(GridBagLayout()).apply {
        border = TitledBorder("Run Set-up")
        val gbc = GridBagConstraints().apply { insets = Insets(4, 8, 4, 8); anchor = GridBagConstraints.WEST }
        listOf("Number of Repetitions", "Repetition Length", "Warm-up", "Output distribution", "auto CVS").forEachIndexed { i, label ->
            gbc.gridy = i; gbc.gridx = 0; add(JLabel(label), gbc)
            gbc.gridx = 1; add(JTextField(10), gbc)
        }
        gbc.gridy = 5; gbc.weighty = 1.0; add(Box.createVerticalGlue(), gbc)
    }*/

    private fun buildRunControlPanel() = JPanel(GridBagLayout()).apply {
        border = TitledBorder("Run Control and Progress")
        val gbc = GridBagConstraints().apply { insets = Insets(8, 8, 8, 8); fill = GridBagConstraints.HORIZONTAL; weightx = 1.0 }

        gbc.gridy = 0; add(JPanel(FlowLayout(FlowLayout.LEFT)).apply {
        add(JButton("Run all")); add(JButton("Step")); add(JButton("Stop"))
    }, gbc)

        gbc.gridy = 1; add(JPanel(FlowLayout(FlowLayout.LEFT)).apply {
        add(JLabel("Progress:"))
        add(JProgressBar(0, 20).apply { value = 3; isStringPainted = true; string = "3/20" })
    }, gbc)

        gbc.gridy = 2; add(JButton("Reporter Options..."), gbc)
        gbc.gridy = 3; gbc.weighty = 1.0; add(Box.createVerticalGlue(), gbc)
        gbc.gridy = 4; gbc.weighty = 0.0; add(JLabel("Status: Idle"), gbc)
    }

    /*private fun buildAdvancedSettingsPanel() = JPanel(GridBagLayout()).apply {
        border = TitledBorder("Advanced Settings")
        val gbc = GridBagConstraints().apply { insets = Insets(4, 8, 4, 8); anchor = GridBagConstraints.WEST; gridx = 0 }
        listOf("Batching" to true, "Half-Width Checker" to false, "Run Parameters" to true, "Track Variable Changes" to true)
            .forEachIndexed { i, (label, checked) -> gbc.gridy = i; add(JCheckBox(label, checked), gbc) }
        gbc.gridy = 4; gbc.weighty = 1.0; add(Box.createVerticalGlue(), gbc)
    }*/

    //new advanced settings layout logic
    private fun buildAdvancedSettingsPanel(): JComponent {
        val outer = JPanel(BorderLayout()).apply {
            border = TitledBorder("Advanced Settings")
        }

        val box = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            border = EmptyBorder(18, 18, 18, 18)
        }

        fun setting(text: String, checked: Boolean): JCheckBox {
            return JCheckBox(text, checked).apply {
                alignmentX = Component.LEFT_ALIGNMENT
                font = font.deriveFont(12f)
                border = EmptyBorder(6, 6, 6, 6)
            }
        }

        box.add(setting("Batching", true))
        box.add(setting("Half-Width Checker", false))
        box.add(setting("Run Parameters", true))
        box.add(setting("Track Variable Changes", true))

        box.add(Box.createVerticalGlue()) // push top


        val topRight = JPanel(BorderLayout()).apply {
            add(box, BorderLayout.NORTH)
        }
        outer.add(topRight, BorderLayout.WEST)

        return outer
    }

}

/**
 * Draggable tabs with 2x2 grid layout (max 4 panes):
 * - Top-Left, Top-Right, Bottom-Left, Bottom-Right
 */
class DraggableTabbedPane : JPanel() {
    private val panes = mutableListOf<JTabbedPane>()
    private val mainPane = JTabbedPane()
    private val overlay = Overlay()
    private var dragTab = -1
    private var dragPane: JTabbedPane? = null

    // Grid positions: null means empty
    private var topLeft: JTabbedPane? = null
    private var topRight: JTabbedPane? = null
    private var bottomLeft: JTabbedPane? = null
    private var bottomRight: JTabbedPane? = null

    // Split panes for grid structure
    private var rootSplit: JSplitPane? = null    // Vertical: top/bottom
    private var topSplit: JSplitPane? = null     // Horizontal: topLeft/topRight
    private var bottomSplit: JSplitPane? = null  // Horizontal: bottomLeft/bottomRight

    init {
        layout = null
        topLeft = mainPane
        panes.add(mainPane)
        add(mainPane)
        add(overlay)
        setComponentZOrder(overlay, 0)
        setupDrag(mainPane)
    }

    override fun doLayout() {
        val content = components.firstOrNull { it != overlay }
        content?.setBounds(0, 0, width, height)
        overlay.setBounds(0, 0, width, height)
        if (overlay.parent == this) setComponentZOrder(overlay, 0)
    }

    fun addTab(title: String, content: Component) = mainPane.addTab(title, content)

    fun resetView() {
        val tabs = panes.flatMap { p -> (0 until p.tabCount).map { p.getTitleAt(it) to p.getComponentAt(it) } }
        removeAll()
        panes.clear()
        mainPane.removeAll()
        panes.add(mainPane)
        topLeft = mainPane
        topRight = null
        bottomLeft = null
        bottomRight = null
        rootSplit = null
        topSplit = null
        bottomSplit = null
        tabs.forEach { (title, content) -> mainPane.addTab(title, content) }
        add(mainPane)
        add(overlay)
        setComponentZOrder(overlay, 0)
        revalidate()
        repaint()
    }

    private fun setupDrag(pane: JTabbedPane) {
        pane.addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {
                val idx = pane.indexAtLocation(e.x, e.y)
                if (idx >= 0) { dragTab = idx; dragPane = pane }
            }

            override fun mouseReleased(e: MouseEvent) {
                overlay.zone = null
                overlay.insertLine = null
                overlay.repaint()
                if (dragTab < 0 || dragPane == null) return

                val p = e.point
                val outside = p.x < 0 || p.x > pane.width || p.y < 0 || p.y > pane.height
                val inTabBar = p.y < tabBarHeight(pane)

                when {
                    outside -> {
                        val target = panes.firstOrNull { it != dragPane && it.isShowing &&
                                Rectangle(it.locationOnScreen, it.size).contains(e.locationOnScreen) }
                        if (target != null) moveTab(dragPane!!, dragTab, target)
                        // else popOut(dragPane!!, dragTab, e.locationOnScreen)  // Window detach disabled
                    }
                    inTabBar && pane.tabCount > 1 -> reorderTab(pane, dragTab, p.x)
                    canSplit() -> getZone(pane, p)?.let { zone -> splitTo(dragPane!!, dragTab, zone) }
                }
                dragTab = -1; dragPane = null
            }
        })

        pane.addMouseMotionListener(object : MouseMotionAdapter() {
            override fun mouseDragged(e: MouseEvent) {
                if (dragTab < 0) {
                    overlay.zone = null
                    overlay.insertLine = null
                    overlay.repaint()
                    return
                }

                val p = e.point
                val inTabBar = p.y in 0..tabBarHeight(pane)
                val inBounds = p.x in 0..pane.width && p.y in 0..pane.height

                if (!inBounds) {
                    val screenPoint = e.locationOnScreen
                    val targetPane = panes.firstOrNull { it != dragPane && it.isShowing &&
                            Rectangle(it.locationOnScreen, it.size).contains(screenPoint) }
                    if (targetPane != null && targetPane.tabCount > 0) {
                        val lastTabBounds = targetPane.getBoundsAt(targetPane.tabCount - 1)
                        if (lastTabBounds != null) {
                            val x = lastTabBounds.x + lastTabBounds.width
                            val lineRect = Rectangle(x - 2, lastTabBounds.y, 4, lastTabBounds.height)
                            overlay.insertLine = SwingUtilities.convertRectangle(targetPane, lineRect, this@DraggableTabbedPane)
                            overlay.zone = null
                            overlay.repaint()
                            return
                        }
                    }
                    overlay.zone = null
                    overlay.insertLine = null
                    overlay.repaint()
                    return
                }

                if (inTabBar && inBounds && pane.tabCount > 1) {
                    val insertIdx = getInsertIndex(pane, p.x)
                    if (insertIdx != dragTab && insertIdx != dragTab + 1) {
                        val x = if (insertIdx < pane.tabCount) {
                            pane.getBoundsAt(insertIdx)?.x ?: 0
                        } else {
                            pane.getBoundsAt(pane.tabCount - 1)?.let { it.x + it.width } ?: 0
                        }
                        val tabBounds = pane.getBoundsAt(0)
                        val lineRect = Rectangle(x - 2, tabBounds?.y ?: 0, 4, tabBounds?.height ?: 25)
                        overlay.insertLine = SwingUtilities.convertRectangle(pane, lineRect, this@DraggableTabbedPane)
                    } else {
                        overlay.insertLine = null
                    }
                    overlay.zone = null
                } else if (canSplit()) {
                    overlay.insertLine = null
                    overlay.zone = getZone(pane, p)?.let { zone ->
                        val b = SwingUtilities.convertRectangle(pane, Rectangle(0, 0, pane.width, pane.height), this@DraggableTabbedPane)
                        when (zone) {
                            Zone.LEFT -> Rectangle(b.x, b.y, b.width / 2, b.height)
                            Zone.RIGHT -> Rectangle(b.x + b.width / 2, b.y, b.width / 2, b.height)
                            Zone.TOP -> Rectangle(b.x, b.y, b.width, b.height / 2)
                            Zone.BOTTOM -> Rectangle(b.x, b.y + b.height / 2, b.width, b.height / 2)
                        }
                    }
                } else {
                    overlay.zone = null
                    overlay.insertLine = null
                }
                overlay.repaint()
            }
        })
    }

    private fun canSplit() = dragPane?.tabCount?.let { it > 1 } == true && panes.size < 4
    private fun tabBarHeight(pane: JTabbedPane) = pane.getBoundsAt(0)?.let { it.y + it.height + 5 } ?: 30

    private fun getInsertIndex(pane: JTabbedPane, x: Int): Int {
        for (i in 0 until pane.tabCount) {
            pane.getBoundsAt(i)?.let { if (x < it.x + it.width / 2) return i }
        }
        return pane.tabCount
    }

    private fun getPosition(pane: JTabbedPane): Position? = when (pane) {
        topLeft -> Position.TOP_LEFT
        topRight -> Position.TOP_RIGHT
        bottomLeft -> Position.BOTTOM_LEFT
        bottomRight -> Position.BOTTOM_RIGHT
        else -> null
    }

    private fun getZone(pane: JTabbedPane, p: Point): Zone? {
        val edge = minOf(80, pane.width / 4, pane.height / 4)
        val inContent = p.y > tabBarHeight(pane)
        val inBounds = p.x in 0..pane.width && p.y in 0..pane.height
        if (!inBounds || !inContent) return null

        val pos = getPosition(pane) ?: return null

        // Determine available zones based on position and what's empty
        val availableZones = mutableListOf<Zone>()

        when (pos) {
            Position.TOP_LEFT -> {
                if (topRight == null) availableZones.add(Zone.RIGHT)
                if (bottomLeft == null) availableZones.add(Zone.BOTTOM)
            }
            Position.TOP_RIGHT -> {
                if (topLeft == null) availableZones.add(Zone.LEFT)
                if (bottomRight == null) availableZones.add(Zone.BOTTOM)
            }
            Position.BOTTOM_LEFT -> {
                if (bottomRight == null) availableZones.add(Zone.RIGHT)
                if (topLeft == null) availableZones.add(Zone.TOP)
            }
            Position.BOTTOM_RIGHT -> {
                if (bottomLeft == null) availableZones.add(Zone.LEFT)
                if (topRight == null) availableZones.add(Zone.TOP)
            }
        }

        return when {
            Zone.LEFT in availableZones && p.x < edge -> Zone.LEFT
            Zone.RIGHT in availableZones && p.x > pane.width - edge -> Zone.RIGHT
            Zone.TOP in availableZones && p.y < tabBarHeight(pane) + edge -> Zone.TOP
            Zone.BOTTOM in availableZones && p.y > pane.height - edge -> Zone.BOTTOM
            else -> null
        }
    }

    private fun reorderTab(pane: JTabbedPane, from: Int, x: Int) {
        val to = getInsertIndex(pane, x)
        if (to != from && to != from + 1) {
            val (title, content) = pane.getTitleAt(from) to pane.getComponentAt(from)
            pane.removeTabAt(from)
            val newIdx = if (to > from) to - 1 else to
            pane.insertTab(title, null, content, null, newIdx)
            pane.selectedIndex = newIdx
        }
    }

    private fun moveTab(from: JTabbedPane, idx: Int, to: JTabbedPane) {
        val (title, content) = from.getTitleAt(idx) to from.getComponentAt(idx)
        from.removeTabAt(idx)
        to.addTab(title, content)
        to.selectedIndex = to.tabCount - 1
        if (from.tabCount == 0) removePane(from)
    }

    private fun splitTo(pane: JTabbedPane, idx: Int, zone: Zone) {
        val (title, content) = pane.getTitleAt(idx) to pane.getComponentAt(idx)
        pane.removeTabAt(idx)
        val newPane = JTabbedPane().also { it.addTab(title, content); panes.add(it); setupDrag(it) }

        val pos = getPosition(pane) ?: return
        val targetPos = getTargetPosition(pos, zone)

        // Set the new pane in its position
        when (targetPos) {
            Position.TOP_LEFT -> topLeft = newPane
            Position.TOP_RIGHT -> topRight = newPane
            Position.BOTTOM_LEFT -> bottomLeft = newPane
            Position.BOTTOM_RIGHT -> bottomRight = newPane
        }

        rebuildLayout()
    }

    private fun getTargetPosition(from: Position, zone: Zone): Position = when (from) {
        Position.TOP_LEFT -> when (zone) {
            Zone.RIGHT -> Position.TOP_RIGHT
            Zone.BOTTOM -> Position.BOTTOM_LEFT
            else -> Position.TOP_LEFT
        }
        Position.TOP_RIGHT -> when (zone) {
            Zone.LEFT -> Position.TOP_LEFT
            Zone.BOTTOM -> Position.BOTTOM_RIGHT
            else -> Position.TOP_RIGHT
        }
        Position.BOTTOM_LEFT -> when (zone) {
            Zone.RIGHT -> Position.BOTTOM_RIGHT
            Zone.TOP -> Position.TOP_LEFT
            else -> Position.BOTTOM_LEFT
        }
        Position.BOTTOM_RIGHT -> when (zone) {
            Zone.LEFT -> Position.BOTTOM_LEFT
            Zone.TOP -> Position.TOP_RIGHT
            else -> Position.BOTTOM_RIGHT
        }
    }

    private fun rebuildLayout() {
        // Remove old structure
        rootSplit?.let { remove(it) }
        topSplit?.let { remove(it) }
        bottomSplit?.let { remove(it) }
        topLeft?.let { remove(it) }
        topRight?.let { remove(it) }
        bottomLeft?.let { remove(it) }
        bottomRight?.let { remove(it) }
        rootSplit = null
        topSplit = null
        bottomSplit = null

        val hasTop = topLeft != null || topRight != null
        val hasBottom = bottomLeft != null || bottomRight != null
        val hasLeft = topLeft != null || bottomLeft != null
        val hasRight = topRight != null || bottomRight != null

        // Build top row
        val topComponent: Component? = when {
            topLeft != null && topRight != null -> {
                topSplit = JSplitPane(JSplitPane.HORIZONTAL_SPLIT, topLeft, topRight).apply {
                    resizeWeight = 0.5; dividerSize = 6
                }
                topSplit
            }
            topLeft != null -> topLeft
            topRight != null -> topRight
            else -> null
        }

        // Build bottom row
        val bottomComponent: Component? = when {
            bottomLeft != null && bottomRight != null -> {
                bottomSplit = JSplitPane(JSplitPane.HORIZONTAL_SPLIT, bottomLeft, bottomRight).apply {
                    resizeWeight = 0.5; dividerSize = 6
                }
                bottomSplit
            }
            bottomLeft != null -> bottomLeft
            bottomRight != null -> bottomRight
            else -> null
        }

        // Combine rows
        val mainComponent: Component = when {
            topComponent != null && bottomComponent != null -> {
                rootSplit = JSplitPane(JSplitPane.VERTICAL_SPLIT, topComponent, bottomComponent).apply {
                    resizeWeight = 0.5; dividerSize = 6
                }
                rootSplit!!
            }
            topComponent != null -> topComponent
            bottomComponent != null -> bottomComponent
            else -> mainPane // fallback
        }

        add(mainComponent, 0)
        setComponentZOrder(overlay, 0)
        revalidate()
        repaint()
    }

    private fun removePane(pane: JTabbedPane) {
        panes.remove(pane)
        when (pane) {
            topLeft -> topLeft = null
            topRight -> topRight = null
            bottomLeft -> bottomLeft = null
            bottomRight -> bottomRight = null
        }
        rebuildLayout()
    }

    // Window detach logic commented out per request
    // private fun popOut(pane: JTabbedPane, idx: Int, loc: Point) {
    //     val (title, content) = pane.getTitleAt(idx) to pane.getComponentAt(idx)
    //     pane.removeTabAt(idx)
    //     if (pane.tabCount == 0) removePane(pane)
    //
    //     JFrame(title).apply {
    //         defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
    //         contentPane.add(JButton("↩ Dock").apply {
    //             addActionListener { dispose(); mainPane.addTab(title, content); mainPane.selectedIndex = mainPane.tabCount - 1 }
    //         }, BorderLayout.NORTH)
    //         contentPane.add(content, BorderLayout.CENTER)
    //         setSize(500, 400)
    //         setLocation(loc.x - 50, loc.y - 20)
    //         isVisible = true
    //     }
    // }

    private enum class Zone { LEFT, RIGHT, TOP, BOTTOM }
    private enum class Position { TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT }

    private class Overlay : JComponent() {
        var zone: Rectangle? = null
        var insertLine: Rectangle? = null
        init { isOpaque = false }
        override fun paintComponent(g: Graphics) {
            val g2 = g as Graphics2D
            zone?.let { r ->
                g2.color = Color(0, 120, 215, 50)
                g2.fillRect(r.x, r.y, r.width, r.height)
                g2.color = Color(0, 120, 215, 180)
                g2.stroke = BasicStroke(2f)
                g2.drawRect(r.x + 1, r.y + 1, r.width - 3, r.height - 3)
            }
            insertLine?.let { r ->
                g2.color = Color(0, 120, 215)
                g2.fillRect(r.x, r.y, r.width, r.height)
            }
        }
    }
}