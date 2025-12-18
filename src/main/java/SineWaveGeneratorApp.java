package com.example;

import javax.sound.sampled.LineUnavailableException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SineWaveGeneratorApp extends JFrame {
    private static final long serialVersionUID = 1L;

    // Components
    private JComboBox<String> waveformSelector;
    private JButton playButton;
    private JButton stopButton;
    private JPanel visualizationPanel;
    private JSlider frequencySlider;
    private JSlider amplitudeSlider;
    private JMenuBar menuBar;
    private JMenu fileMenu;
    private JMenuItem lightModeItem;
    private JMenuItem darkModeItem;

    // Audio properties
    private AudioGenerator audioGenerator;

    public SineWaveGeneratorApp() {
        audioGenerator = new AudioGenerator();
        initializeComponents();
        setupMenuBar();  // Move menu bar setup before event handlers
        setupLayout();
        setupEventHandlers();

        setTitle("Sine Wave Generator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null); // Center on screen
    }

    private void initializeComponents() {
        // Initialize waveform selector
        String[] waveforms = {"Sine", "Square", "Triangle", "Sawtooth"};
        waveformSelector = new JComboBox<>(waveforms);

        // Initialize buttons
        playButton = new JButton("Play");
        stopButton = new JButton("Stop");

        // Initialize sliders
        frequencySlider = new JSlider(JSlider.HORIZONTAL, 50, 2000, 440); // Default to A4 note
        frequencySlider.setMajorTickSpacing(400);
        frequencySlider.setMinorTickSpacing(100);
        frequencySlider.setPaintTicks(true);
        frequencySlider.setPaintLabels(true);

        amplitudeSlider = new JSlider(JSlider.HORIZONTAL, 1, 100, 50);
        amplitudeSlider.setMajorTickSpacing(25);
        amplitudeSlider.setMinorTickSpacing(5);
        amplitudeSlider.setPaintTicks(true);
        amplitudeSlider.setPaintLabels(true);

        // Initialize visualization panel
        visualizationPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawWaveform(g);
            }
        };
        visualizationPanel.setBackground(Color.WHITE);
        visualizationPanel.setBorder(BorderFactory.createEtchedBorder());
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Top controls panel
        JPanel controlsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0; gbc.gridy = 0;
        controlsPanel.add(new JLabel("Waveform:"), gbc);

        gbc.gridx = 1;
        controlsPanel.add(waveformSelector, gbc);

        gbc.gridx = 2;
        controlsPanel.add(playButton, gbc);

        gbc.gridx = 3;
        controlsPanel.add(stopButton, gbc);

        gbc.gridx = 4;
        controlsPanel.add(new JLabel("Frequency (Hz):"), gbc);

        gbc.gridx = 5;
        frequencySlider.setPreferredSize(new Dimension(150, 40));
        controlsPanel.add(frequencySlider, gbc);

        gbc.gridx = 6;
        controlsPanel.add(new JLabel("Amplitude:"), gbc);

        gbc.gridx = 7;
        amplitudeSlider.setPreferredSize(new Dimension(150, 40));
        controlsPanel.add(amplitudeSlider, gbc);

        // Visualization panel
        visualizationPanel.setPreferredSize(new Dimension(800, 300));

        // Add components to frame
        add(controlsPanel, BorderLayout.NORTH);
        add(new JScrollPane(visualizationPanel), BorderLayout.CENTER);
    }

    private void setupMenuBar() {
        menuBar = new JMenuBar();

        // File menu
        fileMenu = new JMenu("File");
        fileMenu.setMnemonic('F');

        // Theme submenu
        JMenu themeMenu = new JMenu("Theme");
        themeMenu.setMnemonic('T');

        lightModeItem = new JMenuItem("Light");
        darkModeItem = new JMenuItem("Dark");

        themeMenu.add(lightModeItem);
        themeMenu.add(darkModeItem);
        fileMenu.add(themeMenu);

        menuBar.add(fileMenu);
        setJMenuBar(menuBar);
    }

    private void setupEventHandlers() {
        playButton.addActionListener(e -> playSound());
        stopButton.addActionListener(e -> stopSound());

        // Update audio parameters when sliders change
        frequencySlider.addChangeListener(e -> {
            if (audioGenerator != null) {
                audioGenerator.setFrequency(frequencySlider.getValue());
            }
        });

        amplitudeSlider.addChangeListener(e -> {
            if (audioGenerator != null) {
                audioGenerator.setAmplitude(amplitudeSlider.getValue());
            }
        });

        waveformSelector.addActionListener(e -> {
            if (audioGenerator != null) {
                audioGenerator.setWaveformType((String) waveformSelector.getSelectedItem());
            }
        });

        // Theme selection handlers
        lightModeItem.addActionListener(e -> setLightMode());
        darkModeItem.addActionListener(e -> setDarkMode());
    }

    private void playSound() {
        audioGenerator.setFrequency(frequencySlider.getValue());
        audioGenerator.setAmplitude(amplitudeSlider.getValue());
        audioGenerator.setWaveformType((String) waveformSelector.getSelectedItem());
        audioGenerator.startPlayback();

        playButton.setEnabled(false);
        stopButton.setEnabled(true);
        waveformSelector.setEnabled(false);
    }

    private void stopSound() {
        audioGenerator.stopPlayback();

        playButton.setEnabled(true);
        stopButton.setEnabled(false);
        waveformSelector.setEnabled(true);
    }

    private void drawWaveform(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = visualizationPanel.getWidth();
        int height = visualizationPanel.getHeight();

        // Draw grid
        g2d.setColor(Color.LIGHT_GRAY);
        for (int i = 0; i < width; i += 50) {
            g2d.drawLine(i, 0, i, height);
        }
        for (int i = 0; i < height; i += 50) {
            g2d.drawLine(0, i, width, i);
        }

        // Draw center line
        g2d.setColor(Color.GRAY);
        g2d.drawLine(0, height/2, width, height/2);

        // Draw waveform based on selection
        String selectedWaveform = (String) waveformSelector.getSelectedItem();
        g2d.setColor(Color.RED);
        g2d.setStroke(new BasicStroke(2f));

        int centerY = height / 2;
        int amplitude = amplitudeSlider.getValue() * 2; // Adjust for visual purposes

        // Generate points for the waveform
        int[] xPoints = new int[width];
        int[] yPoints = new int[width];

        double frequency = frequencySlider.getValue();
        double scale = width / 20.0; // Scale factor to see multiple cycles

        for (int x = 0; x < width; x++) {
            xPoints[x] = x;

            // Calculate corresponding y position
            double angle = (x / scale) * frequency * Math.PI * 2 / 440.0; // Normalize against A4

            double y = 0;
            switch (selectedWaveform) {
                case "Sine":
                    y = Math.sin(angle) * amplitude;
                    break;
                case "Square":
                    y = (Math.sin(angle) >= 0 ? 1 : -1) * amplitude;
                    break;
                case "Triangle":
                    y = (2 / Math.PI) * Math.asin(Math.sin(angle)) * amplitude;
                    break;
                case "Sawtooth":
                    y = ((angle / Math.PI) % 2 - 1) * amplitude;
                    break;
            }

            yPoints[x] = centerY - (int) y;
        }

        // Draw the waveform
        for (int i = 1; i < width; i++) {
            g2d.drawLine(xPoints[i-1], yPoints[i-1], xPoints[i], yPoints[i]);
        }
    }

    private void setLightMode() {
        try {
            UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
            SwingUtilities.updateComponentTreeUI(this);
            // Also update the visualization panel's appearance
            visualizationPanel.setBackground(UIManager.getColor("Panel.background"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setDarkMode() {
        try {
            UIManager.setLookAndFeel("com.formdev.flatlaf.FlatDarkLaf");
            SwingUtilities.updateComponentTreeUI(this);
            // Also update the visualization panel's appearance
            visualizationPanel.setBackground(UIManager.getColor("Panel.background"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            new SineWaveGeneratorApp().setVisible(true);
        });
    }
}