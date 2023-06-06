
import java.util.Scanner;

import clientChat.ChatClient;
import clientFileSend.FileClient;

public class Main {
    private String serverIp;
    private int serverPort;
    private String command;
    private static volatile boolean isRunning = true;
    private static String Menu = "Приветствую клиент,при помощи команд вы сможете" +
            "(только для начала пожалуйста введите какой порт использовать серверу и к какому Ip-адрессу подключаться) " + "\n"
            + "File - отправить файл на сервер" + "\n" + "Chat - начать диалог с сервером" + "\n"
            + "Quit - завершить программу";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Введите IP-адресс сервера : ");
        String serverIp = scanner.nextLine();

        System.out.println("Введите порт сервера : ");
        int serverPort = scanner.nextInt();

        System.out.println("Введите что вы хотите сделать : ");
        String command = scanner.next();
        switch (command) {
            case "File":
                System.out.println("Начинаем отправку файлов...");
                FileClient client = new FileClient(serverIp, serverPort, Main.class);
                client.file();
                break;
            case "Chat":
                System.out.println("Начинаем диалог...");
                ChatClient chats = new ChatClient(serverIp, serverPort);
                chats.setmainClass(Main.class);
                chats.chat();
                break;
            case "Quit":
                System.out.println("Выполнение программы завершено");
                isRunning = false;
                break;
            default:
                System.out.println("Введена неверная команда");
                break;
        }
    }
}
