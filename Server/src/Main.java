import serverChat.ChatServer;
import serverFile.FileServer;
import util.ServerIPAddress;

import java.util.Scanner;
public class Main {

    private static int serverPort ;
    private String command;
    private static volatile boolean isRunning = true;
    private static String Menu = "Приветствую клиент, при помощи команд вы сможете (только для начала пожалуйста введите какой порт использовать серверу и к какому Ip-адресу подключаться)\n" +
            "File - отправить файл на сервер\n" +
            "Chat - начать диалог с сервером\n" +
            "Quit - завершить программу";

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        ServerIPAddress ipAdd = new ServerIPAddress();

        System.out.println(Menu);

        ipAdd.ipAddress();

        System.out.println("Введите порт сервера : ");
        int serverPort = scanner.nextInt();
        System.out.println("Введите что вы хотите сделать : ");
        String command = scanner.next();

        switch (command) {
            case "File":
                System.out.println("Начинаем отправку файлов...");
                FileServer client = new FileServer(serverPort,Main.class);
                client.accept();
                break;
            case "Chat":
                System.out.println("Начинаем диалог...");
                ChatServer chat = new ChatServer(serverPort);
                chat.setMainClass(Main.class);
                chat.chatServ();
                break;
            case "Quit":
                System.out.println("Выполнение программы завершено");
                break;
            default:
                System.out.println("Введена неверная команда");
        }
    }
}
