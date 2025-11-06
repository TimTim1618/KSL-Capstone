package gui;

import javax.swing.*;
import java.awt.*;

public class SidebarPanel extends JPanel {

    public SidebarPanel() {
        setLayout(new GridLayout(0, 1, 0, 10));
        setPreferredSize(new Dimension(200, 0));
        setBackground(new Color(240, 240, 240)); // subtle gray background

        JButton dashboardBtn = new JButton("Dashboard");
        JButton simulationBtn = new JButton("Simulation");

        // Optional: make buttons flat-looking
        dashboardBtn.setFocusPainted(false);
        simulationBtn.setFocusPainted(false);

        add(dashboardBtn);
        add(simulationBtn);

        // Example action listeners (assumes MainWindow instance available)
        dashboardBtn.addActionListener(e -> {
            JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            if (topFrame instanceof MainWindow mw) {
                mw.getWorkspacePanel().showPanel("Dashboard");
                mw.getLogPanel().log("Switched to Dashboard");
            }
        });

        simulationBtn.addActionListener(e -> {
            JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            if (topFrame instanceof MainWindow mw) {
                mw.getWorkspacePanel().showPanel("Simulation");
                mw.getLogPanel().log("Switched to Simulation");
            }
        });
    }
}
