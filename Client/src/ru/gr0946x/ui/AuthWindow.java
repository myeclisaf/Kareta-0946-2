package ru.gr0946x.ui;

import ru.gr0946x.net.Client;
import ru.gr0946x.net.MessageType;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class AuthWindow extends JFrame {
    private final Client client;
    private JTextField loginField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private JLabel statusLabel;

    private static final Color PRIMARY = new Color(63, 81, 181);
    private static final Color SUCCESS = new Color(76, 175, 80);
    private static final Color ERROR = new Color(244, 67, 54);
    private static final Color BG = new Color(245, 247, 250);

    public AuthWindow(Client client) {
        this.client = client;
        setTitle("Kareta");
        setSize(480, 400);
        setMinimumSize(new Dimension(440, 360));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        getContentPane().setBackground(BG);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                new Thread(() -> {
                    client.stop();
                    System.exit(0);
                }).start();
            }
        });

        init();
        client.addDataListener((data, type) -> parseData(data, type));
        setVisible(true);
    }

    private void init() {
        var root = new JPanel(new BorderLayout(0, 15));
        root.setBorder(new EmptyBorder(25, 25, 25, 25));
        root.setBackground(BG);

        // Заголовок
        var titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        var title = new JLabel("Kareta", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(PRIMARY);
        var subtitle = new JLabel("Мессенджер для общения", SwingConstants.CENTER);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(new Color(100, 100, 100));
        titlePanel.add(title, BorderLayout.CENTER);
        titlePanel.add(subtitle, BorderLayout.SOUTH);
        root.add(titlePanel, BorderLayout.NORTH);

        // Карточка авторизации
        var card = new JPanel(new BorderLayout(0, 15));
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(25, 30, 25, 30)
        ));
        card.setBackground(Color.WHITE);

        var form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        form.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Поле логина
        var loginPanel = createLabeledField("Логин", loginField = createTextField());
        loginPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 50));

        // Поле пароля
        var passPanel = createLabeledField("Пароль", passwordField = createPasswordField());
        passPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        passPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 50));

        // Кнопки
        var btnPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        btnPanel.setOpaque(false);
        btnPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 40));
        loginButton = createButton("Войти", SUCCESS);
        registerButton = createButton("Регистрация", PRIMARY);
        btnPanel.add(loginButton);
        btnPanel.add(registerButton);
        btnPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Собираем форму
        form.add(loginPanel);
        form.add(Box.createVerticalStrut(15));
        form.add(passPanel);
        form.add(Box.createVerticalStrut(20));
        form.add(btnPanel);

        card.add(form, BorderLayout.CENTER);

        // Статус
        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        statusLabel.setForeground(new Color(100, 100, 100));
        card.add(statusLabel, BorderLayout.SOUTH);

        root.add(card, BorderLayout.CENTER);
        setContentPane(root);

        // Обработчики событий
        ActionListener authAction = e -> {
            String nick = loginField.getText().trim();
            String pass = new String(passwordField.getPassword());
            if (!validateInput(nick, pass)) return;

            if (e.getSource() == registerButton) {
                setStatus("Выполняется регистрация...", Color.BLUE);
                client.sendData("REGISTER:" + nick + ":" + pass);
            } else {
                setStatus("Выполняется вход...", Color.BLUE);
                client.sendData("LOGIN:" + nick + ":" + pass);
            }
        };

        loginButton.addActionListener(authAction);
        registerButton.addActionListener(authAction);
        loginField.addActionListener(authAction);
        passwordField.addActionListener(authAction);
    }

    private JPanel createLabeledField(String label, JComponent field) {
        var panel = new JPanel(new BorderLayout(8, 0));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Short.MAX_VALUE, 50));

        var lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(new Color(80, 80, 80));
        lbl.setPreferredSize(new Dimension(60, 25));
        panel.add(lbl, BorderLayout.WEST);
        panel.add(field, BorderLayout.CENTER);

        return panel;
    }

    private JTextField createTextField() {
        var field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1, true),
                new EmptyBorder(6, 12, 6, 12)
        ));
        field.setBackground(new Color(250, 250, 250));
        field.setPreferredSize(new Dimension(0, 36));
        field.setMaximumSize(new Dimension(Short.MAX_VALUE, 36));
        return field;
    }

    private JPasswordField createPasswordField() {
        var field = new JPasswordField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1, true),
                new EmptyBorder(6, 12, 6, 12)
        ));
        field.setBackground(new Color(250, 250, 250));
        field.setPreferredSize(new Dimension(0, 36));
        field.setMaximumSize(new Dimension(Short.MAX_VALUE, 36));
        return field;
    }

    private JButton createButton(String text, Color bg) {
        var btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 0, 8, 0));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(0, 36));
        return btn;
    }

    private boolean validateInput(String nick, String pass) {
        if (nick.isBlank() || pass.isBlank()) {
            setStatus("Введите логин и пароль!", ERROR);
            return false;
        }
        if (!Character.isLetter(nick.charAt(0))) {
            setStatus("Имя должно начинаться с буквы!", ERROR);
            return false;
        }
        return true;
    }

    private void setStatus(String text, Color color) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(text);
            statusLabel.setForeground(color);
        });
    }

    private void parseData(String data, MessageType type) {
        if (type == MessageType.INFO) {
            if (data.contains("LOGIN_OK") || data.contains("REGISTER_OK")) {
                SwingUtilities.invokeLater(() -> {
                    String nick = loginField.getText().trim();
                    ChatWindow chat = new ChatWindow(client, nick);
                    chat.setVisible(true);
                    client.sendData("GET_USERS");
                    dispose();
                });
            } else {
                setStatus(data, ERROR);
            }
        } else if (type == MessageType.ERROR) {
            setStatus("Ошибка: " + data, ERROR);
        }
    }
}