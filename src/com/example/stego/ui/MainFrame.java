package com.example.stego.ui;

import com.example.stego.ui.components.NavButton;
import com.example.stego.ui.panels.EmbedPanel;
import com.example.stego.ui.panels.ExtractPanel;
import com.example.stego.ui.panels.TransferPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

// Главное окно приложения
public class MainFrame extends JFrame {

    private final CardLayout cardLayout;
    private final JPanel contentPanel;

    private NavButton btnEmbed;
    private NavButton btnExtract;
    private NavButton btnTransfer;

    public MainFrame() {
        super("StegoTool by Nak");

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1366, 768);
        setMinimumSize(new Dimension(1100, 650));

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(12, 16, 24));
        setContentPane(root);

        JPanel navPanel = createNavPanel();
        root.add(navPanel, BorderLayout.WEST);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setOpaque(true);
        contentPanel.setBackground(new Color(12, 16, 24));
        contentPanel.setBorder(new EmptyBorder(16, 16, 16, 16));
        root.add(contentPanel, BorderLayout.CENTER);

        contentPanel.add(new EmbedPanel(), "embed");
        contentPanel.add(new ExtractPanel(), "extract");
        contentPanel.add(new TransferPanel(), "transfer");

        showScreen("embed");

        setLocationRelativeTo(null);
    }

    private JPanel createNavPanel() {
        JPanel nav = new JPanel();
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));

        // Навигация чуть уже (чтобы больше места было контенту)
        nav.setPreferredSize(new Dimension(185, 10));
        nav.setMinimumSize(new Dimension(170, 10));
        nav.setMaximumSize(new Dimension(200, Integer.MAX_VALUE));

        nav.setBackground(new Color(16, 22, 34));
        nav.setBorder(new EmptyBorder(16, 14, 16, 14));

        JLabel title = new JLabel("Навигация");
        title.setForeground(new Color(170, 180, 200));
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        nav.add(title);

        nav.add(Box.createVerticalStrut(12));

        btnEmbed = new NavButton("Встраивание");
        prepareNavButton(btnEmbed);
        btnEmbed.addActionListener(e -> showScreen("embed"));
        nav.add(btnEmbed);

        nav.add(Box.createVerticalStrut(8));

        btnExtract = new NavButton("Извлечение");
        prepareNavButton(btnExtract);
        btnExtract.addActionListener(e -> showScreen("extract"));
        nav.add(btnExtract);

        nav.add(Box.createVerticalStrut(8));

        btnTransfer = new NavButton("Передача");
        prepareNavButton(btnTransfer);
        btnTransfer.addActionListener(e -> showScreen("transfer"));
        nav.add(btnTransfer);

        nav.add(Box.createVerticalGlue());

        JLabel footer = new JLabel("Версия: 2.0");
        footer.setForeground(new Color(130, 140, 160));
        footer.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);
        nav.add(footer);

        return nav;
    }

    private void prepareNavButton(NavButton b) {
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42)); // было 46, стало компактнее
        b.setMinimumSize(new Dimension(10, 42));
        b.setPreferredSize(new Dimension(10, 42));
    }

    private void showScreen(String key) {
        cardLayout.show(contentPanel, key);
        updateActiveNav(key);
    }

    private void updateActiveNav(String key) {
        if (btnEmbed != null) btnEmbed.setActive("embed".equals(key));
        if (btnExtract != null) btnExtract.setActive("extract".equals(key));
        if (btnTransfer != null) btnTransfer.setActive("transfer".equals(key));
    }
}
