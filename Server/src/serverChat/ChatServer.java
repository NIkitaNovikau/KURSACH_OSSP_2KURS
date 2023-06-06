package serverChat;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.*;

public class ChatServer {
    private static int serverPort;
    private Class<?> mainClass; // Ссылка на класс Main

    public ChatServer(int serverPort) {
        this.serverPort = serverPort;
    }

    public void setMainClass(Class<?> mainClass) {
        this.mainClass = mainClass;
    }

    public void chatServ() {
        try (ServerSocket serverSocket = new ServerSocket(serverPort);
             Socket clientSocket = serverSocket.accept();
             BufferedReader inputReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter outputWriter = new PrintWriter(clientSocket.getOutputStream(), true);
             BufferedReader serverReader = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("Сервер запущен. Ожидание подключения клиента...");
            System.out.println("Клиент подключен.");

            Thread readThread = new Thread(() -> {
                try {
                    String message;
                    while ((message = inputReader.readLine()) != null) {
                        System.out.println("Клиент: " + message);
                        outputWriter.println("Получено сообщение: " + message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            readThread.start();

            String serverMessage;
            while (true) {
                serverMessage = serverReader.readLine();
                outputWriter.println(serverMessage);
                if (serverMessage == null || serverMessage.equalsIgnoreCase("exit")) {
                    break;
                }
            }

            exitProgram();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void exitProgram() {
        if (mainClass != null) {
            try {
                Method mainMethod = mainClass.getMethod("main", String[].class);
                mainMethod.invoke(null, (Object) new String[0]);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }
}
