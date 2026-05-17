package ru.gr0946x.ui;

import ru.gr0946x.net.MessageType;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;

public class ConsoleUi implements Ui {

    private final List<Consumer<String>> listeners =
            new ArrayList<>();

    @Override
    public void showInfo(
            String data,
            MessageType type
    ) {
        System.out.println(data);
    }

    @Override
    public void addUserDataListener(
            Consumer<String> listener
    ) {
        listeners.add(listener);
    }

    @Override
    public void removeUserDataListener(
            Consumer<String> listener
    ) {
        listeners.remove(listener);
    }

    public void start() {

        var scanner =
                new Scanner(
                        System.in,
                        StandardCharsets.UTF_8
                );

        new Thread(() -> {

            while (true) {

                var line = scanner.nextLine();

                for (var listener : listeners) {
                    listener.accept(line);
                }
            }

        }).start();
    }
}



