package gui;

import simulation.ModelRunner;
import simulation.SimulationInput;
import simulation.SimulationOutput;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;

public class MenuBar extends JMenuBar {

    private File currentProject = null; // Track the currently opened project
    private final JLabel projectLabel;  // Label for current project display

    public MenuBar(MainWindow window) {

        // --- Create uploaded_models folder ---
        File storageDir = new File("uploaded_models");
        if (!storageDir.exists()) storageDir.mkdir();

        // === PROJECT LABEL (Centered Title) ===
        projectLabel = new JLabel("No Project Open", SwingConstants.CENTER);
        projectLabel.setForeground(Color.LIGHT_GRAY);
        projectLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

        // === FILE MENU (Styled Red) ===
        JMenu fileMenu = new JMenu("File") {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(200, 0, 0));
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(Color.WHITE);
                g.drawString(getText(), 10, getHeight() - 7);
            }
        };
        fileMenu.setForeground(Color.WHITE);

        // --- New Project (.kt) ---
        JMenuItem newProject = new JMenuItem("New Project (.kt)");
        newProject.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser("src/main/kotlin/studentfiles");
            chooser.setDialogTitle("Select a Model (.kt)");
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Kotlin Files", "kt"));

            int result = chooser.showOpenDialog(window);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = chooser.getSelectedFile();
                try {
                    File destFile = new File(storageDir, selectedFile.getName());
                    if (destFile.exists()) {
                        String newName = System.currentTimeMillis() + "_" + selectedFile.getName();
                        destFile = new File(storageDir, newName);
                    }

                    Files.copy(selectedFile.toPath(), destFile.toPath());
                    setCurrentProject(destFile);

                    JOptionPane.showMessageDialog(window,
                            "Project added successfully and ready to run!",
                            "Project Ready",
                            JOptionPane.INFORMATION_MESSAGE);

                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(window,
                            "Error processing the file:\n" + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // --- Upload Project (.jar) ---
        JMenuItem uploadProject = new JMenuItem("Upload Project (.jar)");
        uploadProject.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Select Simulation Model (.jar)");
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Java Archive Files", "jar"));

            int result = chooser.showOpenDialog(window);
            if (result == JFileChooser.APPROVE_OPTION) {
                File jarFile = chooser.getSelectedFile();
                try {
                    File destFile = new File(storageDir, jarFile.getName());
                    if (destFile.exists()) {
                        String newName = System.currentTimeMillis() + "_" + jarFile.getName();
                        destFile = new File(storageDir, newName);
                    }

                    Files.copy(jarFile.toPath(), destFile.toPath());
                    setCurrentProject(destFile);

                    JOptionPane.showMessageDialog(window,
                            "Project uploaded successfully and ready to run!",
                            "Project Ready",
                            JOptionPane.INFORMATION_MESSAGE);

                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(window,
                            "Error processing the file:\n" + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // --- Recent Projects ---
        JMenu recentProjects = new JMenu("Recent Projects");
        refreshRecentProjects(window, recentProjects, storageDir);

        // --- Close Project ---
        JMenuItem closeProject = new JMenuItem("Close Project");
        closeProject.addActionListener(e -> {
            if (currentProject != null) {
                currentProject = null;
                projectLabel.setText("No Project Open");
                projectLabel.setForeground(Color.LIGHT_GRAY);
                JOptionPane.showMessageDialog(window,
                        "Project closed.",
                        "Closed",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(window,
                        "No project is currently open.",
                        "Info",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        // --- Exit ---
        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener(e -> System.exit(0));

        fileMenu.add(newProject);
        fileMenu.add(uploadProject);
        fileMenu.add(recentProjects);
        fileMenu.add(closeProject);
        fileMenu.addSeparator();
        fileMenu.add(exit);

        // === INPUT ANALYZER MENU ===
        JMenu inputMenu = new JMenu("Input Analyzer");
        JMenuItem importData = new JMenuItem("Import Data");
        JMenuItem runAnalysis = new JMenuItem("Run Analysis");
        inputMenu.add(importData);
        inputMenu.add(runAnalysis);

        // === ANIMATION MENU ===
        JMenu animationMenu = new JMenu("Animation");
        JMenuItem startAnim = new JMenuItem("Start");
        JMenuItem stopAnim = new JMenuItem("Stop");
        animationMenu.add(startAnim);
        animationMenu.add(stopAnim);

        // === HELP MENU ===
        JMenu helpMenu = new JMenu("Help");

        JMenuItem kslDocs = new JMenuItem("Open KSL Documentation");
        kslDocs.addActionListener(e -> {
            try {
                Desktop.getDesktop().browse(new java.net.URI("https://rossetti.github.io/KSLBook/"));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(window,
                        "Unable to open KSL documentation.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });

        JMenuItem about = new JMenuItem("About Arena GUI");
        about.addActionListener(e -> JOptionPane.showMessageDialog(window,
                "Arena-Style GUI\nCreated by Team Low-Taper",
                "About",
                JOptionPane.INFORMATION_MESSAGE));

        helpMenu.add(kslDocs);
        helpMenu.addSeparator();
        helpMenu.add(about);

        // === Add all menus ===
        add(fileMenu);
        add(inputMenu);
        add(animationMenu);
        add(helpMenu);

        // Add spacing and center project name
        add(Box.createHorizontalGlue());
        add(projectLabel);

        setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
    }

    /** Updates the label and sets the current project */
    private void setCurrentProject(File file) {
        currentProject = file;
        projectLabel.setText("Current Project: " + file.getName());
        projectLabel.setForeground(new Color(0, 200, 0));
    }

    /** Refresh recent projects menu dynamically */
    private void refreshRecentProjects(MainWindow window, JMenu recentMenu, File storageDir) {
        recentMenu.removeAll();

        File[] projectFiles = storageDir.listFiles((dir, name) ->
                name.toLowerCase().endsWith(".jar") || name.toLowerCase().endsWith(".kt"));

        if (projectFiles == null || projectFiles.length == 0) {
            recentMenu.add(new JMenuItem("No recent projects"));
            return;
        }

        Arrays.sort(projectFiles, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));

        for (File file : projectFiles) {
            JMenuItem item = new JMenuItem(file.getName());
            item.addActionListener(e -> setCurrentProject(file));
            recentMenu.add(item);
        }
    }
}
