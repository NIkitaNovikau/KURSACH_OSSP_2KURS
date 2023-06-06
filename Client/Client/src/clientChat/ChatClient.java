package clientChat;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.*;

public class ChatClient {
    private static int serverport;
    private static String serverIp; // Укажите IP-адрес сервера
    private Class<?> mainClass;
    public ChatClient(String serverIp, int serverport) {
        this.serverIp = serverIp;
        this.serverport = serverport;
    }
    public void setmainClass(Class<?> mainclass)
    {
        this.mainClass = mainclass;
    }
    public void chat() {
        try (Socket clientSocket = new Socket(serverIp, serverport);
             BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
             BufferedReader inputReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter outputWriter = new PrintWriter(clientSocket.getOutputStream(), true)) {

            System.out.println("Успешное подключение к серверу.");

            // Создание потока для чтения сообщений от сервера
            Thread readThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String response;
                        while ((response = inputReader.readLine()) != null) {
                            System.out.println("Сервер: " + response);
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            readThread.start();

            // Отправка сообщений серверу
            String message;
            while (true) {
                message = reader.readLine();

                // Отправка сообщения на сервер
                outputWriter.println(message);

                if (message.equalsIgnoreCase("exit")) {
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
