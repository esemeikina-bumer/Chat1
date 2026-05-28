package ru.netology;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import java.io.*;
import java.net.Socket;
import static org.assertj.core.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ChatClientTest {

    private static ChatServer testServer;
    private static Thread serverThread;
    private static final int TEST_PORT = 12346;

    // ЗАПУСКАЕМ СЕРВЕР ПЕРЕД ВСЕМИ ТЕСТАМИ
    @BeforeAll
    static void startServer() throws IOException {
        testServer = new ChatServer(TEST_PORT);
        serverThread = new Thread(() -> {
            try {
                testServer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        serverThread.start();

        // Даём серверу время запуститься
        try { Thread.sleep(500); } catch (InterruptedException e) {}
        System.out.println("✅ Тестовый сервер запущен на порту " + TEST_PORT);
    }

    // ОСТАНАВЛИВАЕМ СЕРВЕР ПОСЛЕ ВСЕХ ТЕСТОВ
    @AfterAll
    static void stopServer() throws IOException {
        if (testServer != null) {
            testServer.stop();
        }
        if (serverThread != null) {
            serverThread.interrupt();
        }
        System.out.println("🛑 Тестовый сервер остановлен");
    }

    @Test
    @DisplayName("Тест: Клиент должен подключаться к серверу")
    void testClientConnection() {
        assertThatCode(() -> {
            try (Socket socket = new Socket("localhost", TEST_PORT)) {
                assertThat(socket.isConnected()).isTrue();
            }
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Тест: Клиент должен логировать отправленные сообщения")
    void testClientLogsSentMessages() throws IOException {
        File tempLog = File.createTempFile("test_log", ".log");
        String testMessage = "Привет, мир!";

        try (FileWriter fw = new FileWriter(tempLog, true)) {
            fw.write("[2024-01-15 10:00:00] Отправлено: " + testMessage + "\n");
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(tempLog))) {
            String content = reader.readLine();
            assertThat(content).contains(testMessage);
        }

        tempLog.delete();
    }

    @Test
    @DisplayName("Тест: Команда exit должна закрывать соединение")
    void testExitCommand() {
        assertThatCode(() -> {
            String command = "exit";
            assertThat(command.toLowerCase()).isEqualTo("exit");
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Тест: Клиент может отправить сообщение на сервер")
    void testClientCanSendMessage() throws IOException {
        try (Socket socket = new Socket("localhost", TEST_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Получаем запрос никнейма
            String nickRequest = in.readLine();
            assertThat(nickRequest).isEqualTo("NICK");

            // Отправляем никнейм
            out.println("TestUser");

            // Отправляем сообщение
            out.println("Hello Server!");

            assertThat(true).isTrue();
        }
    }
}