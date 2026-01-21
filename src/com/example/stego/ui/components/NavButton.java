package com.example.stego.ui.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

// Кнопка навигации слева (умеет быть активной)
public class NavButton extends JButton {

    private boolean active;

    private final Color bgNormal = new Color(28, 38, 56);
    private final Color bgHover = new Color(35, 48, 72);
    private final Color bgActive = new Color(240, 160, 20);

    private final Color fgNormal = new Color(230, 235, 245);
    private final Color fgActive = new Color(15, 18, 24);

    public NavButton(String text) {
        super(text);

        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(true);

        setHorizontalAlignment(SwingConstants.LEFT);
        setFont(new Font("Segoe UI", Font.PLAIN, 14));

        setBorder(new EmptyBorder(12, 14, 12, 14));
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        setBackground(bgNormal);
        setForeground(fgNormal);

        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (!active) {
                    setBackground(bgHover);
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                updateColors();
            }
        });
    }

    public void setActive(boolean value) {
        active = value;
        updateColors();
    }

    public boolean isActive() {
        return active;
    }

    private void updateColors() {
        if (active) {
            setBackground(bgActive);
            setForeground(fgActive);
        } else {
            setBackground(bgNormal);
            setForeground(fgNormal);
        }
    }
}
