package com.example.stego.ui.panels;

import com.example.stego.core.Steganography;
import com.example.stego.ui.components.BlockPanel;
import com.example.stego.ui.components.LogPanel;
import com.example.stego.ui.components.PreviewPanel;
import com.example.stego.util.ImageUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

// Экран: Извлечение (шаги + предпросмотр + лог)
public class ExtractPanel extends JPanel {

    private final LogPanel logPanel;
    private final PreviewPanel previewPanel;

    private BufferedImage containerImage;
    private File containerFile;

    private JTextArea resultTextArea;
    private JTextField passwordField;

    public ExtractPanel() {
        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(new Color(12, 16, 24));

        JLabel header = new JLabel("Извлечение");
        header.setForeground(new Color(235, 240, 250));
        header.setFont(new Font("Segoe UI", Font.BOLD, 18));
        header.setBorder(new EmptyBorder(0, 0, 12, 0));
        add(header, BorderLayout.NORTH);

        // Центр: один общий скролл для всего содержимого
        JPanel mainGrid = new JPanel(new GridBagLayout());
        mainGrid.setOpaque(false);

        JScrollPane scroll = new JScrollPane(mainGrid);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        JPanel left = createLeftSteps();
        JPanel right = createRightSide();

        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;

        c.gridx = 0;
        c.weightx = 0.60;
        c.insets = new Insets(0, 0, 0, 12);
        mainGrid.add(left, c);

        c.gridx = 1;
        c.weightx = 0.40;
        c.insets = new Insets(0, 0, 0, 0);
        mainGrid.add(right, c);

        // Справа: (0) preview, (1) strut, (2) log
        previewPanel = (PreviewPanel) right.getComponent(0);
        logPanel = (LogPanel) right.getComponent(2);

        logPanel.log("Ожидание контейнера...");
        logPanel.setProgress(0);
    }

    // Левая колонка (шаги)

    private JPanel createLeftSteps() {
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        BlockPanel step1 = new BlockPanel("1. Загрузите контейнер");
        step1.getBody().add(createStep1Content(), BorderLayout.CENTER);
        fixBlock(step1);

        BlockPanel step2 = new BlockPanel("2. Параметры извлечения");
        step2.getBody().add(createStep2Content(), BorderLayout.CENTER);
        fixBlock(step2);

        BlockPanel step3 = new BlockPanel("3. Результат");
        step3.getBody().add(createStep3Content(), BorderLayout.CENTER);
        fixBlock(step3);

        left.add(step1);
        left.add(Box.createVerticalStrut(12));
        left.add(step2);
        left.add(Box.createVerticalStrut(12));
        left.add(step3);
        left.add(Box.createVerticalStrut(12));

        return left;
    }

    // Фиксация карточки: чтобы она не раздувалась по высоте на больших экранах
    private void fixBlock(BlockPanel block) {
        block.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Ставим "бесконечную" ширину, но высоту оставляем по preferred
        Dimension pref = block.getPreferredSize();
        if (pref == null) pref = new Dimension(10, 10);

        block.setMaximumSize(new Dimension(Integer.MAX_VALUE, pref.height));
    }

    // Правая колонка

    private JPanel createRightSide() {
        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));

        PreviewPanel preview = new PreviewPanel("Предпросмотр контейнера");
        preview.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Фиксируем высоту, чтобы не появлялся “внутренний скролл” и блоки не прыгали
        setFixedHeight(preview, 320);

        right.add(preview);
        right.add(Box.createVerticalStrut(12));

        LogPanel log = new LogPanel("Лог операций");
        log.setAlignmentX(Component.LEFT_ALIGNMENT);

        setFixedHeight(log, 260);

        right.add(log);

        // ВАЖНО: никакого VerticalGlue, иначе на больших окнах всё “тянет”
        return right;
    }

    private void setFixedHeight(JComponent comp, int h) {
        comp.setPreferredSize(new Dimension(10, h));
        comp.setMinimumSize(new Dimension(10, h));
        comp.setMaximumSize(new Dimension(Integer.MAX_VALUE, h));
    }

    //  Шаг 1

    private JComponent createStep1Content() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setOpaque(false);

        JLabel info = new JLabel("Выберите изображение (PNG/JPG)");
        info.setForeground(new Color(150, 160, 180));
        info.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        p.add(info, BorderLayout.NORTH);

        JButton btn = createMainButton("Выбрать контейнер...");
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btn.addActionListener(e -> chooseContainerImage());
        p.add(btn, BorderLayout.CENTER);

        return p;
    }

    private void chooseContainerImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Выберите изображение для извлечения");
        chooser.setFileFilter(new FileNameExtensionFilter(
                "Изображения (PNG, JPG, JPEG, BMP)", "png", "jpg", "jpeg", "bmp"
        ));

        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            logPanel.log("Выбор контейнера отменён");
            logPanel.setProgress(0);
            return;
        }

        File file = chooser.getSelectedFile();
        logPanel.log("Выбран контейнер: " + file.getName());

        try {
            BufferedImage img = ImageUtils.loadImage(file);

            containerFile = file;
            containerImage = img;

            previewPanel.setImage(containerImage);

            logPanel.log("Контейнер загружен: " + img.getWidth() + "x" + img.getHeight());
            logPanel.setProgress(30);

        } catch (IOException ex) {
            containerFile = null;
            containerImage = null;

            previewPanel.setPreviewText("Не удалось загрузить изображение");
            logPanel.log("Ошибка загрузки контейнера: " + ex.getMessage());
            logPanel.setProgress(0);

            JOptionPane.showMessageDialog(
                    this,
                    "Не удалось загрузить изображение.\n" + ex.getMessage(),
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    //  Шаг 2

    private JComponent createStep2Content() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        // Ряд “Пароль: [поле]” — фиксируем высоту, чтобы не раздувало
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel passLabel = new JLabel("Пароль:");
        passLabel.setForeground(new Color(150, 160, 180));
        passLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        row.add(passLabel, BorderLayout.WEST);

        passwordField = new JTextField();
        passwordField.setBackground(new Color(12, 16, 24));
        passwordField.setForeground(new Color(210, 215, 225));
        passwordField.setCaretColor(new Color(210, 215, 225));
        passwordField.setBorder(BorderFactory.createLineBorder(new Color(40, 55, 80)));
        passwordField.setPreferredSize(new Dimension(10, 32));
        passwordField.setMinimumSize(new Dimension(10, 32));
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

        row.add(passwordField, BorderLayout.CENTER);

        // Фикс высоты именно для row (иначе BorderLayout растянет по высоте)
        row.setPreferredSize(new Dimension(10, 32));
        row.setMinimumSize(new Dimension(10, 32));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

        p.add(row);
        p.add(Box.createVerticalStrut(12));

        JButton btn = createAccentButton("Извлечь");
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btn.addActionListener(e -> extractText());
        p.add(btn);

        // Чтобы блок не “резиновый” по высоте
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32 + 12 + 46 + 6));

        return p;
    }

    private void extractText() {
        if (containerImage == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Сначала выберите изображение-контейнер.",
                    "Нет контейнера",
                    JOptionPane.WARNING_MESSAGE
            );
            logPanel.log("Ошибка: контейнер не выбран");
            return;
        }

        String password = passwordField.getText();
        if (password == null) password = "";
        password = password.trim();

        if (!password.isEmpty()) {
            logPanel.log("Пароль введён, но пока не используется (шифрование добавим позже)");
        }

        logPanel.log("Запуск извлечения...");
        logPanel.setProgress(50);

        try {
            String text = Steganography.decode(containerImage);
            if (text == null) text = "";

            resultTextArea.setText(text);

            if (text.trim().isEmpty()) {
                logPanel.log("Готово: сообщение не найдено или пустое");
            } else {
                logPanel.log("Готово: сообщение извлечено (" + text.length() + " символов)");
            }

            logPanel.setProgress(100);

        } catch (Exception ex) {
            logPanel.log("Ошибка извлечения: " + ex.getMessage());
            logPanel.setProgress(0);

            JOptionPane.showMessageDialog(
                    this,
                    "Ошибка при извлечении:\n" + ex.getMessage(),
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    //Шаг 3

    private JComponent createStep3Content() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        JLabel info = new JLabel("Результат извлечения");
        info.setForeground(new Color(150, 160, 180));
        info.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        info.setAlignmentX(Component.LEFT_ALIGNMENT);

        p.add(info);
        p.add(Box.createVerticalStrut(10));

        resultTextArea = new JTextArea(8, 10);
        resultTextArea.setLineWrap(true);
        resultTextArea.setWrapStyleWord(true);
        resultTextArea.setEditable(false);
        resultTextArea.setBackground(new Color(12, 16, 24));
        resultTextArea.setForeground(new Color(210, 215, 225));
        resultTextArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        resultTextArea.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane scroll = new JScrollPane(resultTextArea);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(40, 55, 80)));
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Фиксируем высоту результата
        scroll.setPreferredSize(new Dimension(10, 220));
        scroll.setMinimumSize(new Dimension(10, 220));
        scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));

        p.add(scroll);
        p.add(Box.createVerticalStrut(10));

        JButton clearBtn = createMainButton("Очистить результат");
        clearBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        clearBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        clearBtn.addActionListener(e -> {
            resultTextArea.setText("");
            logPanel.log("Результат очищен");
            logPanel.setProgress(0);
        });

        p.add(clearBtn);

        return p;
    }

    // Кнопки

    private JButton createMainButton(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(true);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        b.setBackground(new Color(28, 38, 56));
        b.setForeground(new Color(230, 235, 245));
        b.setBorder(new EmptyBorder(12, 14, 12, 14));
        return b;
    }

    private JButton createAccentButton(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(true);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setBackground(new Color(240, 160, 20));
        b.setForeground(new Color(15, 18, 24));
        b.setBorder(new EmptyBorder(14, 14, 14, 14));
        return b;
    }
}
