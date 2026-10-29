package restaurant.ui;

import restaurant.model.User;
import restaurant.service.AuthService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginFrame extends JFrame {
    private final JTextField usernameField = new JTextField(22);
    private final JPasswordField passwordField = new JPasswordField(22);
    private final AuthService authService = new AuthService();

    public LoginFrame() {
        UITheme.install();
        setTitle("Table & Thyme | Sign in");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(560, 610);
        setMinimumSize(new Dimension(480, 500));
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.CANVAS);
        setLayout(new GridBagLayout());

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UITheme.LINE), new EmptyBorder(34, 42, 36, 42)));
        card.setPreferredSize(new Dimension(430, 480));
        JLabel brand = new JLabel("TABLE & THYME", SwingConstants.CENTER);
        brand.setFont(new Font("Segoe UI", Font.BOLD, 24)); brand.setForeground(UITheme.NAVY); brand.setAlignmentX(CENTER_ALIGNMENT);
        JLabel subtitle = new JLabel("RESTAURANT MANAGEMENT", SwingConstants.CENTER);
        subtitle.setFont(new Font("Segoe UI", Font.BOLD, 11)); subtitle.setForeground(UITheme.MUTED); subtitle.setAlignmentX(CENTER_ALIGNMENT);
        JLabel welcome = new JLabel("Welcome back", SwingConstants.CENTER);
        welcome.setFont(new Font("Segoe UI", Font.BOLD, 20)); welcome.setForeground(UITheme.INK); welcome.setAlignmentX(CENTER_ALIGNMENT);
        JLabel hint = new JLabel("Sign in to continue to your workspace", SwingConstants.CENTER);
        hint.setFont(UITheme.FONT); hint.setForeground(UITheme.MUTED); hint.setAlignmentX(CENTER_ALIGNMENT);
        card.add(Box.createVerticalGlue()); card.add(brand); card.add(Box.createVerticalStrut(7)); card.add(subtitle);
        card.add(Box.createVerticalStrut(26)); card.add(welcome); card.add(Box.createVerticalStrut(5)); card.add(hint); card.add(Box.createVerticalStrut(24));
        addField(card, "Username", usernameField);
        card.add(Box.createVerticalStrut(13));
        JLabel passwordCaption = new JLabel("Password"); passwordCaption.setFont(new Font("Segoe UI", Font.BOLD, 12)); passwordCaption.setForeground(UITheme.INK); passwordCaption.setAlignmentX(LEFT_ALIGNMENT);
        card.add(passwordCaption); card.add(Box.createVerticalStrut(6));
        JPanel passwordRow = new JPanel(new BorderLayout(8, 0)); passwordRow.setOpaque(false); passwordRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14)); passwordField.setPreferredSize(new Dimension(300, 44));
        JButton toggle = UITheme.button("Show", "secondary"); toggle.setPreferredSize(new Dimension(76, 42));
        toggle.addActionListener(e -> { boolean reveal = passwordField.getEchoChar() != (char) 0; passwordField.setEchoChar(reveal ? (char) 0 : UIManager.getLookAndFeelDefaults().get("PasswordField.echoChar") instanceof Character ch ? ch : '\u2022'); toggle.setText(reveal ? "Hide" : "Show"); });
        passwordRow.add(passwordField, BorderLayout.CENTER); passwordRow.add(toggle, BorderLayout.EAST); card.add(passwordRow);
        card.add(Box.createVerticalStrut(22));
        JButton login = UITheme.button("Sign in", "primary"); login.setFont(new Font("Segoe UI", Font.BOLD, 14)); login.setAlignmentX(CENTER_ALIGNMENT); login.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        login.addActionListener(e -> attemptLogin()); card.add(login); card.add(Box.createVerticalGlue());
        add(card);
        getRootPane().setDefaultButton(login);
    }

    private void addField(JPanel parent, String label, JTextField field) {
        JLabel caption = new JLabel(label); caption.setFont(new Font("Segoe UI", Font.BOLD, 12)); caption.setForeground(UITheme.INK); caption.setAlignmentX(LEFT_ALIGNMENT);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14)); field.setPreferredSize(new Dimension(320, 44)); field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        parent.add(caption); parent.add(Box.createVerticalStrut(6)); parent.add(field);
    }

    private void attemptLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password.", "Input required", JOptionPane.WARNING_MESSAGE); return;
        }
        try {
            User user = authService.login(username, password);
            if (user != null) { new DashboardFrame(authService).setVisible(true); dispose(); }
            else JOptionPane.showMessageDialog(this, "The username or password is incorrect.", "Sign in failed", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Unable to sign in: " + ex.getMessage(), "Connection error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
