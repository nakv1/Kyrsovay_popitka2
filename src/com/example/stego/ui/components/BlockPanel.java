package com.example.stego.ui.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

// Панель-блок с заголовком
public class BlockPanel extends JPanel {

    private final JPanel bodyPanel;

    public BlockPanel(String title) {
        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(new Color(18, 26, 40));
        setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(new Color(170, 180, 200));
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        add(titleLabel, BorderLayout.NORTH);

        bodyPanel = new JPanel(new BorderLayout());
        bodyPanel.setOpaque(false);
        bodyPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        add(bodyPanel, BorderLayout.CENTER);
    }

    public JPanel getBody() {
        return bodyPanel;
    }
}
