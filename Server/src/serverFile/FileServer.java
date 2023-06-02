package serverFile;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class FileServer {
    private static int serverPort; // Порт сервера
    private static volatile boolean isRunning = true;
    private static Class<?> mainClass; // Ссылка на класс Main
    private static final String SAVE_FOLDER = "server_files"; // Папка для сохранения файлов на сервере

    public FileServer(int serverPort, Class<?> mainClass) {
        this.mainClass = mainClass;
        this.serverPort = serverPort;
    }

    public static void Accept() {
        try (ServerSocket serverSocket = new ServerSocket(serverPort)) {
            System.out.println("Сервер запущен. Ожидание подключений...");

            while (isRunning) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Подключение установлено: " + clientSocket);

                // Создаем новый поток для каждого подключения клиента
                Thread clientThread = new Thread(new ClientHandler(clientSocket));
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket clientSocket;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try (InputStream inputStream = clientSocket.getInputStream();
                 DataInputStream dataInputStream = new DataInputStream(inputStream);
                 OutputStream outputStream = clientSocket.getOutputStream();
                 DataOutputStream dataOutputStream = new DataOutputStream(outputStream)) {

                // Читаем команду от клиента
                String command = dataInputStream.readUTF();
                System.out.println("Команда от клиента: " + command);

                if (command.equals("UPLOAD")) {
                    // Команда от клиента: UPLOAD - загрузить файл на сервер

                    // Читаем имя файла от клиента
                    String fileName = dataInputStream.readUTF();

                    // Создаем новый файл для сохранения на сервере
                    File savedFile = new File(SAVE_FOLDER, fileName);

                    // Читаем данные файла от клиента и сохраняем на сервере
                    try (FileOutputStream fileOutputStream = new FileOutputStream(savedFile)) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = dataInputStream.read(buffer)) != -1) {
                            fileOutputStream.write(buffer, 0, bytesRead);
                        }
                    }

                    System.out.println("Файл сохранен на сервере: " + savedFile.getAbsolutePath());

                } else if (command.equals("DOWNLOAD")) {
                    // Команда от клиента: DOWNLOAD - скачать файл с сервера

                    // Читаем имя файла от клиента
                    String fileName = dataInputStream.readUTF();

                    // Проверяем, существует ли файл на сервере
                    File file = new File(SAVE_FOLDER, fileName);
                    if (file.exists()) {
                        // Отправляем имя файла клиенту
                        dataOutputStream.writeUTF(fileName);

                        // Читаем данные файла и отправляем клиенту
                        try (FileInputStream fileInputStream = new FileInputStream(file)) {
                            byte[] buffer = new byte[4096];
                            int bytesRead;
                            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                                dataOutputStream.write(buffer, 0, bytesRead);
                            }
                        }

                        System.out.println("Файл отправлен клиенту: " + file.getAbsolutePath());

                    } else {
                        // Отправляем пустое имя файла, чтобы клиент знал, что файл не найден
                        dataOutputStream.writeUTF("");
                        System.out.println("Файл не найден на сервере.");
                    }

                } else if (command.equals("VIEW")) {
                    // Команда от клиента: VIEW - просмотреть файлы на сервере

                    // Получаем список файлов на сервере
                    List<String> fileNames = getFileNamesOnServer();

                    // Отправляем количество файлов клиенту
                    dataOutputStream.writeInt(fileNames.size());

                    // Отправляем имена файлов клиенту
                    for (String fileName : fileNames) {
                        dataOutputStream.writeUTF(fileName);
                    }
                } else if (command.equals("EXIT")) {
                    isRunning = false;
                    exitProgram();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void exitProgram() {
            try {
                // Вызываем метод main() в основном классе для завершения программы
                Method mainMethod = mainClass.getMethod("main", String[].class);
                mainMethod.invoke(null, new Object[]{new String[0]});
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        private List<String> getFileNamesOnServer() {
            List<String> fileNames = new ArrayList<>();

            File folder = new File(SAVE_FOLDER);
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        fileNames.add(file.getName());
                    }
                }
            }

            return fileNames;
        }
    }
}
