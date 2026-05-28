package ru.netology;
//Требования к серверу
//        Установка порта для подключения клиентов через файл настроек (например, settings.txt);
//        Возможность подключиться к серверу в любой момент и присоединиться к чату;
//        Отправка новых сообщений клиентам;
//        Запись всех отправленных через сервер сообщений с указанием имени пользователя и времени отправки.

import java.io.*;
import java.net.*;
import java.util.*;
import java.text.SimpleDateFormat;

public class ChatServer {
    private int port;
    private List<ClientHandler> clients = new ArrayList<>();
    private boolean isRunning = true;
    private ServerSocket serverSocket;

    // Конструктор для тестов
    public ChatServer(int port) {
        this.port = port;
    }

    // Конструктор по умолчанию
    public ChatServer() {
        this(12345);
    }


    public static void main(String[] args) {
        ChatServer server = new ChatServer();
        try {
            server.start();
        } catch (IOException e) {
            System.err.println("Ошибка запуска сервера: " + e.getMessage());
        }
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        logMessage("Сервер запущен на порту " + port);

        while (isRunning) {
            try {
                Socket socket = serverSocket.accept();
                System.out.println("Новое подключение от " + socket.getInetAddress());
                ClientHandler clientHandler = new ClientHandler(socket);
                clients.add(clientHandler);
                clientHandler.start();
            } catch (SocketException e) {
                if (!isRunning) break;
            }
        }
    }

    public void stop() throws IOException {
        isRunning = false;
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
        for (ClientHandler client : clients) {
            client.close();
        }
    }

    public synchronized void logMessage(String message) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String logEntry = String.format("[%s] %s%n", timestamp, message);

        try (FileWriter fw = new FileWriter("file.log", true);
             BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(logEntry);
        } catch (IOException e) {
            System.err.println("Ошибка записи в лог: " + e.getMessage());
        }
        System.out.print(logEntry);
    }

    public synchronized void broadcastMessage(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender && client.isConnected()) {
                client.sendMessage(message);
            }
        }
    }

    public synchronized void removeClient(ClientHandler client) {
        clients.remove(client);
        logMessage("Клиент " + client.getNickname() + " отключен. Активных: " + clients.size());
        broadcastMessage(client.getNickname() + " покинул чат!", null);
    }

    public List<ClientHandler> getClients() {
        return clients;
    }

    class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String nickname;
        private boolean connected = true;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public String getNickname() {
            return nickname;
        }

        public boolean isConnected() {
            return connected && !socket.isClosed();
        }

        public void sendMessage(String message) {
            if (out != null && connected) {
                out.println(message);
            }
        }

        public void close() {
            connected = false;
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                out.println("NICK");
                nickname = in.readLine();

                logMessage("Клиент " + nickname + " присоединился к чату");
                broadcastMessage(nickname + " присоединился к чату!", this);

                String message;
                while (connected && (message = in.readLine()) != null) {
                    if (message.equalsIgnoreCase("exit")) {
                        logMessage(nickname + " вышел из чата");
                        break;
                    }

                    String formattedMessage = nickname + ": " + message;
                    logMessage("Получено от " + nickname + ": " + message);
                    broadcastMessage(formattedMessage, this);
                }

            } catch (IOException e) {
                logMessage("Ошибка в клиенте " + nickname + ": " + e.getMessage());
            } finally {
                close();
                removeClient(this);
            }
        }
    }
}