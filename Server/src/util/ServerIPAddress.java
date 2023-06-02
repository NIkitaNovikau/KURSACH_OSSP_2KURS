package util;

import java.net.*;

public class ServerIPAddress {
    public static void ipAddress() {
        try {
            // Получение локального IP-адреса сервера
            InetAddress localhost = InetAddress.getLocalHost();
            String ipAddress = localhost.getHostAddress();
            System.out.println("IP-адрес сервера: " + ipAddress);

        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}