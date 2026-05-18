package ru.gr0946x.ui;

import ru.gr0946x.net.Client;
import ru.gr0946x.net.MessageType;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ChatWindow extends JFrame {
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

    private static final Color PRIMARY = new Color(63, 81, 181);
    private static final Color ACCENT = new Color(33, 150, 243);
    private static final Color SUCCESS = new Color(76, 175, 80);
    private static final Color BG = new Color(245, 247, 250);
    private static final Color CHAT_BG = new Color(255, 255, 250);

    public ChatWindow(Client client, String username) {
        this.client = client;
        this.username = username;
        setTitle("Kareta - @" + username);
        setSize(1000, 700);
        setMinimumSize(new Dimension(850, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        getContentPane().setBackground(BG);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                new Thread(() -> { client.stop(); System.exit(0); }).start();
            }
        });

        init();
        client.addDataListener((data, type) -> parseData(data, type));
        setVisible(true);
    }

    private void init() {
        var root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(15, 15, 15, 15));
        root.setBackground(BG);

        var header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(12, 15, 12, 15)
        ));
        currentUserLabel = new JLabel("@" + username, SwingConstants.LEFT);
        currentUserLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        currentUserLabel.setForeground(PRIMARY);
        header.add(currentUserLabel, BorderLayout.WEST);
        var onlineDot = new JLabel("●");
        onlineDot.setForeground(SUCCESS);
        onlineDot.setFont(new Font("Segoe UI", Font.BOLD, 18));
        header.add(onlineDot, BorderLayout.EAST);
        root.add(header, BorderLayout.NORTH);

        var centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        centerSplit.setResizeWeight(0.78);
        centerSplit.setBackground(BG);
        centerSplit.setDividerLocation(750);

        var chatCard = new JPanel(new BorderLayout());
        chatCard.setBackground(Color.WHITE);
        chatCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(0, 0, 0, 0)
        ));
        var chatHeader = new JLabel("Чат");
        chatHeader.setFont(new Font("Segoe UI", Font.BOLD, 13));
        chatHeader.setBorder(new EmptyBorder(10, 15, 8, 15));
        chatHeader.setForeground(new Color(80, 80, 80));
        chatCard.add(chatHeader, BorderLayout.NORTH);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        chatArea.setBackground(CHAT_BG);
        chatArea.setBorder(new EmptyBorder(12, 15, 12, 15));
        var chatScroll = new JScrollPane(chatArea);
        chatScroll.setBorder(null);
        chatCard.add(chatScroll, BorderLayout.CENTER);
        centerSplit.setLeftComponent(chatCard);

        var usersCard = new JPanel(new BorderLayout());
        usersCard.setBackground(Color.WHITE);
        usersCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(0, 0, 0, 0)
        ));
        usersCard.setPreferredSize(new Dimension(220, 0));

        var usersHeader = new JLabel("Онлайн");
        usersHeader.setFont(new Font("Segoe UI", Font.BOLD, 13));
        usersHeader.setBorder(new EmptyBorder(10, 12, 8, 12));
        usersHeader.setForeground(new Color(80, 80, 80));
        usersCard.add(usersHeader, BorderLayout.NORTH);

        usersModel = new DefaultListModel<>();
        usersList = new JList<>(usersModel);
        usersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        usersList.setBackground(new Color(250, 252, 255));
        usersList.setFixedCellHeight(32);
        usersList.setBorder(new EmptyBorder(5, 5, 5, 5));
        usersList.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        usersList.setCellRenderer((list, value, idx, isSelected, hasFocus) -> {
            var panel = new JPanel(new BorderLayout(8, 0));
            panel.setOpaque(true);
            panel.setBorder(new EmptyBorder(6, 10, 6, 10));
            var dot = new JLabel("●");
            dot.setFont(new Font("Segoe UI", Font.BOLD, 10));
            dot.setForeground(ACCENT);
            panel.add(dot, BorderLayout.WEST);
            var label = new JLabel(value.toString());
            label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            label.setForeground(new Color(50, 50, 50));
            panel.add(label, BorderLayout.CENTER);
            if (isSelected) {
                panel.setBackground(ACCENT);
                label.setForeground(Color.WHITE);
                dot.setForeground(Color.WHITE);
            } else {
                panel.setBackground(idx % 2 == 0 ? new Color(250, 252, 255) : Color.WHITE);
            }
            return panel;
        });

        var usersScroll = new JScrollPane(usersList);
        usersScroll.setBorder(null);
        usersCard.add(usersScroll, BorderLayout.CENTER);
        centerSplit.setRightComponent(usersCard);
        root.add(centerSplit, BorderLayout.CENTER);

        var bottom = new JPanel(new BorderLayout(10, 10));
        bottom.setBackground(BG);
        bottom.setBorder(new EmptyBorder(8, 0, 0, 0));

        var searchCard = new JPanel(new BorderLayout(8, 0));
        searchCard.setBackground(Color.WHITE);
        searchCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        searchCard.setPreferredSize(new Dimension(0, 45));
        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
        searchField.setBackground(new Color(250, 250, 250));
        searchButton = new JButton("Поиск");
        searchButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        searchButton.setBackground(PRIMARY);
        searchButton.setForeground(Color.WHITE);
        searchButton.setFocusPainted(false);
        searchButton.setPreferredSize(new Dimension(90, 32));
        searchButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        searchCard.add(new JLabel("Поиск:"), BorderLayout.WEST);
        searchCard.add(searchField, BorderLayout.CENTER);
        searchCard.add(searchButton, BorderLayout.EAST);
        bottom.add(searchCard, BorderLayout.NORTH);

        var sendCard = new JPanel(new BorderLayout(8, 0));
        sendCard.setBackground(Color.WHITE);
        sendCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        messageField = new JTextField();
        messageField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        messageField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        messageField.setBackground(new Color(250, 250, 250));
        sendButton = new JButton("Отправить");
        sendButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        sendButton.setBackground(SUCCESS);
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.setPreferredSize(new Dimension(130, 36));
        sendButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        sendCard.add(messageField, BorderLayout.CENTER);
        sendCard.add(sendButton, BorderLayout.EAST);
        bottom.add(sendCard, BorderLayout.CENTER);

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
                String usersData = data.startsWith("USERS:") ? data.substring(6) : data;
                String[] users = usersData.split(",");
                String currentSelected = usersList.getSelectedValue();
                usersModel.clear();
                usersModel.addElement("Все");
                for (String u : users) {
                    if (!u.isBlank() && !u.equals(username)) {
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
}