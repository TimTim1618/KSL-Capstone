package gui;

import com.formdev.flatlaf.FlatLightLaf;

import java.io.File;
import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame {

    private WorkspacePanel workspacePanel;
    private LogPanel logPanel;

    public MainWindow() {
        // check for uploaded files previously sent by students
        File storageDir = new File("uploaded_models");
        if (!storageDir.exists()) {
            storageDir.mkdir();
        }

        // Initialize Look & Feel
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }

        setTitle("Arena-Style GUI");
        setSize(1200, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Menu Bar
        setJMenuBar(new MenuBar(this));

        // Sidebar + Workspace
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(new SidebarPanel(), BorderLayout.WEST);

        workspacePanel = new WorkspacePanel();
        mainPanel.add(workspacePanel, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);

        // Log Panel at the bottom
        logPanel = new LogPanel();
        add(logPanel, BorderLayout.SOUTH);
    }

    public WorkspacePanel getWorkspacePanel() {
        return workspacePanel;
    }

    public LogPanel getLogPanel() {
        return logPanel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainWindow window = new MainWindow();
            window.setVisible(true);
        });
    }
}
