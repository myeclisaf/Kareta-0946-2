import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import Config.DatabaseConfig;
import Services.MessageService;
import Services.UserService;
import ru.gr0946x.net.Server;

public class Main {
    public static void main(String[] args) {
        var context = new AnnotationConfigApplicationContext(DatabaseConfig.class);

        var userService = context.getBean(UserService.class);
        var messageService = context.getBean(MessageService.class);

        new Server(9460, userService, messageService);

        System.out.println("Система готова к работе.");
    }
}