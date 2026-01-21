package com.example.stego.ui.panels;

import com.example.stego.net.server.ImageSocketUtils;
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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

// Экран: Передача (клиент к серверу) + получение последнего изображения
public class TransferPanel extends JPanel {

    private final LogPanel logPanel;
    private final PreviewPanel previewPanel;

    private JTextField hostField;
    private JTextField portField;

    private JButton connectBtn;
    private JButton disconnectBtn;
    private JButton sendBtn;

    private volatile Socket socket;
    private volatile boolean receiverRunning;

    public TransferPanel() {
        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(new Color(12, 16, 24));

        JLabel header = new JLabel("Передача");
        header.setForeground(new Color(235, 240, 250));
        header.setFont(new Font("Segoe UI", Font.BOLD, 18));
        header.setBorder(new EmptyBorder(0, 0, 12, 0));
        add(header, BorderLayout.NORTH);

        // Один общий скролл, как мы сделали в ExtractPanel
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

        setConnected(false);
        logPanel.log("Готово. Укажи host/port и подключись.");
        logPanel.setProgress(0);
    }

    // Левая часть (шаги)

    private JPanel createLeftSteps() {
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        BlockPanel step1 = new BlockPanel("1. Подключение");
        step1.getBody().add(createConnectContent(), BorderLayout.CENTER);
        fixBlock(step1);

        BlockPanel step2 = new BlockPanel("2. Отправка изображения");
        step2.getBody().add(createSendContent(), BorderLayout.CENTER);
        fixBlock(step2);

        left.add(step1);
        left.add(Box.createVerticalStrut(12));
        left.add(step2);
        left.add(Box.createVerticalStrut(12));

        return left;
    }

    private void fixBlock(BlockPanel block) {
        block.setAlignmentX(Component.LEFT_ALIGNMENT);
        Dimension pref = block.getPreferredSize();
        if (pref == null) pref = new Dimension(10, 10);
        block.setMaximumSize(new Dimension(Integer.MAX_VALUE, pref.height));
    }

    // Правая часть (preview + log)

    private JPanel createRightSide() {
        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));

        PreviewPanel preview = new PreviewPanel("Последнее полученное изображение");
        preview.setAlignmentX(Component.LEFT_ALIGNMENT);
        setFixedHeight(preview, 360);

        right.add(preview);
        right.add(Box.createVerticalStrut(12));

        LogPanel log = new LogPanel("Лог операций");
        log.setAlignmentX(Component.LEFT_ALIGNMENT);
        setFixedHeight(log, 260);

        right.add(log);

        // ВАЖНО: без VerticalGlue, чтобы не тянуло блоки
        return right;
    }

    private void setFixedHeight(JComponent comp, int h) {
        comp.setPreferredSize(new Dimension(10, h));
        comp.setMinimumSize(new Dimension(10, h));
        comp.setMaximumSize(new Dimension(Integer.MAX_VALUE, h));
    }

    // шаг 1 (подключение)

    private JComponent createConnectContent() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        hostField = new JTextField("127.0.0.1");
        portField = new JTextField("12345");

        JPanel hostRow = createFieldRow("Host:", hostField);
        JPanel portRow = createFieldRow("Port:", portField);

        p.add(hostRow);
        p.add(Box.createVerticalStrut(10));
        p.add(portRow);
        p.add(Box.createVerticalStrut(12));

        connectBtn = createAccentButton("Подключиться");
        connectBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        connectBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        connectBtn.addActionListener(e -> connect());

        disconnectBtn = createMainButton("Отключиться");
        disconnectBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        disconnectBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        disconnectBtn.addActionListener(e -> disconnect());

        p.add(connectBtn);
        p.add(Box.createVerticalStrut(8));
        p.add(disconnectBtn);

        // чтобы блок не был резиновым по высоте на больших экранах
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32 + 10 + 32 + 12 + 46 + 8 + 42 + 10));

        return p;
    }

    private JPanel createFieldRow(String label, JTextField field) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel l = new JLabel(label);
        l.setForeground(new Color(150, 160, 180));
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        row.add(l, BorderLayout.WEST);

        field.setBackground(new Color(12, 16, 24));
        field.setForeground(new Color(210, 215, 225));
        field.setCaretColor(new Color(210, 215, 225));
        field.setBorder(BorderFactory.createLineBorder(new Color(40, 55, 80)));
        field.setPreferredSize(new Dimension(10, 32));
        field.setMinimumSize(new Dimension(10, 32));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

        row.add(field, BorderLayout.CENTER);

        // фикс высоты строки, чтобы BorderLayout не раздувал
        row.setPreferredSize(new Dimension(10, 32));
        row.setMinimumSize(new Dimension(10, 32));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

        return row;
    }

    // шаг 2 (отправка)

    private JComponent createSendContent() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        JLabel info = new JLabel("Отправка PNG через сервер (всем клиентам)");
        info.setForeground(new Color(150, 160, 180));
        info.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        info.setAlignmentX(Component.LEFT_ALIGNMENT);

        p.add(info);
        p.add(Box.createVerticalStrut(12));

        sendBtn = createAccentButton("Выбрать и отправить...");
        sendBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        sendBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        sendBtn.addActionListener(e -> chooseAndSend());

        p.add(sendBtn);

        // фиксируем адекватную высоту шага
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 13 + 12 + 46 + 12));

        return p;
    }

    // Логика подключения

    private void connect() {
        if (socket != null && socket.isConnected() && !socket.isClosed()) {
            logPanel.log("Уже подключено");
            return;
        }

        String host = hostField.getText().trim();
        String portText = portField.getText().trim();

        int port;
        try {
            port = Integer.parseInt(portText);
        } catch (Exception ex) {
            logPanel.log("Ошибка: порт должен быть числом");
            JOptionPane.showMessageDialog(this, "Порт должен быть числом", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        logPanel.log("Подключение к " + host + ":" + port + " ...");
        logPanel.setProgress(10);

        try {
            socket = new Socket(host, port);
            setConnected(true);

            logPanel.log("Подключено успешно");
            logPanel.setProgress(30);

            startReceiverThread();

        } catch (Exception ex) {
            socket = null;
            setConnected(false);
            logPanel.log("Ошибка подключения: " + ex.getMessage());
            logPanel.setProgress(0);

        }
    }

    private void disconnect() {
        logPanel.log("Отключение...");
        logPanel.setProgress(0);

        receiverRunning = false;

        try {
            if (socket != null) {
                socket.close();
            }
        } catch (Exception ignored) {
        }

        socket = null;
        setConnected(false);

        logPanel.log("Отключено");
        logPanel.setProgress(0);
    }

    private void setConnected(boolean connected) {
        if (connectBtn != null) connectBtn.setEnabled(!connected);
        if (disconnectBtn != null) disconnectBtn.setEnabled(connected);
        if (sendBtn != null) sendBtn.setEnabled(connected);

        if (hostField != null) hostField.setEnabled(!connected);
        if (portField != null) portField.setEnabled(!connected);
    }

    // ===== Приём изображений в фоне =====

    private void startReceiverThread() {
        receiverRunning = true;

        Thread t = new Thread(() -> {
            try {
                InputStream in = socket.getInputStream();

                while (receiverRunning && socket != null && socket.isConnected() && !socket.isClosed()) {
                    BufferedImage img = ImageSocketUtils.receiveImage(in);
                    if (img == null) continue;

                    BufferedImage finalImg = img;
                    SwingUtilities.invokeLater(() -> {
                        previewPanel.setImage(finalImg);
                        logPanel.log("Получено изображение: " + finalImg.getWidth() + "x" + finalImg.getHeight());
                        logPanel.setProgress(100);
                    });
                }

            } catch (Exception ex) {
                if (receiverRunning) {
                    SwingUtilities.invokeLater(() -> {
                        logPanel.log("Приём остановлен: " + ex.getMessage());
                        logPanel.setProgress(0);
                    });
                }
            } finally {
                SwingUtilities.invokeLater(() -> {
                    socket = null;
                    setConnected(false);
                });
            }
        });

        t.setName("transfer-receiver");
        t.setDaemon(true);
        t.start();
    }

    // Отправка

    private void chooseAndSend() {
        if (socket == null || socket.isClosed() || !socket.isConnected()) {
            logPanel.log("Ошибка: нет подключения");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Выберите PNG для отправки");
        chooser.setFileFilter(new FileNameExtensionFilter("PNG изображение", "png"));

        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            logPanel.log("Выбор файла отменён");
            return;
        }

        File file = chooser.getSelectedFile();

        try {
            BufferedImage img = ImageUtils.loadImage(file);

            // покажем у себя (чтобы было наглядно)
            previewPanel.setImage(img);

            logPanel.log("Отправка: " + file.getName());
            logPanel.setProgress(40);

            OutputStream out = socket.getOutputStream();
            ImageSocketUtils.sendImage(out, img);

            logPanel.log("Отправлено успешно (" + img.getWidth() + "x" + img.getHeight() + ")");
            logPanel.setProgress(100);

        } catch (IOException ex) {
            logPanel.log("Ошибка отправки: " + ex.getMessage());
            logPanel.setProgress(0);

            JOptionPane.showMessageDialog(
                    this,
                    "Ошибка отправки:\n" + ex.getMessage(),
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE
            );
        } catch (Exception ex) {
            logPanel.log("Ошибка: " + ex.getMessage());
            logPanel.setProgress(0);
        }
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
