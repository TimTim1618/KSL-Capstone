package gui

import java.awt.*
import javax.swing.*
import javax.swing.border.EmptyBorder

fun main() {
    SwingUtilities.invokeLater {
        ModelFrame().isVisible = true
    }
}

class ModelFrame : JFrame("Model Executor") {
    init{
        defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        minimumSize = Dimension(1200, 720)
        setLocationRelativeTo(null)

        contentPane.layout = BorderLayout(8, 8)

        (contentPane as JComponent).border = EmptyBorder(8, 8, 8, 8)

        contentPane.add(buildNorthHeader(), BorderLayout.NORTH)
    }

    private fun buildNorthHeader():JComponent {
        val north = JPanel()
        north.layout = BoxLayout(north, BoxLayout.Y_AXIS)

        //info bar inforamation

        val infoBar = JPanel(GridBagLayout()).apply {
            border = EmptyBorder(2, 2, 2, 2)
        }

        val gbc = GridBagConstraints().apply {
            insets = Insets(2, 4, 2, 4)
            fill = GridBagConstraints.HORIZONTAL
            weighty = 0.0
            gridy = 0
        }

        fun addLabeledField(label: String, widthPx: Int): JTextField {
            val field = JTextField().apply{
                preferredSize = Dimension(widthPx, 32)
                minimumSize =  Dimension(widthPx, 32)
                maximumSize =  Dimension(widthPx, 32)
            }

            val panel = JPanel(FlowLayout(FlowLayout.LEFT, 6, 0)).apply {
                add(JLabel(label))
                add(field)
            }

            gbc.gridx = infoBar.componentCount
            gbc.weightx = 0.0
            infoBar.add(panel, gbc)

            return field
        }

        addLabeledField("Model:", 100)
        addLabeledField("Label:", 100)
        addLabeledField("Experiment:", 120)
        addLabeledField("Time Unit:", 90)
        addLabeledField("Default Stream:", 100)

        val attachBtn = JButton("Attach DB...").apply {
            preferredSize = Dimension(130, 26)
            isFocusable = false
            addActionListener {
                JOptionPane.showMessageDialog(this@ModelFrame, "Attach DB clicked (placeholder)")
            }
        }

        gbc.gridx = infoBar.componentCount
        gbc.weightx = 1.0
        infoBar.add(Box.createHorizontalGlue(), gbc)

        gbc.gridx - infoBar.componentCount
        gbc.weightx = 0.0
        infoBar.add(attachBtn, gbc)

        //menu row stuff

        val menuRow = JToolBar().apply {
            isFloatable = false
            layout = FlowLayout(FlowLayout.LEFT, 10, 8)   // spacing between buttons + row padding
            border = BorderFactory.createEmptyBorder(2, 6, 2, 6)
        }

        fun menuButton(text: String, onClick: () -> Unit) {
            menuRow.add(JButton(text).apply {
                isFocusable = false
                font = font.deriveFont(Font.PLAIN, 15f)
                val h = 20
                val minW = 100
                val w = maxOf(minW, preferredSize.width + 20)
                preferredSize = Dimension(w, h)
                minimumSize   = Dimension(w, h)
                maximumSize   = Dimension(w, h)
                margin = Insets(6, 12, 6, 12)
                addActionListener { onClick() }
            })
        }


        menuButton("File") {println("File clicked")}
        menuButton("Edit") {println("Edit clicked")}
        menuButton("Options") {println("Options clicked")}
        menuButton("Help") {
            JOptionPane.showMessageDialog(this@ModelFrame, "Help clicked (placeholder)")
        }
        menuButton("Input analyzer") { println("Input analyzer clicked")}
        menuButton("Output analyzer") { println("Output analyzer clicked")}

        //asseblt the top
        north.add(infoBar)
        north.add(menuRow)

        return north

    }
}

