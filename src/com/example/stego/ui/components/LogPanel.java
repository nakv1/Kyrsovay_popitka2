package com.example.stego.ui.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

// Панель лога операций + прогресс
public class LogPanel extends JPanel {

    private final JTextArea logArea;
    private final JProgressBar progressBar;

    public LogPanel(String title) {
        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(new Color(18, 26, 40));
        setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(new Color(170, 180, 200));
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        add(titleLabel, BorderLayout.NORTH);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setBackground(new Color(12, 16, 24));
        logArea.setForeground(new Color(210, 215, 225));
        logArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        logArea.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane scroll = new JScrollPane(logArea);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        add(scroll, BorderLayout.CENTER);

        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setBorder(new EmptyBorder(8, 0, 0, 0));
        add(progressBar, BorderLayout.SOUTH);
    }

    public void log(String text) {
        logArea.append(text + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    public void setProgress(int value) {
        progressBar.setValue(value);
    }

    public void clear() {
        logArea.setText("");
        progressBar.setValue(0);
    }
}
