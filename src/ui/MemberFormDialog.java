package ui;

import model.Member;
import model.MembershipType;
import util.Validator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;

public class MemberFormDialog extends JDialog {

    private boolean confirmed = false;
    private Member result = null;

    private final JTextField idField    = new JTextField();
    private final JTextField nameField  = new JTextField();
    private final JTextField emailField = new JTextField();
    private final JTextField phoneField = new JTextField();
    private final JComboBox<MembershipType> typeBox = new JComboBox<>(MembershipType.values());
    private final JSpinner durationSpinner = new JSpinner(new SpinnerNumberModel(12, 1, 120, 1));

    public MemberFormDialog(Frame owner, Member existing) {
        super(owner, existing == null ? "Register New Member" : "Edit Member", true);
        setSize(420, existing == null ? 380 : 330);
        setLocationRelativeTo(owner);
        setResizable(false);
        buildUI(existing);
    }

    private void buildUI(Member existing) {
        boolean isEdit = existing != null;

        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBorder(new EmptyBorder(20, 25, 15, 25));

        int rows = isEdit ? 5 : 6;
        JPanel form = new JPanel(new GridLayout(rows, 2, 10, 10));

        if (isEdit) {
            idField.setText(existing.getId());
            idField.setEditable(false);
            idField.setBackground(new Color(230, 230, 230));
        }

        form.add(label("Member ID *"));     form.add(idField);
        form.add(label("Full Name *"));     form.add(nameField);
        form.add(label("Email *"));         form.add(emailField);
        form.add(label("Phone *"));         form.add(phoneField);
        form.add(label("Membership Type")); form.add(typeBox);
        if (!isEdit) {
            form.add(label("Duration (months)")); form.add(durationSpinner);
        }

        if (isEdit) {
            nameField.setText(existing.getName());
            emailField.setText(existing.getEmail());
            phoneField.setText(existing.getPhone());
            typeBox.setSelectedItem(existing.getMembershipType());
        }

        root.add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton cancel = new JButton("Cancel");
        JButton save   = new JButton(existing == null ? "Register" : "Save Changes");
        save.setBackground(new Color(60, 130, 220));
        save.setForeground(Color.WHITE);
        save.setFocusPainted(false);
        save.setBorderPainted(false);

        cancel.addActionListener(e -> dispose());
        save.addActionListener(e -> onSave(existing));

        buttons.add(cancel);
        buttons.add(save);
        root.add(buttons, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void onSave(Member existing) {
        String id    = idField.getText().trim();
        String name  = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        MembershipType type = (MembershipType) typeBox.getSelectedItem();
        int months = (int) durationSpinner.getValue();

        if (!Validator.isValidId(id))    { error("Invalid ID (3–20 alphanumeric chars)."); return; }
        if (!Validator.isNotBlank(name)) { error("Name cannot be empty."); return; }
        if (!Validator.isValidEmail(email)) { error("Invalid email format."); return; }
        if (!Validator.isValidPhone(phone)) { error("Invalid phone format."); return; }

        LocalDate joinDate = existing != null ? existing.getJoinDate() : LocalDate.now();
        LocalDate expiry   = existing != null
                ? (existing.getExpiryDate().isAfter(LocalDate.now())
                ? existing.getExpiryDate().plusMonths(0)
                : LocalDate.now().plusMonths(months))
                : joinDate.plusMonths(months);

        result = new Member(id, name, email, phone, type, joinDate, expiry);
        confirmed = true;
        dispose();
    }

    public boolean isConfirmed() { return confirmed; }
    public Member getResult()    { return result; }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.PLAIN, 13));
        return l;
    }

    private void error(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Validation Error", JOptionPane.ERROR_MESSAGE);
    }
}