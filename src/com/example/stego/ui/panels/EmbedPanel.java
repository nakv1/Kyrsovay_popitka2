package com.example.stego.ui.panels;

import com.example.stego.core.Steganography;
import com.example.stego.ui.components.BlockPanel;
import com.example.stego.ui.components.DualPreviewPanel;
import com.example.stego.ui.components.LogPanel;
import com.example.stego.util.ImageUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

// Экран: Встраивание (шаги + предпросмотр ДО/ПОСЛЕ + лог)
public class EmbedPanel extends JPanel {

    private final LogPanel logPanel;
    private final DualPreviewPanel previewPanel;

    private BufferedImage containerImage;
    private File containerFile;

    private JTextArea secretTextArea;
    private JTextField passwordField;

    public EmbedPanel() {
        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(new Color(12, 16, 24));

        JLabel header = new JLabel("Встраивание");
        header.setForeground(new Color(235, 240, 250));
        header.setFont(new Font("Segoe UI", Font.BOLD, 18));
        header.setBorder(new EmptyBorder(0, 0, 12, 0));
        add(header, BorderLayout.NORTH);

        JPanel mainGrid = new JPanel(new GridBagLayout());
        mainGrid.setOpaque(false);
        add(mainGrid, BorderLayout.CENTER);

        JComponent left = createLeftSteps();      // со скроллом
        JPanel right = createRightSide();         // предпросмотр + лог

        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;

        c.gridx = 0;
        c.weightx = 0.58;
        c.insets = new Insets(0, 0, 0, 12);
        mainGrid.add(left, c);

        c.gridx = 1;
        c.weightx = 0.42;
        c.insets = new Insets(0, 0, 0, 0);
        mainGrid.add(right, c);

        // Справа: (0) preview, (1) strut, (2) log
        previewPanel = (DualPreviewPanel) right.getComponent(0);
        logPanel = (LogPanel) right.getComponent(2);

        previewPanel.clearAll();

        logPanel.log("Ожидание контейнера...");
        logPanel.setProgress(0);
    }

    //Левая часть (шаги)

    private JComponent createLeftSteps() {
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        BlockPanel step1 = new BlockPanel("1. Загрузите контейнер");
        step1.getBody().add(createStep1Content(), BorderLayout.CENTER);
        fixBlock(step1);

        BlockPanel step2 = new BlockPanel("2. Укажите секретные данные");
        step2.getBody().add(createStep2Content(), BorderLayout.CENTER);
        fixBlock(step2);

        BlockPanel step3 = new BlockPanel("3. Настройте параметры");
        step3.getBody().add(createStep3Content(), BorderLayout.CENTER);
        fixBlock(step3);

        left.add(step1);
        left.add(Box.createVerticalStrut(12));
        left.add(step2);
        left.add(Box.createVerticalStrut(12));
        left.add(step3);
        left.add(Box.createVerticalStrut(12));

        // Не добавляем VerticalGlue, чтобы блоки не растягивались “в пустоту”
        JScrollPane scroll = new JScrollPane(left);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        return scroll;
    }

    private void fixBlock(BlockPanel block) {
        block.setAlignmentX(Component.LEFT_ALIGNMENT);

        Dimension pref = block.getPreferredSize();
        if (pref == null) pref = new Dimension(10, 10);

        block.setMaximumSize(new Dimension(Integer.MAX_VALUE, pref.height));
    }

    // Правая часть

    private JPanel createRightSide() {
        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));

        DualPreviewPanel preview = new DualPreviewPanel();
        preview.setAlignmentX(Component.LEFT_ALIGNMENT);
        preview.setPreferredSize(new Dimension(10, 520));
        preview.setMaximumSize(new Dimension(Integer.MAX_VALUE, 10000));

        right.add(preview);
        right.add(Box.createVerticalStrut(12));

        LogPanel log = new LogPanel("Лог операций");
        log.setAlignmentX(Component.LEFT_ALIGNMENT);
        log.setPreferredSize(new Dimension(10, 240));
        log.setMaximumSize(new Dimension(Integer.MAX_VALUE, 10000));

        right.add(log);

        return right;
    }

    // Шаг 1

    private JComponent createStep1Content() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setOpaque(false);

        JLabel info = new JLabel("Выберите изображение-контейнер (PNG/JPG)");
        info.setForeground(new Color(150, 160, 180));
        info.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        p.add(info, BorderLayout.NORTH);

        JButton btn = createMainButton("Выбрать контейнер...");
        btn.addActionListener(e -> chooseContainerImage());
        p.add(btn, BorderLayout.CENTER);

        return p;
    }

    private void chooseContainerImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Выберите изображение-контейнер");
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

            previewPanel.setOriginalImage(containerImage);
            previewPanel.clearResult();

            logPanel.log("Контейнер загружен: " + img.getWidth() + "x" + img.getHeight());
            logPanel.setProgress(30);

        } catch (IOException ex) {
            containerFile = null;
            containerImage = null;

            previewPanel.clearAll();

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

    // Шаг 2

    private JComponent createStep2Content() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setOpaque(false);

        JLabel info = new JLabel("Введите текст для скрытия");
        info.setForeground(new Color(150, 160, 180));
        info.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        p.add(info, BorderLayout.NORTH);

        // Сделал пониже (чтобы реально было “шире вниз”)
        secretTextArea = new JTextArea(12, 10);
        secretTextArea.setLineWrap(true);
        secretTextArea.setWrapStyleWord(true);
        secretTextArea.setBackground(new Color(12, 16, 24));
        secretTextArea.setForeground(new Color(210, 215, 225));
        secretTextArea.setCaretColor(new Color(210, 215, 225));
        secretTextArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        secretTextArea.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane scroll = new JScrollPane(secretTextArea);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(40, 55, 80)));
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        // Чуть фиксируем минимальную высоту, чтобы было “вниз”
        scroll.setPreferredSize(new Dimension(10, 260));

        p.add(scroll, BorderLayout.CENTER);

        return p;
    }

    // Шаг 3

    private JComponent createStep3Content() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel passLabel = new JLabel("Пароль шифрования:");
        passLabel.setForeground(new Color(150, 160, 180));
        passLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        row.add(passLabel, BorderLayout.WEST);

        passwordField = new JTextField();
        passwordField.setBackground(new Color(12, 16, 24));
        passwordField.setForeground(new Color(210, 215, 225));
        passwordField.setCaretColor(new Color(210, 215, 225));
        passwordField.setBorder(BorderFactory.createLineBorder(new Color(40, 55, 80)));
        passwordField.setPreferredSize(new Dimension(10, 32));

        row.add(passwordField, BorderLayout.CENTER);

        p.add(row);
        p.add(Box.createVerticalStrut(12));

        JButton btn = createAccentButton("Встроить и сохранить");
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btn.addActionListener(e -> embedAndSave());
        p.add(btn);

        return p;
    }

    private void embedAndSave() {
        if (containerImage == null) {
            JOptionPane.showMessageDialog(this, "Сначала выберите изображение-контейнер.", "Нет контейнера", JOptionPane.WARNING_MESSAGE);
            logPanel.log("Ошибка: контейнер не выбран");
            return;
        }

        String text = secretTextArea.getText();
        if (text == null) text = "";
        text = text.trim();

        if (text.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Введите текст, который нужно скрыть.", "Нет текста", JOptionPane.WARNING_MESSAGE);
            logPanel.log("Ошибка: секретный текст пустой");
            return;
        }

        String password = passwordField.getText();
        if (password == null) password = "";
        password = password.trim();

        if (!password.isEmpty()) {
            logPanel.log("Пароль введён, но пока не используется (шифрование добавим позже)");
        }

        logPanel.log("Запуск встраивания...");
        logPanel.setProgress(40);

        try {
            BufferedImage resultImage = Steganography.encode(containerImage, text);

            logPanel.log("Встраивание выполнено успешно");
            logPanel.setProgress(70);

            File outFile = chooseSaveFile();
            if (outFile == null) {
                logPanel.log("Сохранение отменено");
                logPanel.setProgress(30);
                return;
            }

            ImageUtils.saveImage(resultImage, outFile, "png");

            // Показываем результат в нижнем предпросмотре
            previewPanel.setResultImage(resultImage);

            logPanel.log("Файл сохранён: " + outFile.getName());
            logPanel.setProgress(100);

            JOptionPane.showMessageDialog(
                    this,
                    "Готово! Файл сохранён:\n" + outFile.getAbsolutePath(),
                    "Успех",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (Exception ex) {
            logPanel.log("Ошибка встраивания: " + ex.getMessage());
            logPanel.setProgress(0);

            JOptionPane.showMessageDialog(
                    this,
                    "Ошибка при встраивании:\n" + ex.getMessage(),
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private File chooseSaveFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Сохранить результат");
        chooser.setFileFilter(new FileNameExtensionFilter("PNG изображение (*.png)", "png"));

        String defaultName = "stego_result.png";
        if (containerFile != null) {
            String name = containerFile.getName();
            int dot = name.lastIndexOf('.');
            if (dot > 0) name = name.substring(0, dot);
            defaultName = name + "_stego.png";
        }
        chooser.setSelectedFile(new File(defaultName));

        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return null;
        }

        File file = chooser.getSelectedFile();
        String path = file.getAbsolutePath();
        if (!path.toLowerCase().endsWith(".png")) {
            file = new File(path + ".png");
        }

        return file;
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
