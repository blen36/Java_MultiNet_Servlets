import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ChatServer {

    private static final int PORT = 4567;

    private static Map<String, String> usersDB = new HashMap<>();

    private static Map<String, ClientHandler> activeUsers = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {

        usersDB.put("user1", "123");
        usersDB.put("user2", "123");
        usersDB.put("admin", "admin");

        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Сервер запущен...");

        while (true) {
            Socket socket = serverSocket.accept();
            new ClientHandler(socket).start();
        }
    }

    static class ClientHandler extends Thread {

        private Socket socket;
        private DataInputStream in;
        private DataOutputStream out;
        private String username;

        public ClientHandler(Socket socket) throws IOException {
            this.socket = socket;
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        }

        @Override
        public void run() {
            try {
                while (true) {
                    String command = in.readUTF();

                    switch (command) {
                        case "LOGIN":
                            handleLogin();
                            break;

                        case "SEND":
                            handleSend();
                            break;

                        case "GET_USERS":
                            sendUsers();
                            break;

                        case "LOGOUT":
                            logout();
                            return;
                    }
                }
            } catch (IOException e) {
                logout();
            }
        }

        private void handleLogin() throws IOException {
            String user = in.readUTF();
            String pass = in.readUTF();

            if (usersDB.containsKey(user) &&
                    usersDB.get(user).equals(pass)) {

                username = user;
                activeUsers.put(username, this);
                out.writeUTF("LOGIN_OK");
                System.out.println(user + " вошёл");

            } else {
                out.writeUTF("LOGIN_FAIL");
            }
        }

        private void handleSend() throws IOException {
            String recipient = in.readUTF();
            String message = in.readUTF();

            ClientHandler target = activeUsers.get(recipient);

            if (target != null) {
                target.out.writeUTF("MESSAGE");
                target.out.writeUTF(username);
                target.out.writeUTF(message);
            } else {
                out.writeUTF("ERROR");
                out.writeUTF("Пользователь не онлайн");
            }
        }

        private void sendUsers() throws IOException {
            out.writeUTF("USERS");
            for (String user : activeUsers.keySet()) {
                out.writeUTF(user);
            }
            out.writeUTF("END");
        }

        private void logout() {
            if (username != null) {
                activeUsers.remove(username);
                System.out.println(username + " вышел");
            }
            try { socket.close(); } catch (Exception ignored) {}
        }
    }
}