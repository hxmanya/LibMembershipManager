import service.AuthService;
import service.MembershipService;
import ui.MainWindow;

import javax.swing.*;

/**
 * Application entry point.
 * Launches the Swing GUI with login dialog.
 * Falls back to CLI if "--cli" argument is passed.
 */
public class Main {
    public static void main(String[] args) {
        AuthService auth = new AuthService();
        MembershipService service = new MembershipService();

        boolean useCli = args.length > 0 && args[0].equals("--cli");
        if (useCli) {
            // CLI mode (original ConsoleUI, now requires login too)
            System.out.print("Username: ");
            java.util.Scanner sc = new java.util.Scanner(System.in);
            String user = sc.nextLine().trim();
            System.out.print("Password: ");
            String pass = sc.nextLine().trim();
            if (!auth.login(user, pass)) {
                System.out.println("Invalid credentials.");
                System.exit(1);
            }
            System.out.println("Logged in as " + auth.getCurrentUser().getUsername()
                + " [" + auth.getCurrentUser().getRole() + "]");
            new ui.ConsoleUI(service, auth).start();
        } else {
            // GUI mode (default)
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) {}
            MainWindow.showLogin(service, auth);
        }
    }
}
