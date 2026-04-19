package ui;

import model.Member;
import model.MembershipStatus;
import model.MembershipType;
import service.AuthService;
import service.MembershipService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Map;

public class MainWindow extends JFrame {

    private final MembershipService service;
    private final AuthService auth;

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JLabel statusBar;

    private static final Color BG      = new Color(245, 247, 252);
    private static final Color SIDEBAR = new Color(30, 38, 55);
    private static final Color ACCENT  = new Color(60, 130, 220);
    private static final Color TEXT_LT = new Color(240, 242, 248);

    public MainWindow(MembershipService service, AuthService auth) {
        this.service = service;
        this.auth    = auth;
        setTitle("📚 Library Membership System — "
                + auth.getCurrentUser().getUsername()
                + " (" + auth.getCurrentUser().getRole() + ")");
        setSize(1100, 680);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        buildUI();
        loadTable(service.findAll());
    }

    // ── BUILD UI ─────────────────────────────────────────────────────────────

    private void buildUI() {
        setLayout(new BorderLayout());
        add(buildSidebar(), BorderLayout.WEST);
        add(buildContent(), BorderLayout.CENTER);
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(SIDEBAR);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(200, 0));
        sidebar.setBorder(new EmptyBorder(20, 12, 20, 12));

        JLabel appTitle = new JLabel("<html><center>📚<br>Library<br>System</center></html>");
        appTitle.setForeground(new Color(100, 180, 255));
        appTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        appTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        appTitle.setBorder(new EmptyBorder(0, 0, 20, 0));
        sidebar.add(appTitle);

        boolean isAdmin = auth.isAdmin();

        sidebar.add(sideBtn("➕  Register Member",  isAdmin, e -> registerMember()));
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(sideBtn("✏️  Edit Member",       isAdmin, e -> editMember()));
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(sideBtn("🔄  Renew Membership",  isAdmin, e -> renewMembership()));
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(sideBtn("❌  Cancel Membership", isAdmin, e -> cancelMembership()));
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(sideBtn("♻️  Reactivate Member", isAdmin, e -> reactivateMembership()));
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(sideBtn("🗑️  Delete Member",     isAdmin, e -> deleteMember()));
        sidebar.add(Box.createVerticalStrut(20));
        sidebar.add(sideBtn("📤  Export CSV",         true,   e -> exportCsv()));
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(sideBtn("📥  Import CSV",         isAdmin, e -> importCsv()));
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(sideBtn("📊  Statistics",         true,   e -> showStats()));
        sidebar.add(Box.createVerticalGlue());

        JButton logoutBtn = new JButton("🚪 Logout");
        logoutBtn.setBackground(new Color(180, 50, 50));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        logoutBtn.setFocusPainted(false);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoutBtn.setMaximumSize(new Dimension(176, 34));
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> logout());
        sidebar.add(logoutBtn);

        return sidebar;
    }

    private JPanel buildContent() {
        JPanel content = new JPanel(new BorderLayout(0, 8));
        content.setBackground(BG);
        content.setBorder(new EmptyBorder(16, 16, 8, 16));

        // Search bar
        JPanel topBar = new JPanel(new BorderLayout(10, 0));
        topBar.setOpaque(false);

        searchField = new JTextField();
        searchField.putClientProperty("JTextField.placeholderText", "🔍  Search by name or ID...");
        searchField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 210, 230)),
            new EmptyBorder(6, 10, 6, 10)
        ));
        searchField.addActionListener(e -> doSearch());

        JButton searchBtn = new JButton("Search");
        searchBtn.setBackground(ACCENT);
        searchBtn.setForeground(Color.WHITE);
        searchBtn.setFocusPainted(false);
        searchBtn.setBorderPainted(false);
        searchBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        searchBtn.addActionListener(e -> doSearch());

        JButton showAllBtn = new JButton("Show All");
        showAllBtn.setFocusPainted(false);
        showAllBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        showAllBtn.addActionListener(e -> loadTable(service.findAll()));

        JPanel searchBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        searchBtns.setOpaque(false);
        searchBtns.add(searchBtn);
        searchBtns.add(showAllBtn);

        topBar.add(searchField, BorderLayout.CENTER);
        topBar.add(searchBtns, BorderLayout.EAST);
        content.add(topBar, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Name", "Email", "Phone", "Type", "Status", "Joined", "Expires"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(28);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(210, 220, 240));
        table.setSelectionBackground(new Color(195, 220, 255));
        table.setGridColor(new Color(220, 225, 240));
        table.setDefaultRenderer(Object.class, new StatusCellRenderer());
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && auth.isAdmin()) editMember();
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230)));
        content.add(scrollPane, BorderLayout.CENTER);

        statusBar = new JLabel("  Ready");
        statusBar.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statusBar.setForeground(new Color(100, 110, 130));
        statusBar.setBorder(new EmptyBorder(4, 0, 0, 0));
        content.add(statusBar, BorderLayout.SOUTH);

        return content;
    }

    // ── TABLE ────────────────────────────────────────────────────────────────

    private void loadTable(List<Member> members) {
        tableModel.setRowCount(0);
        for (Member m : members) {
            tableModel.addRow(new Object[]{
                m.getId(), m.getName(), m.getEmail(), m.getPhone(),
                m.getMembershipType(), m.getStatus(),
                m.getJoinDate(), m.getExpiryDate()
            });
        }
        setStatus("Showing " + members.size() + " member(s).");
    }

    // ── ACTIONS ──────────────────────────────────────────────────────────────

    private void registerMember() {
        MemberFormDialog dlg = new MemberFormDialog(this, null);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            try {
                service.addMember(dlg.getResult());
                loadTable(service.findAll());
                setStatus("✅ Member registered: " + dlg.getResult().getName());
            } catch (Exception ex) {
                error(ex.getMessage());
            }
        }
    }

    private void editMember() {
        Member selected = getSelectedMember();
        if (selected == null) { info("Please select a member from the table."); return; }
        MemberFormDialog dlg = new MemberFormDialog(this, selected);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            Member r = dlg.getResult();
            service.updateMember(r.getId(), r.getName(), r.getEmail(), r.getPhone(), r.getMembershipType());
            loadTable(service.findAll());
            setStatus("✅ Member updated: " + r.getName());
        }
    }

    private void renewMembership() {
        Member m = getSelectedMember();
        if (m == null) { info("Please select a member."); return; }
        if (m.getStatus() == MembershipStatus.CANCELLED) {
            info("This membership is cancelled. Use ♻️ Reactivate Member instead.");
            return;
        }
        String[] options = {"1 month", "3 months", "6 months", "12 months"};
        int[] months = {1, 3, 6, 12};
        int choice = JOptionPane.showOptionDialog(this,
            "Renew membership for: " + m.getName(),
            "Renew Membership", JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE, null, options, options[3]);
        if (choice >= 0) {
            service.renewMembership(m.getId(), months[choice]);
            loadTable(service.findAll());
            setStatus("✅ Membership renewed for " + months[choice] + " month(s).");
        }
    }

    private void cancelMembership() {
        Member m = getSelectedMember();
        if (m == null) { info("Please select a member."); return; }
        if (m.getStatus() == MembershipStatus.CANCELLED) {
            info("This membership is already cancelled."); return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
            "Cancel membership for: " + m.getName() + "?",
            "Confirm Cancel", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            service.cancelMembership(m.getId());
            loadTable(service.findAll());
            setStatus("⚠️ Membership cancelled: " + m.getName());
        }
    }

    private void reactivateMembership() {
        Member m = getSelectedMember();
        if (m == null) { info("Please select a member."); return; }
        if (m.getStatus() != MembershipStatus.CANCELLED) {
            info("This member is not cancelled. Use 🔄 Renew Membership instead."); return;
        }

        // Build dialog with duration + type selector
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] durationLabels = {"1 month", "3 months", "6 months", "12 months"};
        int[] months = {1, 3, 6, 12};

        JComboBox<String> durationBox = new JComboBox<>(durationLabels);
        durationBox.setSelectedIndex(3); // default 12 months

        JComboBox<MembershipType> typeBox = new JComboBox<>(MembershipType.values());
        typeBox.setSelectedItem(m.getMembershipType()); // default = current type

        panel.add(new JLabel("Member:"));
        panel.add(new JLabel(m.getName()));
        panel.add(new JLabel("New Duration:"));
        panel.add(durationBox);
        panel.add(new JLabel("Membership Type:"));
        panel.add(typeBox);

        int result = JOptionPane.showConfirmDialog(this, panel,
            "Reactivate Membership", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            int selectedMonths = months[durationBox.getSelectedIndex()];
            MembershipType selectedType = (MembershipType) typeBox.getSelectedItem();
            service.reactivateMembership(m.getId(), selectedMonths, selectedType);
            loadTable(service.findAll());
            setStatus("♻️ Membership reactivated: " + m.getName()
                + " | " + selectedType + " | " + selectedMonths + " month(s) from today");
        }
    }

    private void deleteMember() {
        Member m = getSelectedMember();
        if (m == null) { info("Please select a member."); return; }
        String input = JOptionPane.showInputDialog(this,
            "Type DELETE to confirm removing: " + m.getName(),
            "Delete Member", JOptionPane.WARNING_MESSAGE);
        if ("DELETE".equals(input)) {
            service.deleteMember(m.getId());
            loadTable(service.findAll());
            setStatus("🗑️ Member deleted: " + m.getName());
        }
    }

    private void exportCsv() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new java.io.File("members_export.csv"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                service.exportToCsv(fc.getSelectedFile().getAbsolutePath());
                setStatus("📤 Exported to: " + fc.getSelectedFile().getName());
                info("Export successful!");
            } catch (Exception ex) { error(ex.getMessage()); }
        }
    }

    private void importCsv() {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                int count = service.importFromCsv(fc.getSelectedFile().getAbsolutePath());
                loadTable(service.findAll());
                setStatus("📥 Imported " + count + " new member(s).");
                info("Import successful! " + count + " member(s) added.");
            } catch (Exception ex) { error(ex.getMessage()); }
        }
    }

    private void showStats() {
        Map<String, Long> stats = service.getStatusSummary();
        StringBuilder sb = new StringBuilder("<html><body style='padding:10px;font-size:13px'>");
        sb.append("<b>Membership Statistics</b><br><br>");
        stats.forEach((k, v) -> sb.append(String.format("%-12s : %d<br>", k, v)));
        sb.append("</body></html>");
        JOptionPane.showMessageDialog(this, sb.toString(), "Statistics", JOptionPane.INFORMATION_MESSAGE);
    }

    private void doSearch() {
        String query = searchField.getText().trim();
        if (query.isBlank()) { loadTable(service.findAll()); return; }
        List<Member> byName = service.findByName(query);
        if (byName.isEmpty()) {
            service.findById(query).ifPresentOrElse(
                m -> loadTable(List.of(m)),
                () -> { loadTable(List.of()); setStatus("No results for: " + query); }
            );
        } else {
            loadTable(byName);
        }
    }

    private void logout() {
        auth.logout();
        dispose();
        showLogin(service, auth);
    }

    // ── HELPERS ──────────────────────────────────────────────────────────────

    private Member getSelectedMember() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        String id = (String) tableModel.getValueAt(row, 0);
        return service.findById(id).orElse(null);
    }

    private JButton sideBtn(String text, boolean enabled, ActionListener al) {
        JButton btn = new JButton(text);
        btn.setBackground(enabled ? new Color(50, 65, 90) : new Color(40, 44, 55));
        btn.setForeground(enabled ? TEXT_LT : new Color(100, 110, 130));
        btn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(176, 34));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setEnabled(enabled);
        if (enabled) {
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.addActionListener(al);
        }
        return btn;
    }

    private void setStatus(String msg) { statusBar.setText("  " + msg); }

    private void error(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void info(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    // ── STATUS COLOR RENDERER ────────────────────────────────────────────────

    private static class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v,
                boolean sel, boolean foc, int row, int col) {
            super.getTableCellRendererComponent(t, v, sel, foc, row, col);
            if (col == 5 && v != null) {
                String s = v.toString();
                if (!sel) setForeground(switch (s) {
                    case "ACTIVE"    -> new Color(30, 140, 60);
                    case "EXPIRED"   -> new Color(200, 100, 20);
                    case "CANCELLED" -> new Color(180, 40, 40);
                    default          -> Color.BLACK;
                });
                setFont(getFont().deriveFont(Font.BOLD));
            } else if (!sel) {
                setForeground(Color.BLACK);
                setFont(getFont().deriveFont(Font.PLAIN));
            }
            return this;
        }
    }

    // ── STATIC LAUNCHER ──────────────────────────────────────────────────────

    public static void showLogin(MembershipService service, AuthService auth) {
        LoginDialog login = new LoginDialog(auth);
        login.setVisible(true);
        if (login.isSuccess()) {
            SwingUtilities.invokeLater(() -> new MainWindow(service, auth).setVisible(true));
        } else {
            System.exit(0);
        }
    }
}
