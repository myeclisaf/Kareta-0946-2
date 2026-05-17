package ru.gr0946x.net;

import Services.MessageService;
import Services.UserService;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private boolean isActive = true;
    private final UserService userService;
    private final MessageService messageService;

    public Server(int port, UserService userService, MessageService messageService) {
        this.userService = userService;
        this.messageService = messageService;

        new Thread(() -> {
            try (var serverSocket = new ServerSocket(port)) {
                System.out.println("Сервер запущен на порту " + port);
                while (isActive) {
                    Socket socket = serverSocket.accept();
                    ConnectedClient connClient = new ConnectedClient(socket, userService, messageService);
                    connClient.start();
                }
            } catch (IOException e) {
                System.err.println("Ошибка сервера: " + e.getMessage());
            }
        }).start();
    }
}