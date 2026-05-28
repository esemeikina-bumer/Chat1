package ru.netology;

import org.junit.jupiter.api.*;
import java.io.*;
import java.net.Socket;
import static org.assertj.core.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ChatClientIntegrationTest {

    private ChatServer server;
    private Thread serverThread;
    private static final int TEST_PORT = 12347;

    @BeforeAll
    void startServer() throws IOException, InterruptedException {
        server = new ChatServer(TEST_PORT);
        serverThread = new Thread(() -> {
            try {
                server.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        serverThread.start();
        Thread.sleep(500);
    }

    @AfterAll
    void stopServer() throws IOException {
        server.stop();
        serverThread.interrupt();
    }

    @Test
    @DisplayName("Тест: Полный цикл подключения клиента")
    void testFullClientConnection() throws IOException {
        // Подключаемся к серверу
        Socket socket = new Socket("localhost", TEST_PORT);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // Проверяем запрос никнейма
        String nickRequest = in.readLine();
        assertThat(nickRequest).isEqualTo("NICK");

        // Отправляем никнейм
        out.println("IntegrationUser");

        // Отправляем тестовое сообщение
        out.println("Test message");

        // Проверяем, что соединение активно
        assertThat(socket.isConnected()).isTrue();

        // Закрываем
        out.println("exit");
        socket.close();
    }
}
