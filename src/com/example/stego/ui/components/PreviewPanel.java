package com.example.stego.ui.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;

// Панель предпросмотра изображения
public class PreviewPanel extends JPanel {

    private final JLabel imageLabel;
    private BufferedImage originalImage;

    public PreviewPanel(String title) {
        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(new Color(18, 26, 40));
        setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(new Color(170, 180, 200));
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        add(titleLabel, BorderLayout.NORTH);

        imageLabel = new JLabel("Загрузите изображение для предпросмотра", SwingConstants.CENTER);
        imageLabel.setForeground(new Color(140, 150, 170));
        imageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        imageLabel.setBorder(new EmptyBorder(12, 12, 12, 12));
        add(imageLabel, BorderLayout.CENTER);

        imageLabel.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                updatePreviewIcon();
            }
        });
    }

    public void setPreviewText(String text) {
        originalImage = null;
        imageLabel.setText(text);
        imageLabel.setIcon(null);
    }

    public void setImage(BufferedImage image) {
        originalImage = image;
        imageLabel.setText("");
        updatePreviewIcon();
    }

    private void updatePreviewIcon() {
        if (originalImage == null) {
            return;
        }

        int w = imageLabel.getWidth();
        int h = imageLabel.getHeight();

        if (w <= 0 || h <= 0) {
            return;
        }

        Image scaled = scaleToFit(originalImage, w - 10, h - 10);
        imageLabel.setIcon(new ImageIcon(scaled));
    }

    private Image scaleToFit(BufferedImage img, int maxW, int maxH) {
        int w = img.getWidth();
        int h = img.getHeight();

        if (w <= 0 || h <= 0) {
            return img;
        }

        double kW = (double) maxW / (double) w;
        double kH = (double) maxH / (double) h;
        double k = Math.min(kW, kH);

        if (k > 1.0) {
            k = 1.0;
        }

        int newW = Math.max(1, (int) (w * k));
        int newH = Math.max(1, (int) (h * k));

        return img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
    }
}
