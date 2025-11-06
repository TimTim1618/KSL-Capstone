package gui;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class WorkspacePanel extends JPanel {

    private CardLayout cardLayout;
    private Map<String, JPanel> panels;

    public WorkspacePanel() {
        cardLayout = new CardLayout();
        setLayout(cardLayout);

        panels = new HashMap<>();

        // Create default panels
        JPanel dashboardPanel = new JPanel();
        dashboardPanel.add(new JLabel("Dashboard Panel"));
        dashboardPanel.setBackground(Color.WHITE);

        JPanel simulationPanel = new JPanel();
        simulationPanel.add(new JLabel("Simulation Panel"));
        simulationPanel.setBackground(Color.LIGHT_GRAY);

        // Add panels to map and CardLayout
        add(dashboardPanel, "Dashboard");
        add(simulationPanel, "Simulation");

        panels.put("Dashboard", dashboardPanel);
        panels.put("Simulation", simulationPanel);

        // Show default panel
        showPanel("Dashboard");
    }

    public void showPanel(String name) {
        if (panels.containsKey(name)) {
            cardLayout.show(this, name);
        } else {
            System.out.println("Panel not found: " + name);
        }
    }

    // Optional helper to add new panels dynamically
    public void addPanel(String name, JPanel panel) {
        panels.put(name, panel);
        add(panel, name);
    }
}
