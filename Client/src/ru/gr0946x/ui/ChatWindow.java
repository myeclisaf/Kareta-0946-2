package ru.gr0946x.ui;

import ru.gr0946x.net.Client;
import ru.gr0946x.net.MessageType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ChatWindow extends JFrame implements Ui {
    private final Client client;
    private final String username;
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private JButton searchButton;
    private JTextField searchField;
    private JList<String> usersList;
    private DefaultListModel<String> usersModel;
    private JLabel currentUserLabel;
    private final List<Consumer<String>> userDataListeners = new ArrayList<>();

    public ChatWindow(Client client, String username) {
        this.client = client;
        this.username = username;
        setTitle("Kareta - " + username);
        setSize(900, 600);
        setMinimumSize(new Dimension(800, 500));
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
        var root = new JPanel(new BorderLayout());
        var topPanel = new JPanel(new BorderLayout());
        currentUserLabel = new JLabel("Вы: " + username, SwingConstants.LEFT);
        currentUserLabel.setFont(new Font("Arial", Font.BOLD, 14));
        currentUserLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        topPanel.add(currentUserLabel, BorderLayout.WEST);
        root.add(topPanel, BorderLayout.NORTH);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        root.add(new JScrollPane(chatArea), BorderLayout.CENTER);

        usersModel = new DefaultListModel<>();
        usersList = new JList<>(usersModel);
        usersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        usersList.setFont(new Font("Arial", Font.PLAIN, 12));
        usersList.setPreferredSize(new Dimension(180, 0));
        var usersPanel = new JPanel(new BorderLayout());
        var usersTitle = new JLabel("Онлайн пользователи", SwingConstants.CENTER);
        usersTitle.setFont(new Font("Arial", Font.BOLD, 12));
        usersTitle.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        usersPanel.add(usersTitle, BorderLayout.NORTH);
        usersPanel.add(new JScrollPane(usersList), BorderLayout.CENTER);
        root.add(usersPanel, BorderLayout.EAST);

        var bottom = new JPanel(new BorderLayout(0, 5));
        bottom.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        var searchPanel = new JPanel(new BorderLayout(5, 0));
        searchField = new JTextField();
        searchButton = new JButton("Поиск");
        searchPanel.add(new JLabel("Поиск:"), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);
        var sendPanel = new JPanel(new BorderLayout(5, 0));
        messageField = new JTextField();
        sendButton = new JButton("Отправить");
        sendPanel.add(messageField, BorderLayout.CENTER);
        sendPanel.add(sendButton, BorderLayout.EAST);
        bottom.add(searchPanel, BorderLayout.NORTH);
        bottom.add(sendPanel, BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);
        setContentPane(root);

        sendButton.addActionListener(e -> sendMessage());
        messageField.addActionListener(e -> sendMessage());
        usersList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                var selected = usersList.getSelectedValue();
                if (selected != null) {
                    client.sendData("SELECT:" + selected);
                    chatArea.setText("");
                }
            }
        });
        searchButton.addActionListener(e -> sendSearch());
        searchField.addActionListener(e -> sendSearch());
    }

    private void sendMessage() {
        String text = messageField.getText().trim();
        if (text.isBlank()) return;
        client.sendData("MESSAGE:" + text);
        messageField.setText("");
    }

    private void sendSearch() {
        String query = searchField.getText().trim();
        if (query.isBlank()) return;
        client.sendData("SEARCH:" + query);
    }

    private void parseData(String data, MessageType type) {
        SwingUtilities.invokeLater(() -> {
            if (type == MessageType.USERS) {
                System.out.println("[DEBUG " + username + "] Обработка списка пользователей"); // <-- ДОБАВИТЬ
                String usersData = data.startsWith("USERS:") ? data.substring(6) : data;
                String[] users = usersData.split(",");
                String currentSelected = usersList.getSelectedValue();
                usersModel.clear();
                for (String u : users) {
                    if (!u.isBlank() && !u.equals(username)) {
                        System.out.println("[DEBUG " + username + "] Добавляю пользователя: " + u); // <-- ДОБАВИТЬ
                        usersModel.addElement(u);
                    }
                }
                if (currentSelected != null) usersList.setSelectedValue(currentSelected, false);
                userDataListeners.forEach(listener -> listener.accept(data));
                return;
            }

            switch (type) {
                case MESSAGE -> {
                    String[] msg = data.split(":", 2);
                    chatArea.append(msg.length == 2 ? msg[0] + ": " + msg[1] + "\n" : data + "\n");
                }
                case INFO -> {
                    chatArea.append("[Система] " + data + "\n");
                    userDataListeners.forEach(listener -> listener.accept(data));
                }
                case ERROR -> JOptionPane.showMessageDialog(this, data, "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }

    @Override
    public void showInfo(String data, MessageType type) {
        SwingUtilities.invokeLater(() -> {
            switch (type) {
                case INFO -> chatArea.append("[Инфо] " + data + "\n");
                case ERROR -> JOptionPane.showMessageDialog(this, data, "Ошибка", JOptionPane.ERROR_MESSAGE);
                default -> chatArea.append(data + "\n");
            }
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }

    @Override
    public void addUserDataListener(Consumer<String> listener) { userDataListeners.add(listener); }
    @Override
    public void removeUserDataListener(Consumer<String> listener) { userDataListeners.remove(listener); }
}