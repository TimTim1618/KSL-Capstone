package gui;

import javax.swing.*;
import java.awt.*;

public class LogPanel extends JPanel {

    private JTextArea logArea;

    public LogPanel() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(0, 150)); // height 150px

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(logArea);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void log(String message) {
        logArea.append(message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
}
