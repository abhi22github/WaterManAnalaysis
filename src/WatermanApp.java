import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class WatermanApp extends JFrame {

    private CardLayout cardLayout = new CardLayout();
    private JPanel mainPanel = new JPanel(cardLayout);

    private Connection conn;

    private User currentUser = null;

    // CHANGE THESE TO MATCH YOUR MYSQL SETUP
    private static final String DB_URL = "jdbc:mysql://localhost:3306/waterman?useSSL=false";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "Abhi2209!!";

    public WatermanApp() {
        setTitle("Waterman Analysis");
        setSize(450, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Initialize DB connection and setup tables
        initDatabase();

        // Panels
        RegisterPanel registerPanel = new RegisterPanel();
        LoginPanel loginPanel = new LoginPanel();
        OrderPanel orderPanel = new OrderPanel();

        mainPanel.add(registerPanel, "register");
        mainPanel.add(loginPanel, "login");
        mainPanel.add(orderPanel, "order");

        add(mainPanel);
        cardLayout.show(mainPanel, "login"); // start at login
    }

    private void initDatabase() {
        try {
            // Load MySQL driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Connect to MySQL
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database connection failed. Exiting.");
            System.exit(1);
        }
    }

    private static class User {
        int id;
        String username;
        User(int id, String username) {
            this.id = id;
            this.username = username;
        }
    }

    class RegisterPanel extends JPanel {
        JTextField usernameField = new JTextField(20);
        JPasswordField passwordField = new JPasswordField(20);
        JButton registerBtn = new JButton("Register");
        JButton toLoginBtn = new JButton("Already have account? Login");

        RegisterPanel() {
            setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10,10,10,10);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            gbc.gridx = 0; gbc.gridy = 0;
            add(new JLabel("Choose Username:"), gbc);
            gbc.gridx = 1;
            add(usernameField, gbc);

            gbc.gridx = 0; gbc.gridy = 1;
            add(new JLabel("Choose Password:"), gbc);
            gbc.gridx = 1;
            add(passwordField, gbc);

            gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
            add(registerBtn, gbc);

            gbc.gridy = 3;
            add(toLoginBtn, gbc);

            registerBtn.addActionListener(e -> registerUser());
            toLoginBtn.addActionListener(e -> cardLayout.show(mainPanel, "login"));
        }

        private void registerUser() {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username and password cannot be empty.");
                return;
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO users (username, password) VALUES (?, ?)")) {
                ps.setString(1, username);
                ps.setString(2, password);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Registration successful! Please login.");
                usernameField.setText("");
                passwordField.setText("");
                cardLayout.show(mainPanel, "login");
            } catch (SQLException ex) {
                if (ex.getMessage().contains("Duplicate")) {
                    JOptionPane.showMessageDialog(this, "Username already exists. Choose another.");
                } else {
                    JOptionPane.showMessageDialog(this, "Registration failed: " + ex.getMessage());
                }
            }
        }
    }

    class LoginPanel extends JPanel {
        JTextField usernameField = new JTextField(20);
        JPasswordField passwordField = new JPasswordField(20);
        JButton loginBtn = new JButton("Login");
        JButton toRegisterBtn = new JButton("New user? Register now");

        LoginPanel() {
            setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            gbc.gridx = 0; gbc.gridy = 0;
            add(new JLabel("Username:"), gbc);
            gbc.gridx = 1;
            add(usernameField, gbc);

            gbc.gridx = 0; gbc.gridy = 1;
            add(new JLabel("Password:"), gbc);
            gbc.gridx = 1;
            add(passwordField, gbc);

            gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
            add(loginBtn, gbc);

            gbc.gridy = 3;
            add(toRegisterBtn, gbc);

            loginBtn.addActionListener(e -> loginUser());
            toRegisterBtn.addActionListener(e -> cardLayout.show(mainPanel, "register"));
        }

        private void loginUser() {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username and password cannot be empty.");
                return;
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT id, password FROM users WHERE username = ?")) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String storedPassword = rs.getString("password");
                        if (storedPassword.equals(password)) {
                            currentUser = new User(rs.getInt("id"), username);
                            usernameField.setText("");
                            passwordField.setText("");
                            ((OrderPanel) mainPanel.getComponent(2)).setUser(currentUser);
                            cardLayout.show(mainPanel, "order");
                        } else {
                            JOptionPane.showMessageDialog(this, "Incorrect password.");
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, "User not found. Please register.");
                    }
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Login failed: " + ex.getMessage());
            }
        }
    }

    class OrderPanel extends JPanel {
        JTextField addressField = new JTextField(20);
        JComboBox<String> qtyBox = new JComboBox<>(new String[]{"1", "2"});
        JComboBox<String> canBox = new JComboBox<>(new String[]{"Aquafina", "Bisleri", "Bailey"});
        JComboBox<String> paymentBox = new JComboBox<>(new String[]{"Cash", "UPI", "Card"});
        JButton submitBtn = new JButton("Place Order & Show Bill");
        JLabel greetLabel = new JLabel();
        User user;

        OrderPanel() {
            setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(8,8,8,8);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
            greetLabel.setHorizontalAlignment(JLabel.CENTER);
            add(greetLabel, gbc);
            gbc.gridwidth = 1;
            gbc.anchor = GridBagConstraints.LINE_END;

            gbc.gridy = 1;
            add(new JLabel("Delivery Address:"), gbc);
            gbc.gridx = 1;
            add(addressField, gbc);

            gbc.gridx = 0; gbc.gridy = 2;
            add(new JLabel("Quantity of Can:"), gbc);
            gbc.gridx = 1;
            add(qtyBox, gbc);

            gbc.gridx = 0; gbc.gridy = 3;
            add(new JLabel("Name of Can:"), gbc);
            gbc.gridx = 1;
            add(canBox, gbc);

            gbc.gridx = 0; gbc.gridy = 4;
            add(new JLabel("Payment Mode:"), gbc);
            gbc.gridx = 1;
            add(paymentBox, gbc);

            gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
            add(submitBtn, gbc);

            submitBtn.addActionListener(e -> submitOrder());
        }

        void setUser(User u) {
            this.user = u;
            greetLabel.setText("Welcome, " + u.username + "!");
            addressField.setText("");
            qtyBox.setSelectedIndex(0);
            canBox.setSelectedIndex(0);
            paymentBox.setSelectedIndex(0);
        }

        private void submitOrder() {
            String address = addressField.getText().trim();
            int qty = Integer.parseInt((String) qtyBox.getSelectedItem());
            String can = (String) canBox.getSelectedItem();
            String payment = (String) paymentBox.getSelectedItem();
            String timeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            if (address.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter delivery address.");
                return;
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO orders (user_id, address, quantity, can_name, payment_mode, order_time) " +
                            "VALUES (?, ?, ?, ?, ?, ?)")) {
                ps.setInt(1, user.id);
                ps.setString(2, address);
                ps.setInt(3, qty);
                ps.setString(4, can);
                ps.setString(5, payment);
                ps.setString(6, timeStr);
                ps.executeUpdate();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Order failed: " + ex.getMessage());
                return;
            }

            String bill = "=== Waterman Bill ===\nUser: " + user.username +
                    "\nDelivery Address: " + address +
                    "\nQuantity: " + qty +
                    "\nCan: " + can +
                    "\nPayment: " + payment +
                    "\nOrder Time: " + timeStr +
                    "\nThank you for your order!";
            JOptionPane.showMessageDialog(this, bill);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new WatermanApp().setVisible(true);
        });
    }
}
