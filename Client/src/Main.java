import ru.gr0946x.net.Client;
import ru.gr0946x.ui.AuthWindow;
import ru.gr0946x.ui.ChatWindow;

void main() {
    try {
        var client = new Client("localhost", 9460);
        client.start();
        new AuthWindow(client);
    } catch (Exception e) {
        System.out.println(e.getMessage());
    }
}