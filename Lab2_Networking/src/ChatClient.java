import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ChatClient extends JFrame {

    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 4567;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private String username;

    private JTextField textFieldUsername;
    private JPasswordField passwordField;
    private JButton loginButton;

    private JTextField textFieldMessage;
    private JButton sendButton;

    private DefaultListModel<String> usersListModel;
    private JList<String> usersList;
    private JButton refreshUsersButton;

    private JTabbedPane chatTabs;
    // Храним вкладки чатов
    private Map<String, JTextArea> chats = new HashMap<>();

    public ChatClient() {
        setTitle("Клиент чата");
        setSize(800, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        initLoginPanel();
        initMainUI();

        setVisible(true);
    }

    private void initLoginPanel() {
        JPanel loginPanel = new JPanel();

        textFieldUsername = new JTextField(10);
        passwordField = new JPasswordField(10);
        loginButton = new JButton("Войти");

        loginPanel.add(new JLabel("Логин:"));
        loginPanel.add(textFieldUsername);
        loginPanel.add(new JLabel("Пароль:"));
        loginPanel.add(passwordField);
        loginPanel.add(loginButton);

        add(loginPanel, BorderLayout.NORTH);

        loginButton.addActionListener(e -> connectToServer());
    }

    private void initMainUI() {

        // Вкладки диалогов
        chatTabs = new JTabbedPane();
        add(chatTabs, BorderLayout.CENTER);

        // Панель отправки
        JPanel bottomPanel = new JPanel(new BorderLayout());
        textFieldMessage = new JTextField();
        sendButton = new JButton("Отправить");

        bottomPanel.add(textFieldMessage, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);

        sendButton.addActionListener(e -> sendMessage());

        // Панель пользователей
        usersListModel = new DefaultListModel<>();
        usersList = new JList<>(usersListModel);
        refreshUsersButton = new JButton("Обновить");

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(refreshUsersButton, BorderLayout.NORTH);
        leftPanel.add(new JScrollPane(usersList), BorderLayout.CENTER);
        leftPanel.setPreferredSize(new Dimension(150, 0));

        add(leftPanel, BorderLayout.WEST);

        refreshUsersButton.addActionListener(e -> requestUsers());

        // Двойной клик по пользователю — открыть чат
        usersList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    String user = usersList.getSelectedValue();
                    if (user != null) {
                        openChat(user);
                    }
                }
            }
        });
    }

    private void connectToServer() {
        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            username = textFieldUsername.getText();
            String password = new String(passwordField.getPassword());

            out.writeUTF("LOGIN");
            out.writeUTF(username);
            out.writeUTF(password);

            String response = in.readUTF();

            if (response.equals("LOGIN_OK")) {
                JOptionPane.showMessageDialog(this, "Вход выполнен");
                startListening();
            } else {
                JOptionPane.showMessageDialog(this, "Ошибка входа");
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Не удалось подключиться");
        }
    }

    private void startListening() {
        new Thread(() -> {
            try {
                while (true) {
                    String command = in.readUTF();

                    switch (command) {

                        case "MESSAGE":
                            String sender = in.readUTF();
                            String message = in.readUTF();
                            SwingUtilities.invokeLater(() -> {
                                openChat(sender);
                                chats.get(sender)
                                        .append(sender + ": " + message + "\n");
                            });
                            break;

                        case "USERS":
                            SwingUtilities.invokeLater(() -> usersListModel.clear());
                            String user;
                            while (!(user = in.readUTF()).equals("END")) {
                                String finalUser = user;
                                SwingUtilities.invokeLater(() ->
                                        usersListModel.addElement(finalUser));
                            }
                            break;

                        case "ERROR":
                            String error = in.readUTF();
                            JOptionPane.showMessageDialog(this, error);
                            break;
                    }
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                        "Соединение с сервером потеряно");
            }
        }).start();
    }

    private void sendMessage() {
        try {
            int index = chatTabs.getSelectedIndex();
            if (index == -1) {
                JOptionPane.showMessageDialog(this,
                        "Выберите пользователя");
                return;
            }

            String recipient = chatTabs.getTitleAt(index);
            String message = textFieldMessage.getText();

            if (message.isEmpty()) return;

            out.writeUTF("SEND");
            out.writeUTF(recipient);
            out.writeUTF(message);

            chats.get(recipient).append("Я: " + message + "\n");
            textFieldMessage.setText("");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void requestUsers() {
        try {
            out.writeUTF("GET_USERS");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openChat(String user) {
        if (!chats.containsKey(user)) {
            JTextArea area = new JTextArea();
            area.setEditable(false);
            chats.put(user, area);
            chatTabs.addTab(user, new JScrollPane(area));
        }
        chatTabs.setSelectedIndex(chatTabs.indexOfTab(user));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChatClient::new);
    }
}