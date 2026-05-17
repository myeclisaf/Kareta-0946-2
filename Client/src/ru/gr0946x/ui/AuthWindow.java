package ru.gr0946x.ui;

import ru.gr0946x.net.Client;
import ru.gr0946x.net.MessageType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class AuthWindow extends JFrame {
    private final Client client;
    private JTextField loginField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private JLabel statusLabel;

    public AuthWindow(Client client) {
        this.client = client;
        setTitle("Kareta - Вход");
        setSize(400, 250);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

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
        // ✅ Явная лямбда вместо this::parseData
        client.addDataListener((data, type) -> parseData(data, type));
        setVisible(true);
    }

    private void init() {
        var root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        var title = new JLabel("Добро пожаловать в Kareta", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        root.add(title, BorderLayout.NORTH);

        var authPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        authPanel.add(new JLabel("Логин:"));
        loginField = new JTextField();
        authPanel.add(loginField);
        authPanel.add(new JLabel("Пароль:"));
        passwordField = new JPasswordField();
        authPanel.add(passwordField);

        loginButton = new JButton("Войти");
        registerButton = new JButton("Регистрация");
        var buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        authPanel.add(new JLabel());
        authPanel.add(buttonPanel);

        root.add(authPanel, BorderLayout.CENTER);
        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setForeground(Color.BLUE);
        root.add(statusLabel, BorderLayout.SOUTH);
        setContentPane(root);

        loginButton.addActionListener(e -> attemptLogin());
        registerButton.addActionListener(e -> attemptRegister());
        loginField.addActionListener(e -> attemptLogin());
        passwordField.addActionListener(e -> attemptLogin());
    }

    private void attemptLogin() {
        String nick = loginField.getText().trim();
        String pass = new String(passwordField.getPassword());
        if (!validateInput(nick, pass)) return;
        setStatus("Выполняется вход...", Color.BLUE);
        client.sendData("LOGIN:" + nick + ":" + pass);
    }

    private void attemptRegister() {
        String nick = loginField.getText().trim();
        String pass = new String(passwordField.getPassword());
        if (!validateInput(nick, pass)) return;
        setStatus("Выполняется регистрация...", Color.BLUE);
        client.sendData("REGISTER:" + nick + ":" + pass);
    }

    private boolean validateInput(String nick, String pass) {
        if (nick.isBlank() || pass.isBlank()) {
            setStatus("Введите логин и пароль!", Color.RED);
            return false;
        }
        if (!nick.matches("^[a-zA-Zа-яА-ЯёЁ].*")) {
            setStatus("Имя должно начинаться с буквы!", Color.RED);
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
        if (type == MessageType.INFO && (data.contains("LOGIN_OK") || data.contains("REGISTER_OK"))) {
            SwingUtilities.invokeLater(() -> {
                String nick = loginField.getText().trim();
                ChatWindow chat = new ChatWindow(client, nick);
                chat.setVisible(true);
                SwingUtilities.invokeLater(() -> client.sendData("GET_USERS"));
                dispose();
            });
        } else if (type == MessageType.ERROR) {
            setStatus("Ошибка: " + data, Color.RED);
        }
    }
}