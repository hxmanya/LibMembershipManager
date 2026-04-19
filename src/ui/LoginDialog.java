package ui;

import service.AuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginDialog extends JDialog {

    private final AuthService authService;
    private boolean success = false;

    public LoginDialog(AuthService authService) {
        this.authService = authService;
        setTitle("Library System — Login");
        setModal(true);
        setSize(380, 260);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(30, 34, 45));
        root.setBorder(new EmptyBorder(20, 30, 20, 30));

        // Title
        JLabel title = new JLabel("📚 Library Membership System", SwingConstants.CENTER);
        title.setForeground(new Color(100, 180, 255));
        title.setFont(new Font("SansSerif", Font.BOLD, 15));
        title.setBorder(new EmptyBorder(0, 0, 18, 0));
        root.add(title, BorderLayout.NORTH);

        // Form
        JPanel form = new JPanel(new GridLayout(4, 1, 0, 10));
        form.setOpaque(false);

        JTextField userField = styledField("Username");
        JPasswordField passField = styledPassField("Password");
        JLabel errorLabel = new JLabel(" ");
        errorLabel.setForeground(new Color(255, 90, 90));
        errorLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JButton loginBtn = new JButton("Login");
        loginBtn.setBackground(new Color(60, 130, 220));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        loginBtn.setFocusPainted(false);
        loginBtn.setBorderPainted(false);
        loginBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        form.add(userField);
        form.add(passField);
        form.add(errorLabel);
        form.add(loginBtn);
        root.add(form, BorderLayout.CENTER);

        // Hint
        JLabel hint = new JLabel("Default: admin/admin123  or  librarian/lib123", SwingConstants.CENTER);
        hint.setForeground(new Color(120, 130, 150));
        hint.setFont(new Font("SansSerif", Font.ITALIC, 11));
        hint.setBorder(new EmptyBorder(12, 0, 0, 0));
        root.add(hint, BorderLayout.SOUTH);

        setContentPane(root);

        Runnable doLogin = () -> {
            String user = userField.getText().trim();
            String pass = new String(passField.getPassword());
            if (authService.login(user, pass)) {
                success = true;
                dispose();
            } else {
                errorLabel.setText("❌ Invalid username or password.");
                passField.setText("");
            }
        };

        loginBtn.addActionListener(e -> doLogin.run());
        passField.addActionListener(e -> doLogin.run());
        userField.addActionListener(e -> passField.requestFocus());
    }

    public boolean isSuccess() { return success; }

    private JTextField styledField(String placeholder) {
        JTextField f = new JTextField();
        f.putClientProperty("JTextField.placeholderText", placeholder);
        styleInput(f);
        return f;
    }

    private JPasswordField styledPassField(String placeholder) {
        JPasswordField f = new JPasswordField();
        f.putClientProperty("JTextField.placeholderText", placeholder);
        styleInput(f);
        return f;
    }

    private void styleInput(JTextField f) {
        f.setBackground(new Color(45, 50, 65));
        f.setForeground(Color.WHITE);
        f.setCaretColor(Color.WHITE);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(70, 80, 110)),
            new EmptyBorder(6, 10, 6, 10)
        ));
        f.setFont(new Font("SansSerif", Font.PLAIN, 13));
    }
}
