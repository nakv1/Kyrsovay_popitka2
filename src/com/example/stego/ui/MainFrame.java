package com.example.stego.ui;

import com.example.stego.net.server.ImageSocketUtils;
import com.example.stego.util.ImageUtils;
import com.example.stego.core.Steganography;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.Socket;
import java.io.IOException;


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
    private JButton connectButton;
    private JButton sendButton;

    private Socket socket;
    private String serverHost = "localhost";
    private int serverPort = 12345;


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

        //
        openButton.addActionListener(this::onOpen);
        saveButton.addActionListener(this::onSave);
        encodeButton.addActionListener(this::onEncode);
        decodeButton.addActionListener(this::onDecode);

        connectButton = new JButton("Подключиться");
        sendButton = new JButton("Отправить");
        topPanel.add(connectButton);
        topPanel.add(sendButton);

        connectButton.addActionListener(this::onConnect);
        sendButton.addActionListener(this::onSend);


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
    ///
    ///
    /// ДАЛЕЕ ВСЕ МЕТОДЫ ПРЕДНАЗНАЧЕНЫ ДЛЯ СЕРВЕРА
    ///
    ///
    ///
    private void onConnect(ActionEvent e) {
        String def = serverHost + ":" + serverPort;
        String s = JOptionPane.showInputDialog(this, "Введите IP:порт сервера", def);
        if (s == null || s.trim().isEmpty()) return;

        try {
            String[] parts = s.trim().split(":");
            serverHost = parts[0];
            serverPort = (parts.length > 1) ? Integer.parseInt(parts[1]) : 12345;

            socket = new Socket(serverHost, serverPort);
            JOptionPane.showMessageDialog(this, "Подключено к " + serverHost + ":" + serverPort);

            // запускаем фоновый приём картинок
            new Thread(this::listenIncomingImages).start();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Ошибка подключения: " + ex.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void onSend(ActionEvent e) {
        if (loadedImage == null) {
            JOptionPane.showMessageDialog(this, "Сначала загрузите изображение!", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            // если соединения нет или оно закрыто — создаём заново с последними host:port
            if (socket == null || socket.isClosed()) {
                socket = new Socket(serverHost, serverPort);
            }

            ImageSocketUtils.sendImage(socket.getOutputStream(), loadedImage);
            JOptionPane.showMessageDialog(this, "Изображение отправлено на сервер: " + serverHost + ":" + serverPort);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Ошибка при отправке: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            try { if (socket != null) socket.close(); } catch (IOException ignored) {}
            socket = null; // чтобы при следующем нажатии выполнилось переподключение
        }
    }
    private void listenIncomingImages() {
        if (socket == null) return;
        try (java.io.DataInputStream dis =
                     new java.io.DataInputStream(new java.io.BufferedInputStream(socket.getInputStream()))) {
            while (true) {
                BufferedImage img = ImageSocketUtils.receiveImage(dis); // читает [int][bytes]
                if (img == null) continue;
                File out = new File("downloaded_" + System.currentTimeMillis() + ".png");
                javax.imageio.ImageIO.write(img, "png", out);
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(this, "Получено изображение.\nСохранено: " + out.getAbsolutePath())
                );
            }
        } catch (IOException ex) {
            // соединение закрыто
        }
    }

}
