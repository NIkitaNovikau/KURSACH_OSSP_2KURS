package clientFileSend;

import com.sun.tools.javac.Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.io.DataInputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;

public class FileClient {
    private String serverIp; // Адрес сервера
    private int serverPort; // Порт сервера
    private Class<?> mainClass; // Ссылка на класс Main

    private static final String SAVE_FOLDER = "client_files"; // Папка для сохранения файлов на клиенте

    public FileClient(String serverIp, int serverPort, Class<?> mainClass) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.mainClass = mainClass;
    }

    public void File() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }

    private void createAndShowGUI() {
        JFrame frame = new JFrame("File Transfer Client");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                frame.dispose();
                theEnd();
            }
        });

        JButton uploadButton = new JButton("Upload File");
        uploadButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int result = fileChooser.showOpenDialog(frame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    uploadFile(selectedFile);
                }
            }
        });

        JButton quitButton = new JButton("Quit");
        quitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                exitProgram();
            }
        });

        JButton downloadButton = new JButton("Download File");
        downloadButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int result = fileChooser.showSaveDialog(frame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    downloadFile(selectedFile);
                }
            }
        });

        JButton viewButton = new JButton("View Files on Server");
        viewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                viewFilesOnServer();
            }
        });

        frame.setLayout(new FlowLayout());
        frame.add(uploadButton);
        frame.add(downloadButton);
        frame.add(viewButton);
        frame.add(quitButton);

        frame.pack();
        frame.setVisible(true);
    }

    private void uploadFile(File file) {
        try (Socket socket = new Socket(serverIp, serverPort);
             OutputStream outputStream = socket.getOutputStream();
             DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
             FileInputStream fileInputStream = new FileInputStream(file)) {

            System.out.println("Соединение установлено: " + socket);

            dataOutputStream.writeUTF("UPLOAD");
            dataOutputStream.writeUTF(file.getName());

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                dataOutputStream.write(buffer, 0, bytesRead);
            }

            System.out.println("Файл успешно загружен на сервер: " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void downloadFile(File file) {
        try (Socket socket = new Socket(serverIp, serverPort);
             OutputStream outputStream = socket.getOutputStream();
             DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
             InputStream inputStream = socket.getInputStream();
             DataInputStream dataInputStream = new DataInputStream(inputStream)) {

            System.out.println("Соединение установлено: " + socket);

            dataOutputStream.writeUTF("DOWNLOAD");
            dataOutputStream.writeUTF(file.getName());

            long fileSize = dataInputStream.readLong();

            if (fileSize == -1) {
                System.out.println("Файл не существует на сервере: " + file.getName());
                return;
            }

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int result = fileChooser.showSaveDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {
                File saveFolder = fileChooser.getSelectedFile();
                File outputFile = new File(saveFolder, file.getName());
                FileOutputStream fileOutputStream = new FileOutputStream(outputFile);

                byte[] buffer = new byte[4096];
                int bytesRead;
                long totalBytesRead = 0;
                while (totalBytesRead < fileSize && (bytesRead = dataInputStream.read(buffer, 0, (int) Math.min(buffer.length, fileSize - totalBytesRead))) != -1) {
                    fileOutputStream.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                }

                fileOutputStream.close();

                System.out.println("Файл успешно скачан с сервера и сохранен: " + outputFile.getAbsolutePath());
            } else {
                System.out.println("Загрузка файла отменена");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void viewFilesOnServer() {
        try (Socket socket = new Socket(serverIp, serverPort);
             OutputStream outputStream = socket.getOutputStream();
             DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
             InputStream inputStream = socket.getInputStream();
             DataInputStream dataInputStream = new DataInputStream(inputStream)) {

            System.out.println("Соединение установлено: " + socket);

            dataOutputStream.writeUTF("VIEW");

            int fileCount = dataInputStream.readInt();

            System.out.println("Список файлов на сервере:");
            for (int i = 0; i < fileCount; i++) {
                String fileName = dataInputStream.readUTF();
                System.out.println(fileName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void exitProgram() {
        try (Socket socket = new Socket(serverIp, serverPort);
             OutputStream outputStream = socket.getOutputStream();
             DataOutputStream dataOutputStream = new DataOutputStream(outputStream)) {

            System.out.println("Соединение установлено: " + socket);

            dataOutputStream.writeUTF("EXIT");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void theEnd() {
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

