package com.example.stego;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;

//Простой Swing-интерфейс GUI
// вОТ тут нужно будет порабоатть чтобы не был старперский стиль из 00-ых
// Я это взял с экзамена по яп 2 семака и изменил, посмотри че тут

public class MainFrame extends JFrame {
    private BufferedImage loadedImage;
    private JLabel imageInfoLabel;
    private JTextArea inputTextArea;
    private JTextArea outputTextArea;
    private JButton encodeButton;
    private JButton decodeButton;
    private JButton openButton;
    private JButton saveButton;

    public MainFrame() {
        super("StegoApp - LSB (BMP/PNG)");
        initUI();
    }

    private void initUI() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 600);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        openButton = new JButton("Открыть изображение");
        saveButton = new JButton("Сохранить (после кодирования)");
        imageInfoLabel = new JLabel("Файл не загружен");
        topPanel.add(openButton);
        topPanel.add(saveButton);
        topPanel.add(imageInfoLabel);
        add(topPanel, BorderLayout.NORTH);

        JSplitPane centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        JPanel left = new JPanel(new BorderLayout());
        left.add(new JLabel("Сообщение для встраивания:"), BorderLayout.NORTH);
        inputTextArea = new JTextArea();
        left.add(new JScrollPane(inputTextArea), BorderLayout.CENTER);

        JPanel right = new JPanel(new BorderLayout());
        right.add(new JLabel("Извлечённое сообщение:"), BorderLayout.NORTH);
        outputTextArea = new JTextArea();
        outputTextArea.setEditable(false);
        right.add(new JScrollPane(outputTextArea), BorderLayout.CENTER);

        centerSplit.setLeftComponent(left);
        centerSplit.setRightComponent(right);
        centerSplit.setDividerLocation(380);
        add(centerSplit, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        encodeButton = new JButton("Встроить");
        decodeButton = new JButton("Извлечь");
        bottom.add(encodeButton);
        bottom.add(decodeButton);
        add(bottom, BorderLayout.SOUTH);

        // Actions
        openButton.addActionListener(this::onOpen);
        saveButton.addActionListener(this::onSave);
        encodeButton.addActionListener(this::onEncode);
        decodeButton.addActionListener(this::onDecode);
    }

    private void onOpen(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Images (PNG, BMP)", "png", "bmp");
        chooser.setFileFilter(filter);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            try {
                loadedImage = ImageUtils.loadImage(f);
                imageInfoLabel.setText(String.format("Загружено: %s  (%dx%d)", f.getName(), loadedImage.getWidth(), loadedImage.getHeight()));
                JOptionPane.showMessageDialog(this, "Изображение загружено.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Ошибка загрузки: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                imageInfoLabel.setText("Файл не загружен");
            }
        }
    }

    private void onSave(ActionEvent e) {
        if (loadedImage == null) {
            JOptionPane.showMessageDialog(this, "Сначала загрузите/сгенерируйте изображение.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("stego.png"));
        FileNameExtensionFilter filter = new FileNameExtensionFilter("PNG or BMP", "png", "bmp");
        chooser.setFileFilter(filter);
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File out = chooser.getSelectedFile();
            String name = out.getName().toLowerCase();
            String format = name.endsWith(".bmp") ? "bmp" : "png"; // default png
            try {
                ImageUtils.saveImage(loadedImage, out, format);
                JOptionPane.showMessageDialog(this, "Сохранено в " + out.getAbsolutePath());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Ошибка сохранения: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onEncode(ActionEvent e) {
        if (loadedImage == null) {
            JOptionPane.showMessageDialog(this, "Сначала загрузите изображение.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String text = inputTextArea.getText();
        if (text == null) text = "";
        try {
            BufferedImage encoded = Steganography.encode(loadedImage, text);
            loadedImage = encoded; // заменяем текущую картинку на закодированную
            imageInfoLabel.setText(String.format("Изображение: (закодировано)  (%dx%d)", loadedImage.getWidth(), loadedImage.getHeight()));
            JOptionPane.showMessageDialog(this, "Сообщение встроено. Не забудьте сохранить изображение (кнопка Save).");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "Ошибка: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Непредвиденная ошибка: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onDecode(ActionEvent e) {
        if (loadedImage == null) {
            JOptionPane.showMessageDialog(this, "Сначала загрузите изображение.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            String decoded = Steganography.decode(loadedImage);
            outputTextArea.setText(decoded);
            JOptionPane.showMessageDialog(this, "Сообщение извлечено.");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "Ошибка: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Непредвиденная ошибка: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }
}
