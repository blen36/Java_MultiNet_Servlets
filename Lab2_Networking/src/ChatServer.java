import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 4567;
    private static Map<String, String> usersDB = new HashMap<>(); // Логины и пароли
    private static Map<String, DataOutputStream> activeUsers = new HashMap<>(); // Активные пользователи

    public static void main(String[] args) {
        // Добавим несколько пользователей в базу данных для аутентификации
        usersDB.put("user1", "password1");
        usersDB.put("user2", "password2");
        usersDB.put("user3", "password3");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Сервер запущен, слушаем порт " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new ClientHandler(clientSocket).start();  // Обработка каждого клиента в отдельном потоке
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class ClientHandler extends Thread {
        private Socket socket;
        private DataInputStream in;
        private DataOutputStream out;

        public ClientHandler(Socket socket) throws IOException {
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
        }

        @Override
        public void run() {
            try {
                // Аутентификация
                String username = in.readUTF();
                String password = in.readUTF();

                if (usersDB.containsKey(username) && usersDB.get(username).equals(password)) {
                    out.writeUTF("Аутентификация успешна!");
                    activeUsers.put(username, out);
                    System.out.println(username + " вошел в систему.");

                    // Поиск пользователей
                    while (true) {
                        String request = in.readUTF();
                        if (request.equals("GET_USERS")) {
                            sendActiveUsersList(out);
                        } else {
                            String recipient = in.readUTF();
                            String message = in.readUTF();

                            // Проверяем, существует ли получатель
                            if (activeUsers.containsKey(recipient)) {
                                activeUsers.get(recipient).writeUTF(username + ": " + message);
                            } else {
                                out.writeUTF("Пользователь " + recipient + " не найден.");
                            }
                        }
                    }
                } else {
                    out.writeUTF("Неверный логин или пароль.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // Метод отправки списка активных пользователей
        private void sendActiveUsersList(DataOutputStream out) throws IOException {
            List<String> userList = new ArrayList<>(activeUsers.keySet());
            out.writeUTF("ACTIVE_USERS");
            for (String user : userList) {
                out.writeUTF(user);
            }
            out.writeUTF("END_OF_USERS");
        }
    }
}