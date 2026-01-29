package simulation.modelexe.runpanel

import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JProgressBar
import javax.swing.JTextArea
import javax.swing.JTextField
import javax.swing.SwingUtilities
import javax.swing.SwingWorker
import javax.swing.border.EmptyBorder
import javax.swing.border.TitledBorder

class RunPanel(
    private val service: KslRunService,
    private val consoleArea: JTextArea,
    private val errorArea: JTextArea,
) : JPanel(BorderLayout(10, 10)) {

    private val numRepsField = JTextField("20", 8)
    private val repLenField = JTextField("", 10)      // blank => don’t force
    private val warmUpField = JTextField("0.0", 10)
    private val outputDirField = JTextField("", 18)
    private val autoCsvBox = JCheckBox("auto CSV", false)
    private val finiteHorizonBox = JCheckBox("Finite horizon (stop when model ends)", true)

    private val statusLabel = JLabel("Status: Idle")
    private val progressBar = JProgressBar().apply {
        isIndeterminate = false
        value = 0
        isStringPainted = true
        string = "0/0"
        preferredSize = Dimension(280, 26)
    }

    private val runBtn = JButton("Run all")
    private val stopBtn = JButton("Stop").apply { isEnabled = false }
    private val stepBtn = JButton("Step").apply { isEnabled = false } // optional stub

    @Volatile private var handle: RunHandle? = null
    @Volatile private var worker: SwingWorker<Unit, Unit>? = null

    init {
        border = TitledBorder("Run Control and Progress")
        add(buildConfigPanel(), BorderLayout.NORTH)
        add(buildControlsPanel(), BorderLayout.CENTER)
        add(buildStatusPanel(), BorderLayout.SOUTH)
        wire()
    }

    private fun buildConfigPanel(): JComponent {
        val outer = JPanel(BorderLayout()).apply { border = EmptyBorder(10, 12, 10, 12) }

        val form = JPanel(GridBagLayout())
        val gbc = GridBagConstraints().apply {
            insets = Insets(6, 6, 6, 6)
            anchor = GridBagConstraints.EAST
            fill = GridBagConstraints.HORIZONTAL
            weightx = 1.0
            gridx = 0
            gridy = 0
        }

        fun row(label: String, field: JComponent) {
            gbc.gridx = 0; gbc.weightx = 0.0
            form.add(JLabel(label), gbc)
            gbc.gridx = 1; gbc.weightx = 1.0
            form.add(field, gbc)
            gbc.gridy++
        }

        row("Number of Repetitions", numRepsField)
        row("Repetition Length (optional)", repLenField)
        row("Warm-up", warmUpField)
        row("Output Dir (optional)", outputDirField)

        gbc.gridx = 1
        form.add(JPanel(FlowLayout(FlowLayout.LEFT, 8, 0)).apply {
            add(autoCsvBox)
            add(finiteHorizonBox)
        }, gbc)

        outer.add(form, BorderLayout.CENTER)
        return outer
    }

    private fun buildControlsPanel(): JComponent {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        panel.border = EmptyBorder(8, 12, 8, 12)

        val btnRow = JPanel(FlowLayout(FlowLayout.LEFT, 10, 4)).apply {
            add(runBtn); add(stepBtn); add(stopBtn)
        }

        val progRow = JPanel(FlowLayout(FlowLayout.LEFT, 10, 4)).apply {
            add(JLabel("Progress:"))
            add(progressBar)
        }

        panel.add(btnRow)
        panel.add(progRow)
        panel.add(Box.createVerticalGlue())
        return panel
    }

    private fun buildStatusPanel(): JComponent =
        JPanel(FlowLayout(FlowLayout.LEFT, 10, 6)).apply {
            border = EmptyBorder(0, 12, 10, 12)
            add(statusLabel)
        }

    private fun wire() {
        runBtn.addActionListener { startRun() }
        stopBtn.addActionListener { requestStop() }
        stepBtn.addActionListener { startStep() }
    }

    private fun startRun() {
        if (worker != null) return

        val cfg = readConfig()
        appendConsole("Starting run with config: $cfg")

        runBtn.isEnabled = false
        stopBtn.isEnabled = true
        setStatus("Starting…")
        setProgress(0, cfg.numReps)
        progressBar.isIndeterminate = true

        worker = object : SwingWorker<Unit, Unit>() {
            override fun doInBackground() {
                val cb = RunCallbacks(
                    onStatus = { SwingUtilities.invokeLater { setStatus(it) } },
                    onProgress = { rep, total -> SwingUtilities.invokeLater { setProgress(rep, total) } },
                    onConsole = { SwingUtilities.invokeLater { appendConsole(it) } },
                    onError = { SwingUtilities.invokeLater { appendError(it) } }
                )
                handle = service.runAll(cfg, cb)
            }

            override fun done() {
                progressBar.isIndeterminate = false
                stopBtn.isEnabled = false
                runBtn.isEnabled = true
                handle = null
                worker = null
            }
        }

        worker!!.execute()
    }

    private fun startStep() {
        appendConsole("Step pressed (not implemented).")
    }

    private fun requestStop() {
        appendConsole("Stop requested.")
        setStatus("Stopping…")
        handle?.requestStop()
    }

    private fun readConfig(): RunConfig {
        val numReps = numRepsField.text.trim().toIntOrNull() ?: 20
        val warmUp = warmUpField.text.trim().toDoubleOrNull() ?: 0.0
        val repLen = repLenField.text.trim().takeIf { it.isNotEmpty() }?.toDoubleOrNull()

        return RunConfig(
            numReps = numReps,
            repLength = repLen,
            warmUp = warmUp,
            outputDir = outputDirField.text.trim(),
            autoCsv = autoCsvBox.isSelected,
            finiteHorizon = finiteHorizonBox.isSelected
        )
    }

    private fun setStatus(s: String) {
        statusLabel.text = "Status: $s"
    }

    private fun setProgress(rep: Int, total: Int) {
        progressBar.isIndeterminate = false
        progressBar.maximum = maxOf(1, total)
        progressBar.value = rep.coerceIn(0, progressBar.maximum)
        progressBar.string = "$rep/$total"
    }

    private fun appendConsole(line: String) {
        consoleArea.append(line + "\n")
        consoleArea.caretPosition = consoleArea.document.length
    }

    private fun appendError(line: String) {
        errorArea.append(line + "\n")
        errorArea.caretPosition = errorArea.document.length
    }
}