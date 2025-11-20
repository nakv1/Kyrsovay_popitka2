package com.example.stego.net.server;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends JFrame {

    private static final int PORT = 12345;

    private final java.util.List<Socket> clients =
            java.util.Collections.synchronizedList(new java.util.ArrayList<>());

    private JTextArea logArea;

    public Server() {
        super("Image Server");
        setSize(500, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        logArea = new JTextArea();
        logArea.setEditable(false);
        add(new JScrollPane(logArea), BorderLayout.CENTER);

        // запуск сервера в отдельном потоке
        new Thread(this::startServer).start();
    }

    private void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            log("Сервер запущен на порту " + PORT + "...");
            while (true) {
                Socket socket = serverSocket.accept();
                log("Подключился клиент: " + socket.getInetAddress());
                clients.add(socket); // запоминаем клиента
                new Thread(() -> handleClient(socket)).start();
            }
        } catch (IOException e) {
            log("Ошибка сервера: " + e.getMessage());
        }
    }

    // приём изображений от одного клиента
    private void handleClient(Socket socket) {
        try (Socket s = socket;
             DataInputStream dis = new DataInputStream(new BufferedInputStream(s.getInputStream()))) {

            while (true) {
                int length = dis.readInt(); //  исключение
                if (length <= 0) {
                    log("Некорректная длина: " + length);
                    break;
                }
                byte[] bytes = new byte[length];
                dis.readFully(bytes);

                BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
                if (img == null) {
                    log("Изображение не распознано");
                    continue;
                }

                File dir = new File("downloads/server");
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                File out = new File(dir, "received_" + System.currentTimeMillis() + ".png");
                ImageIO.write(img, "png", out);
                log("Сохранено: " + out.getAbsolutePath());


                // рассылаем всем остальным клиентам
                broadcast(bytes, s);
            }
        } catch (EOFException eof) {
            log("Клиент завершил соединение");
        } catch (IOException e) {
            log("Клиентская ошибка: " + e.getMessage());
        } finally {
            clients.remove(socket);
            try { socket.close(); } catch (IOException ignored) {}
        }
    }

    // отправляем байты изображения всем подключенным, кроме отправителя
    private void broadcast(byte[] bytes, Socket from) {
        synchronized (clients) {
            java.util.Iterator<Socket> it = clients.iterator();
            while (it.hasNext()) {
                Socket c = it.next();
                if (c == from) continue;           // не отпр обратно отправителю
                if (c.isClosed()) { it.remove(); continue; }
                try {
                    DataOutputStream dos = new DataOutputStream(c.getOutputStream());
                    dos.writeInt(bytes.length);
                    dos.write(bytes);
                    dos.flush();
                } catch (IOException ex) {
                    try { c.close(); } catch (IOException ignored) {}
                    it.remove();
                }
            }
        }
    }

    private void log(String text) {
        SwingUtilities.invokeLater(() -> logArea.append(text + "\n"));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Server().setVisible(true));
    }
}
