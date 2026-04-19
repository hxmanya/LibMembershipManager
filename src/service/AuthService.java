package service;

import model.Role;
import model.UserAccount;

import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.LinkedHashMap;
import java.util.Map;

public class AuthService {

    private static final String USERS_FILE = "data/users.csv";
    private final Map<String, UserAccount> users = new LinkedHashMap<>();
    private UserAccount loggedInUser = null;

    public AuthService() {
        loadUsers();
        // Create default admin if no users exist
        if (users.isEmpty()) {
            addUser("admin", "admin123", Role.ADMIN);
            addUser("librarian", "lib123", Role.USER);
            saveUsers();
            System.out.println("  Default accounts created: admin/admin123  |  librarian/lib123");
        }
    }

    public boolean login(String username, String password) {
        UserAccount account = users.get(username.toLowerCase());
        if (account != null && account.getPasswordHash().equals(hash(password))) {
            loggedInUser = account;
            return true;
        }
        return false;
    }

    public void logout() { loggedInUser = null; }

    public UserAccount getCurrentUser() { return loggedInUser; }

    public boolean isLoggedIn() { return loggedInUser != null; }

    public boolean isAdmin() { return isLoggedIn() && loggedInUser.isAdmin(); }

    public void addUser(String username, String password, Role role) {
        users.put(username.toLowerCase(), new UserAccount(username.toLowerCase(), hash(password), role));
        saveUsers();
    }

    public boolean userExists(String username) {
        return users.containsKey(username.toLowerCase());
    }

    // SHA-256 password hashing
    public static String hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Hashing failed", e);
        }
    }

    private void loadUsers() {
        try {
            Files.createDirectories(Paths.get("data"));
            Path path = Paths.get(USERS_FILE);
            if (!Files.exists(path)) return;
            for (String line : Files.readAllLines(path)) {
                if (line.isBlank() || line.startsWith("#")) continue;
                UserAccount u = UserAccount.fromCsv(line);
                users.put(u.getUsername(), u);
            }
        } catch (IOException e) {
            System.err.println("Could not load users: " + e.getMessage());
        }
    }

    private void saveUsers() {
        try {
            Files.createDirectories(Paths.get("data"));
            PrintWriter pw = new PrintWriter(new FileWriter(USERS_FILE));
            users.values().forEach(u -> pw.println(u.toCsv()));
            pw.close();
        } catch (IOException e) {
            System.err.println("Could not save users: " + e.getMessage());
        }
    }
}
