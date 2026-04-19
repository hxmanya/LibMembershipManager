package ui;

import model.Member;
import model.MembershipStatus;
import model.MembershipType;
import service.AuthService;
import service.MembershipService;
import util.Validator;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

public class ConsoleUI {

    private static final String DIVIDER  = "─".repeat(75);
    private static final String DIVIDER2 = "═".repeat(75);

    private final MembershipService service;
    private final AuthService auth;
    private final Scanner scanner = new Scanner(System.in);

    public ConsoleUI(MembershipService service, AuthService auth) {
        this.service = service;
        this.auth    = auth;
    }

    public void start() {
        printBanner();
        boolean running = true;
        while (running) {
            printMainMenu();
            String choice = prompt("Enter choice");
            switch (choice.trim()) {
                case "1" -> adminOnly(() -> registerMember());
                case "2" -> viewAllMembers();
                case "3" -> searchMember();
                case "4" -> adminOnly(() -> updateMember());
                case "5" -> adminOnly(() -> renewMembership());
                case "6" -> adminOnly(() -> cancelMembership());
                case "7" -> adminOnly(() -> deleteMember());
                case "8" -> viewSummary();
                case "9" -> exportImportMenu();
                case "0" -> running = false;
                default  -> warn("Invalid choice. Please try again.");
            }
        }
        System.out.println("\n  Goodbye! Thank you for using the Library Membership System.\n");
    }

    private void adminOnly(Runnable action) {
        if (!auth.isAdmin()) { warn("Access denied. Admin role required."); return; }
        action.run();
    }

    // ─── MAIN MENU ──────────────────────────────────────────────────────────────

    private void printBanner() {
        System.out.println();
        System.out.println(DIVIDER2);
        System.out.println("      📚  LIBRARY MEMBERSHIP MANAGEMENT SYSTEM  📚");
        System.out.println(DIVIDER2);
    }

    private void printMainMenu() {
        System.out.println("\n" + DIVIDER);
        System.out.println("  MAIN MENU");
        System.out.println(DIVIDER);
        System.out.println("  [1] Register New Member");
        System.out.println("  [2] View All Members");
        System.out.println("  [3] Search Member");
        System.out.println("  [4] Update Member Details");
        System.out.println("  [5] Renew Membership");
        System.out.println("  [6] Cancel Membership");
        System.out.println("  [7] Delete Member Record");
        System.out.println("  [8] Membership Summary / Statistics");
        System.out.println("  [9] Export / Import Data");
        System.out.println("  [0] Exit");
        System.out.println(DIVIDER);
    }

    // ─── REGISTER ───────────────────────────────────────────────────────────────

    private void registerMember() {
        sectionHeader("REGISTER NEW MEMBER");
        String id = promptValidated("Member ID (letters/digits, 3–20 chars)",
                Validator::isValidId, "ID must be 3–20 alphanumeric characters.");
        if (service.findById(id).isPresent()) {
            warn("A member with ID '" + id + "' already exists.");
            return;
        }
        String name  = promptNotBlank("Full Name");
        String email = promptValidated("Email", Validator::isValidEmail, "Invalid email format.");
        String phone = promptValidated("Phone", Validator::isValidPhone, "Invalid phone format.");
        MembershipType type = chooseMembershipType();
        int months = chooseDuration();

        Member m = MembershipService.buildMember(id, name, email, phone,
                                                  type, LocalDate.now(), months);
        service.addMember(m);
        success("Member registered successfully!");
        printMemberCard(m);
    }

    // ─── VIEW ALL ────────────────────────────────────────────────────────────────

    private void viewAllMembers() {
        sectionHeader("ALL MEMBERS");
        List<Member> all = service.findAll();
        if (all.isEmpty()) { info("No members registered yet."); return; }

        System.out.printf("  %-10s %-20s %-8s %-9s %-10s%n",
                "ID", "Name", "Type", "Status", "Expiry");
        System.out.println("  " + "─".repeat(65));
        all.forEach(m -> System.out.println("  " + m.getDisplayInfo()));
        System.out.println(DIVIDER);
        info("Total: " + all.size() + " member(s)");
    }

    // ─── SEARCH ──────────────────────────────────────────────────────────────────

    private void searchMember() {
        sectionHeader("SEARCH MEMBER");
        System.out.println("  [1] Search by ID");
        System.out.println("  [2] Search by Name");
        System.out.println("  [3] Filter by Status");
        String choice = prompt("Choose search type");

        switch (choice.trim()) {
            case "1" -> {
                String id = prompt("Enter Member ID");
                Optional<Member> opt = service.findById(id.trim());
                if (opt.isPresent()) printMemberCard(opt.get());
                else warn("No member found with ID: " + id);
            }
            case "2" -> {
                String name = prompt("Enter name to search");
                List<Member> results = service.findByName(name.trim());
                if (results.isEmpty()) warn("No members found matching: " + name);
                else results.forEach(this::printMemberCard);
            }
            case "3" -> {
                MembershipStatus status = chooseStatus();
                List<Member> results = service.findByStatus(status);
                if (results.isEmpty()) info("No " + status + " members found.");
                else {
                    info(results.size() + " " + status + " member(s):");
                    results.forEach(m -> System.out.println("  " + m.getDisplayInfo()));
                }
            }
            default -> warn("Invalid choice.");
        }
    }

    // ─── UPDATE ──────────────────────────────────────────────────────────────────

    private void updateMember() {
        sectionHeader("UPDATE MEMBER");
        String id = prompt("Enter Member ID to update");
        Optional<Member> opt = service.findById(id.trim());
        if (opt.isEmpty()) { warn("Member not found: " + id); return; }

        Member m = opt.get();
        printMemberCard(m);
        info("Leave any field blank to keep the current value.");

        String name  = prompt("New Name  [" + m.getName()  + "]");
        String email = prompt("New Email [" + m.getEmail() + "]");
        String phone = prompt("New Phone [" + m.getPhone() + "]");

        if (!email.isBlank() && !Validator.isValidEmail(email)) {
            warn("Invalid email. Update aborted."); return;
        }
        if (!phone.isBlank() && !Validator.isValidPhone(phone)) {
            warn("Invalid phone. Update aborted."); return;
        }

        System.out.println("  Change membership type? Current: " + m.getMembershipType());
        String changetype = prompt("  Enter Y to change, or press Enter to skip");
        MembershipType type = changetype.equalsIgnoreCase("y") ? chooseMembershipType() : null;

        service.updateMember(id.trim(),
            name.isBlank()  ? null : name,
            email.isBlank() ? null : email,
            phone.isBlank() ? null : phone,
            type);
        success("Member updated successfully!");
        service.findById(id.trim()).ifPresent(this::printMemberCard);
    }

    // ─── RENEW ───────────────────────────────────────────────────────────────────

    private void renewMembership() {
        sectionHeader("RENEW MEMBERSHIP");
        String id = prompt("Enter Member ID");
        Optional<Member> opt = service.findById(id.trim());
        if (opt.isEmpty()) { warn("Member not found: " + id); return; }

        printMemberCard(opt.get());
        int months = chooseDuration();
        try {
            service.renewMembership(id.trim(), months);
            success("Membership renewed for " + months + " month(s).");
            service.findById(id.trim()).ifPresent(this::printMemberCard);
        } catch (IllegalStateException e) {
            warn(e.getMessage());
        }
    }

    // ─── CANCEL ──────────────────────────────────────────────────────────────────

    private void cancelMembership() {
        sectionHeader("CANCEL MEMBERSHIP");
        String id = prompt("Enter Member ID");
        Optional<Member> opt = service.findById(id.trim());
        if (opt.isEmpty()) { warn("Member not found: " + id); return; }

        printMemberCard(opt.get());
        String confirm = prompt("Are you sure you want to CANCEL this membership? (yes/no)");
        if (confirm.equalsIgnoreCase("yes")) {
            service.cancelMembership(id.trim());
            success("Membership cancelled.");
        } else {
            info("Cancellation aborted.");
        }
    }

    // ─── DELETE ──────────────────────────────────────────────────────────────────

    private void deleteMember() {
        sectionHeader("DELETE MEMBER RECORD");
        String id = prompt("Enter Member ID to delete");
        Optional<Member> opt = service.findById(id.trim());
        if (opt.isEmpty()) { warn("Member not found: " + id); return; }

        printMemberCard(opt.get());
        String confirm = prompt("This will permanently delete the record. Type DELETE to confirm");
        if (confirm.equals("DELETE")) {
            service.deleteMember(id.trim());
            success("Member record deleted.");
        } else {
            info("Deletion aborted.");
        }
    }

    // ─── SUMMARY ─────────────────────────────────────────────────────────────────

    private void viewSummary() {
        sectionHeader("MEMBERSHIP STATISTICS");
        Map<String, Long> stats = service.getStatusSummary();
        stats.forEach((k, v) -> System.out.printf("  %-12s : %d%n", k, v));
    }

    // ─── EXPORT / IMPORT ─────────────────────────────────────────────────────────

    private void exportImportMenu() {
        sectionHeader("EXPORT / IMPORT DATA");
        System.out.println("  [1] Export members to CSV");
        System.out.println("  [2] Import members from CSV");
        String choice = prompt("Choice");
        switch (choice.trim()) {
            case "1" -> {
                String path = prompt("Export file path (e.g., export.csv)");
                try {
                    service.exportToCsv(path.trim());
                    success("Data exported to: " + path.trim());
                } catch (Exception e) {
                    warn("Export failed: " + e.getMessage());
                }
            }
            case "2" -> {
                String path = prompt("Import file path (e.g., import.csv)");
                try {
                    int count = service.importFromCsv(path.trim());
                    success("Imported " + count + " new member(s) from: " + path.trim());
                } catch (Exception e) {
                    warn("Import failed: " + e.getMessage());
                }
            }
            default -> warn("Invalid choice.");
        }
    }

    // ─── HELPERS ─────────────────────────────────────────────────────────────────

    private MembershipType chooseMembershipType() {
        System.out.println("  Membership Types:");
        MembershipType[] types = MembershipType.values();
        for (int i = 0; i < types.length; i++) {
            System.out.printf("  [%d] %-10s — $%.0f/yr, borrow up to %d books%n",
                    i + 1, types[i].getLabel(), types[i].getAnnualFee(),
                    types[i].getBookBorrowLimit());
        }
        while (true) {
            String input = prompt("Choose type (1–" + types.length + ")");
            try {
                int idx = Integer.parseInt(input.trim()) - 1;
                if (idx >= 0 && idx < types.length) return types[idx];
            } catch (NumberFormatException ignored) {}
            warn("Invalid selection, try again.");
        }
    }

    private int chooseDuration() {
        System.out.println("  Duration options: [1] 1 month  [2] 3 months  [3] 6 months  [4] 12 months");
        int[] options = {1, 3, 6, 12};
        while (true) {
            String input = prompt("Choose duration (1–4)");
            try {
                int idx = Integer.parseInt(input.trim()) - 1;
                if (idx >= 0 && idx < options.length) return options[idx];
            } catch (NumberFormatException ignored) {}
            warn("Invalid selection, try again.");
        }
    }

    private MembershipStatus chooseStatus() {
        MembershipStatus[] statuses = MembershipStatus.values();
        for (int i = 0; i < statuses.length; i++) {
            System.out.println("  [" + (i + 1) + "] " + statuses[i]);
        }
        while (true) {
            String input = prompt("Choose status (1–" + statuses.length + ")");
            try {
                int idx = Integer.parseInt(input.trim()) - 1;
                if (idx >= 0 && idx < statuses.length) return statuses[idx];
            } catch (NumberFormatException ignored) {}
            warn("Invalid selection, try again.");
        }
    }

    private void printMemberCard(Member m) {
        m.refreshStatus();
        String statusIcon = switch (m.getStatus()) {
            case ACTIVE    -> "✅";
            case EXPIRED   -> "⚠️ ";
            case CANCELLED -> "❌";
        };
        System.out.println("  " + DIVIDER.substring(2));
        System.out.printf("  %s  %-20s  [%s]%n", statusIcon, m.getName(), m.getId());
        System.out.printf("  📧 %-25s  📞 %s%n", m.getEmail(), m.getPhone());
        System.out.printf("  📋 Type: %-10s  🗓 Joined: %s  🗓 Expires: %s%n",
                m.getMembershipType(), m.getJoinDate(), m.getExpiryDate());
        System.out.println("  " + DIVIDER.substring(2));
    }

    private String prompt(String label) {
        System.out.print("  » " + label + ": ");
        return scanner.nextLine();
    }

    private String promptNotBlank(String label) {
        while (true) {
            String val = prompt(label);
            if (Validator.isNotBlank(val)) return val.trim();
            warn("This field cannot be empty.");
        }
    }

    private String promptValidated(String label, java.util.function.Predicate<String> validator,
                                    String errorMsg) {
        while (true) {
            String val = prompt(label);
            if (validator.test(val.trim())) return val.trim();
            warn(errorMsg);
        }
    }

    private void sectionHeader(String title) {
        System.out.println("\n" + DIVIDER2);
        System.out.println("  " + title);
        System.out.println(DIVIDER2);
    }

    private void success(String msg) { System.out.println("\n  ✅  " + msg); }
    private void warn(String msg)    { System.out.println("\n  ⚠️   " + msg); }
    private void info(String msg)    { System.out.println("  ℹ️   " + msg); }
}
