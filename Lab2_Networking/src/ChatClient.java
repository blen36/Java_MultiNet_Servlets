import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class ChatClient extends JFrame {
    private static final int SERVER_PORT = 4567;
    private static final String SERVER_ADDRESS = "localhost";  // Для локальной машины используем "localhost"

    private DataOutputStream out;
    private DataInputStream in;

    private JTextField textFieldUsername;
    private JPasswordField passwordField;
    private JTextField textFieldTo;
    private JTextArea textAreaIncoming;
    private JTextArea textAreaOutgoing;

    private JButton searchButton; // Кнопка для поиска пользователей
    private DefaultListModel<String> usersListModel; // Модель для списка пользователей
    private JList<String> usersList; // Список пользователей

    public ChatClient() {
        // Инициализация UI
        setTitle("Клиент чата");
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Панель для логина
        JPanel loginPanel = new JPanel();
        textFieldUsername = new JTextField(15);
        passwordField = new JPasswordField(15);
        JButton loginButton = new JButton("Войти");

        loginPanel.add(new JLabel("Логин:"));
        loginPanel.add(textFieldUsername);
        loginPanel.add(new JLabel("Пароль:"));
        loginPanel.add(passwordField);
        loginPanel.add(loginButton);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                connectToServer();
            }
        });

        // Панель для сообщений
        JPanel messagePanel = new JPanel();
        textFieldTo = new JTextField(15);
        textAreaIncoming = new JTextArea(10, 30);
        textAreaOutgoing = new JTextArea(5, 30);
        JScrollPane scrollPaneIncoming = new JScrollPane(textAreaIncoming);
        JScrollPane scrollPaneOutgoing = new JScrollPane(textAreaOutgoing);
        JButton sendButton = new JButton("Отправить");

        messagePanel.add(new JLabel("Получатель:"));
        messagePanel.add(textFieldTo);
        messagePanel.add(scrollPaneOutgoing);
        messagePanel.add(sendButton);

        // Кнопка для поиска пользователей
        searchButton = new JButton("Поиск пользователей");
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getUsersList();
            }
        });

        // Список пользователей
        usersListModel = new DefaultListModel<>();
        usersList = new JList<>(usersListModel);
        JScrollPane usersScrollPane = new JScrollPane(usersList);

        JPanel usersPanel = new JPanel();
        usersPanel.setLayout(new BorderLayout());
        usersPanel.add(searchButton, BorderLayout.NORTH);
        usersPanel.add(usersScrollPane, BorderLayout.CENTER);

        add(loginPanel, BorderLayout.NORTH);
        add(scrollPaneIncoming, BorderLayout.CENTER);
        add(messagePanel, BorderLayout.SOUTH);
        add(usersPanel, BorderLayout.WEST);

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        setVisible(true);
    }

    private void connectToServer() {
        try {
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());

            String username = textFieldUsername.getText();
            String password = new String(passwordField.getPassword());

            // Отправка логина и пароля для аутентификации
            out.writeUTF(username);
            out.writeUTF(password);

            String response = in.readUTF();
            if (response.equals("Аутентификация успешна!")) {
                JOptionPane.showMessageDialog(this, "Успешно вошли в систему!");
            } else {
                JOptionPane.showMessageDialog(this, response, "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Не удалось подключиться к серверу", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void getUsersList() {
        try {
            out.writeUTF("GET_USERS");  // Отправка запроса на сервер для получения списка пользователей

            String response;
            while (!(response = in.readUTF()).equals("END_OF_USERS")) {
                if (response.equals("ACTIVE_USERS")) {
                    continue; // Пропускаем эту строку
                }
                usersListModel.addElement(response); // Добавляем пользователей в список
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage() {
        try {
            String recipient = textFieldTo.getText();
            String message = textAreaOutgoing.getText();

            if (recipient.isEmpty() || message.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Введите получателя и сообщение.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }

            out.writeUTF(recipient);
            out.writeUTF(message);

            textAreaIncoming.append("Я -> " + recipient + ": " + message + "\n");
            textAreaOutgoing.setText("");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ChatClient();
            }
        });
    }
}