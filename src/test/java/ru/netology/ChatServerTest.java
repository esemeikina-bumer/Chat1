package ru.netology;
import org.junit.jupiter.api.*;
import java.io.*;
import java.net.Socket;
import static org.junit.jupiter.api.Assertions.*;

class SimpleServerTest {

    private ChatServer server;

    @BeforeEach
    void setUp() throws IOException, InterruptedException {
        server = new ChatServer(12347);
        new Thread(() -> {
            try {
                server.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        Thread.sleep(500); // Ждём запуска
    }
    //очистка

    @AfterEach
    void tearDown() throws IOException {
        server.stop();
    }

    @Test
    void testAddAndRemoveClient() throws IOException, InterruptedException {
        // Проверяем, что клиентов 0
        assertEquals(0, server.getClients().size());

        // Подключаем клиента
        Socket socket = new Socket("localhost", 12347);
        Thread.sleep(100);

        // Проверяем, что клиент появился
        assertEquals(1, server.getClients().size());

        // Отключаем клиента
        socket.close();
        Thread.sleep(200);

        // Проверяем, что клиент исчез
        assertEquals(0, server.getClients().size());
    }
}