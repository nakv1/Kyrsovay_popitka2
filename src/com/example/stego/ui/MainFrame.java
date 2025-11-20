package com.example.stego.ui;

import com.example.stego.core.Steganography;
import com.example.stego.net.server.ImageSocketUtils;
import com.example.stego.util.ImageUtils;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;

// Простой GUI для стеганографии:
// одна вкладка: встроить / извлечь
// предпросмотр изображения (контейнер и stego)
// отдельные поля для текста: встраиваемый и извлечённый
// лог операций
// опционально: отправка/приём изображений по сети

public class MainFrame extends JFrame {

    // Текущее исходное изображение
    private BufferedImage containerImage;
    // Текущее стеганографическое изображение (после встраивания)
    private BufferedImage stegoImage;
    // Файл, из которого загружена картиника
    private File currentFile;

    //  Верхняя панель + сеть
    private JButton btnOpen;
    private JButton btnSave;
    private JLabel lblFileInfo;
    private JLabel lblCapacity;
    private JButton btnClear;

    private JTextField txtHost;
    private JTextField txtPort;

    private JButton btnConnect;
    private JButton btnDisconnect;
    private JButton btnSend;


    private Socket socket;
    private DataOutputStream netOut;



    // Центр: предпросмотр + текст
    // Предпросмотр файла (до встраивания)
    private JLabel lblContainerPreview;
    // Предпросмотр stego-изображения (после встраивания)
    private JLabel lblStegoPreview;

    // Текст для встраивания
    private JTextArea txtEmbedMessage;
    // Текст, извлечённый из изображения
    private JTextArea txtExtractedMessage;

    //   Низ: лог
    private JTextArea txtLog;

    // Инт гл
    public MainFrame() {
        super("StegoTool");

        initComponents();
        initLayout();
        initActions();

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1200, 800));
        setLocationRelativeTo(null);
    }

    // Инициализация компонентов (создание)
    private void initComponents() {
        // верх
        btnOpen = new JButton("Выбрать файл...");
        btnSave = new JButton("Сохранить как...");
        btnSave.setEnabled(false);
        btnClear = new JButton("Очистить");

        lblFileInfo = new JLabel("Файл не загружен");
        lblCapacity = new JLabel("Вместимость: —");

        txtHost = new JTextField("127.0.0.1", 10);
        txtPort = new JTextField("", 5);
        btnConnect = new JButton("Подключиться");
        btnDisconnect = new JButton("Отключиться");
        btnDisconnect.setEnabled(false);

        btnSend = new JButton("Отправить изображение");
        btnSend.setEnabled(false);

        // предпросмотр файла
        lblContainerPreview = new JLabel("Нет изображения", SwingConstants.CENTER);
        lblContainerPreview.setBorder(new TitledBorder("Контейнер (до)"));

        // предпросмотр stego
        lblStegoPreview = new JLabel("Нет stego", SwingConstants.CENTER);
        lblStegoPreview.setBorder(new TitledBorder("Stego (после)"));

        // текст для встраивания
        txtEmbedMessage = new JTextArea(6, 30);
        txtEmbedMessage.setLineWrap(true);
        txtEmbedMessage.setWrapStyleWord(true);

        // текст, извлечённый из изображения
        txtExtractedMessage = new JTextArea(6, 30);
        txtExtractedMessage.setLineWrap(true);
        txtExtractedMessage.setWrapStyleWord(true);
        txtExtractedMessage.setEditable(false);

        // лог
        txtLog = new JTextArea(5, 80);
        txtLog.setEditable(false);
    }

    // Компоновка
    private void initLayout() {
        setLayout(new BorderLayout(8, 8));

        //     верх
        JPanel topPanel = new JPanel(new BorderLayout(8, 8));

        JPanel filePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filePanel.add(btnOpen);
        filePanel.add(btnSave);
        filePanel.add(btnClear);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.add(lblFileInfo);
        infoPanel.add(lblCapacity);

        JPanel netPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        netPanel.add(new JLabel("Host:"));
        netPanel.add(txtHost);
        netPanel.add(new JLabel("Port:"));
        netPanel.add(txtPort);
        netPanel.add(btnConnect);
        netPanel.add(btnDisconnect);
        netPanel.add(btnSend);

        topPanel.add(filePanel, BorderLayout.WEST);
        topPanel.add(infoPanel, BorderLayout.CENTER);
        topPanel.add(netPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // --- центр
        // слева: предпросмотр (до / после)
        JPanel previewsPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        previewsPanel.add(lblContainerPreview);
        previewsPanel.add(lblStegoPreview);

        // справа: два текстовых поля (встроить / извлечённое) + кнопки
        JPanel rightPanel = new JPanel(new BorderLayout(8, 8));

        JPanel textsPanel = new JPanel(new GridLayout(2, 1, 5, 5));

        JPanel embedPanel = new JPanel(new BorderLayout());
        embedPanel.setBorder(new TitledBorder("Секретный текст (для встраивания)"));
        embedPanel.add(new JScrollPane(txtEmbedMessage), BorderLayout.CENTER);

        JPanel extractedPanel = new JPanel(new BorderLayout());
        extractedPanel.setBorder(new TitledBorder("Извлечённый текст"));
        extractedPanel.add(new JScrollPane(txtExtractedMessage), BorderLayout.CENTER);

        textsPanel.add(embedPanel);
        textsPanel.add(extractedPanel);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnDecode = new JButton("Извлечь");
        JButton btnEncode = new JButton("Встроить");
        buttonsPanel.add(btnDecode);
        buttonsPanel.add(btnEncode);

        rightPanel.add(textsPanel, BorderLayout.CENTER);
        rightPanel.add(buttonsPanel, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                previewsPanel,
                rightPanel
        );
        splitPane.setResizeWeight(0.4);

        add(splitPane, BorderLayout.CENTER);

        // низ: лог
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(new TitledBorder("Лог операций"));
        bottomPanel.add(new JScrollPane(txtLog), BorderLayout.CENTER);

        add(bottomPanel, BorderLayout.SOUTH);

        // привязка обработчиков кнопок encode/decode
        btnEncode.addActionListener(this::onEncode);
        btnDecode.addActionListener(this::onDecode);
    }

    // Привязка обработчиков к остальным кнопкам
    private void initActions() {
        btnOpen.addActionListener(this::onOpen);
        btnSave.addActionListener(this::onSave);
        btnClear.addActionListener(this::onClear);

        btnConnect.addActionListener(this::onConnect);
        btnDisconnect.addActionListener(this::onDisconnect);

        btnSend.addActionListener(this::onSend);
    }

    //     Обработчики

    // Открыть файл
    private void onOpen(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter(
                "Изображения (PNG, BMP)", "png", "bmp"
        ));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File f = chooser.getSelectedFile();
        try {
            containerImage = ImageUtils.loadImage(f);
            stegoImage = null;
            currentFile = f;

            updatePreview();        // обновляем предпросмотр файла
            updateCapacityLabel();  // пересчёт вместимости

            lblFileInfo.setText(String.format(
                    "Снимок: %s (%d×%d)",
                    f.getName(),
                    containerImage.getWidth(),
                    containerImage.getHeight()
            ));

            // stego-превью ОЧИСТКА
            lblStegoPreview.setIcon(null);
            lblStegoPreview.setText("Нет stego");

            log("Файл загружен: " + f.getAbsolutePath());
            btnSave.setEnabled(false);   // пока нет stego
            btnSend.setEnabled(containerImage != null && socket != null && socket.isConnected());

        } catch (IOException ex) {
            showError("Ошибка загрузки: " + ex.getMessage());
            log("Ошибка загрузки: " + ex.getMessage());
        }
    }

    // Сохранить stego-изображение
    private void onSave(ActionEvent e) {
        if (stegoImage == null) {
            showError("Нет стего-файла для сохранения.");
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter(
                "PNG изображение", "png"
        ));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File f = chooser.getSelectedFile();
        // добавим .png, если нет
        if (!f.getName().toLowerCase().endsWith(".png")) {
            f = new File(f.getParentFile(), f.getName() + ".png");
        }
        try {
            ImageUtils.saveImage(stegoImage, f, "png");
            log("Stego-изображение сохранено: " + f.getAbsolutePath());
            JOptionPane.showMessageDialog(this,
                    "Изображение сохранено:\n" + f.getAbsolutePath(),
                    "Сохранение", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            showError("Ошибка сохранения: " + ex.getMessage());
            log("Ошибка сохранения: " + ex.getMessage());
        }
    }

    // Встроить текст
    private void onEncode(ActionEvent e) {
        if (containerImage == null) {
            showError("Сначала загрузите файл.");
            return;
        }
        String message = txtEmbedMessage.getText();
        if (message == null || message.isEmpty()) {
            showError("Введите секретный текст.");
            return;
        }
        try {
            // Steganography
            stegoImage = Steganography.encode(containerImage, message);
            updatePreviewStego(); // обновляем предпросмотр stego
            btnSave.setEnabled(true);
            btnSend.setEnabled(socket != null && socket.isConnected());

            log("Сообщение успешно встроено ("
                    + message.length() + " символов).");
            JOptionPane.showMessageDialog(this,
                    "Сообщение успешно встроено.",
                    "Готово", JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
            log("Ошибка встраивания: " + ex.getMessage());
        } catch (Exception ex) {
            showError("Ошибка встраивания: " + ex.getMessage());
            log("Ошибка встраивания: " + ex);
        }
    }

    // Извлечь текст
    private void onDecode(ActionEvent e) {
        if (containerImage == null && stegoImage == null) {
            showError("Нет изображения для извлечения.");
            return;
        }
        BufferedImage img = (stegoImage != null) ? stegoImage : containerImage;
        try {
            String message = Steganography.decode(img);
            txtExtractedMessage.setText(message); // выводим в отдельное поле
            log("Сообщение извлечено (" + message.length() + " символов).");
            JOptionPane.showMessageDialog(this,
                    "Сообщение извлечено.",
                    "Готово", JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
            log("Ошибка извлечения: " + ex.getMessage());
        } catch (Exception ex) {
            showError("Ошибка извлечения: " + ex.getMessage());
            log("Ошибка извлечения: " + ex);
        }
    }

    // Подключиться к серверу
    private void onConnect(ActionEvent e) {
        if (socket != null && socket.isConnected()) {
            // уже подключены
            showInfo("Уже подключено.");
            return;
        }
        String host = txtHost.getText().trim();
        int port;
        try {
            port = Integer.parseInt(txtPort.getText().trim());
        } catch (NumberFormatException ex) {
            showError("Некорректный порт.");
            return;
        }
        try {
            socket = new Socket(host, port);
            netOut = new DataOutputStream(socket.getOutputStream());
            btnSend.setEnabled(containerImage != null || stegoImage != null);
            btnDisconnect.setEnabled(true);

            log("Подключено к " + host + ":" + port);
            showInfo("Соединение установлено.");

            // отдельный поток для приёма изображений
            Thread t = new Thread(this::receiveLoop, "ImageReceiver");
            t.setDaemon(true);
            t.start();
        } catch (IOException ex) {
            showError("Не удалось подключиться: " + ex.getMessage());
            log("Ошибка подключения: " + ex.getMessage());
        }
    }
    private void onDisconnect(ActionEvent e) {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ex) {
            log("Ошибка при отключении: " + ex.getMessage());
        }

        socket = null;
        netOut = null;

        btnSend.setEnabled(false);
        btnDisconnect.setEnabled(false);

        log("Клиент отключился от сервера.");
        showInfo("Отключено.");
    }

    // Отправить изображение на сервер
    private void onSend(ActionEvent e) {
        if (socket == null || !socket.isConnected()) {
            showError("Сначала подключитесь к серверу.");
            return;
        }
        BufferedImage img = (stegoImage != null) ? stegoImage : containerImage;
        if (img == null) {
            showError("Нет изображения для отправки.");
            return;
        }
        try {
            ImageSocketUtils.sendImage(netOut, img);
            log("Изображение отправлено на сервер.");
        } catch (IOException ex) {
            showError("Ошибка отправки: " + ex.getMessage());
            log("Ошибка отправки: " + ex.getMessage());
        }
    }

    // Цикл приёма изображений с сервера
    private void receiveLoop() {
        try {
            InputStream in = socket.getInputStream();
            while (!socket.isClosed()) {
                BufferedImage img = ImageSocketUtils.receiveImage(in);
                if (img == null) continue;

                // сохраняем в downloads
                File dir = new File("downloads/client");
                if (!dir.exists())
                    dir.mkdirs();
                File out = new File(dir,"downloaded_" + System.currentTimeMillis() + ".png");
                javax.imageio.ImageIO.write(img, "png", out);

                log("Получено изображение от сервера. Сохранено: " + out.getAbsolutePath());
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(
                            this,
                            "Получено изображение.\nСохранено:\n" + out.getAbsolutePath(),
                            "Получено", JOptionPane.INFORMATION_MESSAGE
                    );
                });
            }
        } catch (IOException ex) {
            log("Соединение закрыто: " + ex.getMessage());
        }
    }


    //  Вспомогательные методы

    // Обновляем предпросмотр контейнера (до)
    private void updatePreview() {
        if (containerImage == null) {
            lblContainerPreview.setIcon(null);
            lblContainerPreview.setText("Нет изображения");
            return;
        }
        lblContainerPreview.setText(null);
        lblContainerPreview.setIcon(new ImageIcon(
                containerImage.getScaledInstance(
                        lblContainerPreview.getWidth(),
                        lblContainerPreview.getHeight(),
                        Image.SCALE_SMOOTH
                )
        ));
    }

    // Обновляем предпросмотр stego (после)
    private void updatePreviewStego() {
        if (stegoImage == null) {
            lblStegoPreview.setIcon(null);
            lblStegoPreview.setText("Нет stego");
            return;
        }
        lblStegoPreview.setText(null);
        lblStegoPreview.setIcon(new ImageIcon(
                stegoImage.getScaledInstance(
                        lblStegoPreview.getWidth(),
                        lblStegoPreview.getHeight(),
                        Image.SCALE_SMOOTH
                )
        ));
    }

    private void updateCapacityLabel() {
        if (containerImage == null) {
            lblCapacity.setText("Вместимость: —");
            return;
        }
        int width = containerImage.getWidth();
        int height = containerImage.getHeight();
        int bits = width * height * 3;      // 1 младший бит на каждый RGB-канал
        int totalBytes = bits / 8;
        int payload = totalBytes - 4;       // 4 байта на длину
        if (payload < 0) payload = 0;
        lblCapacity.setText("Вместимость: примерно " + payload + " байт");
    }

    private void log(String text) {
        SwingUtilities.invokeLater(() -> {
            txtLog.append(text + "\n");
            txtLog.setCaretPosition(txtLog.getDocument().getLength());
        });
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Ошибка", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfo(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Информация", JOptionPane.INFORMATION_MESSAGE);
    }

    // Очистка картинок и состояний
    private void onClear(ActionEvent e) {
        containerImage = null;
        stegoImage = null;
        currentFile = null;

        lblContainerPreview.setIcon(null);
        lblContainerPreview.setText("Нет изображения");

        lblStegoPreview.setIcon(null);
        lblStegoPreview.setText("Нет stego");

        lblFileInfo.setText("Файл не загружен");
        lblCapacity.setText("Вместимость: —");

        btnSave.setEnabled(false);
        btnSend.setEnabled(false);

        log("Изображения очищены.");

    }
}
