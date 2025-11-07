package com.example.stego.ui;

import javax.swing.*;

// Запуск приложения
public class Main {
    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

        });
    }
}
