package ru.gr0946x.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Communicator {
    private final Socket socket;
    private final BufferedReader in;
    private final PrintWriter out;
    private volatile boolean isActive;

    private final List<Consumer<String>> dataListeners = new ArrayList<>();
    private final List<Runnable> disconnectListeners = new ArrayList<>();

    public void addDataListener(Consumer<String> c) {
        dataListeners.add(c);
    }

    public void removeDataListener(Consumer<String> c) {
        dataListeners.remove(c);
    }

    public void addDisconnectListener(Runnable r) {
        disconnectListeners.add(r);
    }

    public Communicator(Socket socket) throws IOException {
        this.socket = socket;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
    }

    public void start() {
        isActive = true;
        new Thread(() -> {
            try {
                while (isActive) {
                    var data = in.readLine();
                    if (data == null) break;
                    for (var listener : dataListeners) {
                        listener.accept(data);
                    }
                }
            } catch (Exception e) {
                if (isActive) {
                    System.err.println("Ошибка чтения: " + e.getMessage());
                }
            } finally {
                stop();
                for (var r : disconnectListeners) {
                    r.run();
                }
            }
        }).start();
    }

    public void sendData(String data) {
        if (isActive && !socket.isClosed())
            out.println(data);
    }

    public synchronized void stop() {
        if (!isActive) return;
        isActive = false;
        try {
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.err.println("Ошибка закрытия: " + e.getMessage());
        }
    }
}