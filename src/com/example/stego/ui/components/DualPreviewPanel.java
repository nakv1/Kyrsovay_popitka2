package com.example.stego.ui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

// Два предпросмотра друг под другом: Оригинал / Результат
public class DualPreviewPanel extends JPanel {

    private final PreviewPanel original;
    private final PreviewPanel result;

    public DualPreviewPanel() {
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        original = new PreviewPanel("Оригинал");
        result = new PreviewPanel("Результат");

        original.setAlignmentX(Component.LEFT_ALIGNMENT);
        result.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Одинаковая высота, чтобы смотрелось ровно
        setFixedHeight(original, 210);
        setFixedHeight(result, 210);

        add(original);
        add(Box.createVerticalStrut(12));
        add(result);
    }

    private void setFixedHeight(JComponent comp, int h) {
        comp.setPreferredSize(new Dimension(10, h));
        comp.setMinimumSize(new Dimension(10, h));
        comp.setMaximumSize(new Dimension(Integer.MAX_VALUE, h));
    }

    public void setOriginalImage(BufferedImage img) {
        original.setImage(img);
    }

    public void setResultImage(BufferedImage img) {
        result.setImage(img);
    }

    public void clearResult() {
        result.setPreviewText("Результат появится после встраивания");
    }

    public void clearAll() {
        original.setPreviewText("Загрузите изображение для предпросмотра");
        result.setPreviewText("Результат появится после встраивания");
    }
}
