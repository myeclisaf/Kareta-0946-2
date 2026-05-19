package ru.gr0946x.net;

import ru.gr0946x.net.entities.UserEntity;
import ru.gr0946x.net.services.MessageService;
import ru.gr0946x.net.services.UserService;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ConnectedClient {

    private static final String BROADCAST_TARGET = "Все";

    private final Communicator communicator;
    private static final List<ConnectedClient> clients = new ArrayList<>();

    private final UserService userService;
    private final MessageService messageService;

    private UserEntity user;
    private String selectedUser;

    public ConnectedClient(Socket socket, UserService userService, MessageService messageService) throws IOException {
        this.userService = userService;
        this.messageService = messageService;
        this.communicator = new Communicator(socket);
        this.communicator.addDataListener(this::parseData);
        this.communicator.addDisconnectListener(this::onDisconnect);
        synchronized (clients) {
            clients.add(this);
        }
    }

    public void start() {
        communicator.start();
        sendData(MessageType.REQUEST + ":LOGIN_OR_REGISTER");
    }

    private void parseData(String data) {
        var parts = data.split(":", 3);
        try {
            switch (parts[0]) {
                case "REGISTER" -> {
                    user = userService.register(parts[1], parts[2]);
                    sendData(MessageType.INFO + ":REGISTER_OK");
                    sendUsersToAll();
                }
                case "LOGIN" -> {
                    user = userService.login(parts[1], parts[2]);
                    sendData(MessageType.INFO + ":LOGIN_OK");
                    sendUsersToAll();
                }
                case "SELECT" -> {
                    selectedUser = parts[1];
                    if (BROADCAST_TARGET.equals(selectedUser)) {
                        selectedUser = null;
                        sendData(MessageType.INFO + ":Переключено в общий чат");
                        sendGeneralHistory();
                    } else {
                        sendHistory();
                    }
                }
                case "MESSAGE" -> {
                    if (user == null) return;
                    String text = parts.length > 1 ? parts[1] : "";
                    if (text.isBlank()) return;
                    if (selectedUser == null || BROADCAST_TARGET.equals(selectedUser)) {
                        sendBroadcast(text);
                    } else {
                        sendPrivate(text);
                    }
                }
                case "SEARCH" -> {
                    if (user == null) return;
                    if (selectedUser == null || BROADCAST_TARGET.equals(selectedUser)) {
                        sendData(MessageType.INFO + ":Для поиска выберите конкретного пользователя из списка");
                        return;
                    }
                    String query = parts.length > 1 ? parts[1] : "";
                    sendSearchResults(query);
                }
                case "GET_USERS" -> {
                    if (user != null) {
                        sendUsersToAll();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Ошибка в parseData: " + e.getMessage());
            e.printStackTrace();
            sendData(MessageType.ERROR + ":" + e.getMessage());
        }
    }

    private void sendBroadcast(String text) {
        try {
            if (user == null) return;
            messageService.saveMessage(user, null, text);
            String msg = MessageType.MESSAGE + ":" + user.getNick() + ":" + text;
            synchronized (clients) {
                clients.stream()
                        .filter(c -> c.user != null)
                        .forEach(c -> c.sendData(msg));
            }
        } catch (Exception e) {
            System.err.println("❌ Ошибка отправки в общий чат: " + e.getMessage());
            e.printStackTrace();
            sendData(MessageType.ERROR + ":Не удалось отправить сообщение");
        }
    }

    private void sendPrivate(String text) {
        try {
            if (user == null) return;
            ConnectedClient target = findClientByNick(selectedUser);
            if (target == null) {
                sendData(MessageType.ERROR + ":Пользователь не в сети");
                return;
            }
            messageService.saveMessage(user, target.user, text);
            String msg = MessageType.MESSAGE + ":" + user.getNick() + ":" + text;
            target.sendData(msg);
            this.sendData(msg);
        } catch (Exception e) {
            System.err.println("❌ Ошибка отправки личного сообщения: " + e.getMessage());
            e.printStackTrace();
            sendData(MessageType.ERROR + ":Не удалось отправить сообщение");
        }
    }

    // ✅ ИСТОРИЯ ЛИЧНОГО ДИАЛОГА
    private void sendHistory() {
        if (user == null) return;
        ConnectedClient target = findClientByNick(selectedUser);
        if (target == null || target.user == null) return;

        var history = messageService.getDialog(user.getId(), target.user.getId());

        for (var m : history) {
            sendData(MessageType.MESSAGE + ":" + m.getSender().getNick() + ":" + m.getText());
        }
    }

    // ✅ ИСТОРИЯ ОБЩЕГО ЧАТА
    private void sendGeneralHistory() {
        var history = messageService.getGeneralChatHistory();

        for (var m : history) {
            sendData(MessageType.MESSAGE + ":" + m.getSender().getNick() + ":" + m.getText());
        }
    }

    private void sendSearchResults(String query) {
        try {
            ConnectedClient target = findClientByNick(selectedUser);
            if (target == null || user == null) return;
            var results = messageService.search(user.getId(), target.user.getId(), query);
            sendData(MessageType.INFO + ":--- Результаты поиска: \"" + query + "\" ---");
            if (results.isEmpty()) {
                sendData(MessageType.INFO + ":Ничего не найдено");
            } else {
                for (var m : results) {
                    sendData(MessageType.MESSAGE + ":" + m.getSender().getNick() + ":" + m.getText());
                }
            }
            sendData(MessageType.INFO + ":--- Конец поиска ---");
        } catch (Exception e) {
            System.err.println("❌ Ошибка поиска: " + e.getMessage());
            e.printStackTrace();
            sendData(MessageType.ERROR + ":Ошибка при поиске");
        }
    }

    private ConnectedClient findClientByNick(String nick) {
        synchronized (clients) {
            return clients.stream()
                    .filter(c -> c.user != null && c.user.getNick().equalsIgnoreCase(nick))
                    .findFirst()
                    .orElse(null);
        }
    }

    private void sendUsersToAll() {
        synchronized (clients) {
            for (ConnectedClient client : clients) {
                if (client.user == null) continue;

                var builder = new StringBuilder(BROADCAST_TARGET);
                for (ConnectedClient c : clients) {
                    if (c.user != null && !c.user.getNick().equals(client.user.getNick())) {
                        builder.append(",").append(c.user.getNick());
                    }
                }

                String usersData = MessageType.USERS + ":" + builder;
                client.sendData(usersData);
            }
        }
    }

    private void onDisconnect() {
        synchronized (clients) {
            clients.remove(this);
        }
        if (user != null) {
            sendUsersToAll();
        }
    }

    public void sendData(String data) {
        communicator.sendData(data);
    }
}