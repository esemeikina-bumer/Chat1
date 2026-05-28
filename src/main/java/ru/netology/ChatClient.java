package ru.netology;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Scanner;

public class ChatClient {
    private String serverHost;
    private int serverPort;
    private String logFile;
    private String nickname;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private boolean connected = false;
    private Thread receiveThread;

    public ChatClient() {
        loadConfig();
    }

    public ChatClient(String host, int port, String logFile) {
        this.serverHost = host;
        this.serverPort = port;
        this.logFile = logFile;
    }

    private void loadConfig() {
        Properties props = new Properties();
        try (InputStream input = new FileInputStream("config.properties")) {
            props.load(input);
            serverHost = props.getProperty("server.host", "localhost");
            serverPort = Integer.parseInt(props.getProperty("server.port", "12345"));
            logFile = props.getProperty("log.file", "file.log");
            System.out.println("Настройки загружены:");
            System.out.println("  Сервер: " + serverHost + ":" + serverPort);
            System.out.println("  Лог-файл: " + logFile);
        } catch (IOException e) {
            System.err.println("Не удалось загрузить config.properties, используются значения по умолчанию");
            serverHost = "localhost";
            serverPort = 12345;
            logFile = "file.log";
        }
    }

    private void chooseNickname() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Введите ваше имя для чата: ");
        nickname = scanner.nextLine().trim();

        while (nickname.isEmpty()) {
            System.out.print("Имя не может быть пустым. Введите имя: ");
            nickname = scanner.nextLine().trim();
        }

        System.out.println("Добро пожаловать, " + nickname + "!");
        logMessage("Пользователь " + nickname + " подключился к чату");
    }

    private void logMessage(String message) {
        logMessage(message, false);
    }

    private void logMessage(String message, boolean isReceived) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String type = isReceived ? "Получено" : "Отправлено";
        String logEntry = String.format("[%s] %s: %s%n", timestamp, type, message);

        try (FileWriter fw = new FileWriter(logFile, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.print(logEntry);
        } catch (IOException e) {
            System.err.println("Ошибка записи в лог: " + e.getMessage());
        }
    }

    private boolean connect() {
        try {
            socket = new Socket(serverHost, serverPort);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            connected = true;
            System.out.println("Подключено к серверу " + serverHost + ":" + serverPort);
            logMessage("Подключение к серверу " + serverHost + ":" + serverPort);
            return true;
        } catch (IOException e) {
            System.err.println("Не удалось подключиться к серверу: " + e.getMessage());
            logMessage("Ошибка подключения: " + e.getMessage());
            return false;
        }
    }

    private void receiveMessages() {
        try {
            String message;
            while (connected && (message = in.readLine()) != null) {
                if (message.equals("NICK")) {
                    out.println(nickname);
                } else {
                    System.out.println("\r" + message);
                    System.out.print("Вы: ");
                    logMessage(message, true);
                }
            }
        } catch (IOException e) {
            if (connected) {
                System.out.println("\nСоединение с сервером потеряно");
                logMessage("Соединение потеряно: " + e.getMessage());
            }
        } finally {
            disconnect();
        }
    }

    private void sendMessages() {
        Scanner scanner = new Scanner(System.in);

        while (connected) {
            System.out.print("Вы: ");
            String message = scanner.nextLine();

            // Проверка на команду выхода /exit
            if (message.equalsIgnoreCase("/exit")) {
                System.out.println("Выход из чата...");
                logMessage("Пользователь " + nickname + " вышел из чата по команде /exit");

                // Отправляем команду exit на сервер (без слеша)
                out.println("exit");

                // Закрываем соединение
                disconnect();
                break;
            } else if (!message.trim().isEmpty()) {
                out.println(message);
                logMessage(message);
            }
        }
        scanner.close();
    }

    private void disconnect() {
        connected = false;

        // Прерываем поток получения
        if (receiveThread != null && receiveThread.isAlive()) {
            receiveThread.interrupt();
        }

        // Закрываем сокет
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Отключено от сервера");
        System.exit(0); // Принудительный выход из программы
    }

    public void start() {
        chooseNickname();

        if (!connect()) {
            System.out.println("Не удалось подключиться к серверу. Приложение будет закрыто.");
            return;
        }

        // Запуск потока для получения сообщений
        receiveThread = new Thread(this::receiveMessages);
        receiveThread.setDaemon(false);
        receiveThread.start();

        // Отправка сообщений (главный поток)
        sendMessages();
    }

    public boolean isConnected() {
        return connected;
    }

    public String getNickname() {
        return nickname;
    }

    public static void main(String[] args) {
        ChatClient client = new ChatClient();
        client.start();
    }
}