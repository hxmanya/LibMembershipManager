package model;

/**
 * Represents a library staff member.
 * Second CHILD class of Person — further demonstrates INHERITANCE and POLYMORPHISM.
 */
public class Staff extends Person {

    private String role;
    private String department;

    public Staff(String id, String name, String email, String phone,
                 String role, String department) {
        super(id, name, email, phone);
        this.role = role;
        this.department = department;
    }

    public String getRole()       { return role; }
    public String getDepartment() { return department; }
    public void setRole(String role)           { this.role = role; }
    public void setDepartment(String dept)     { this.department = dept; }

    // POLYMORPHISM: different display from Member
    @Override
    public String getDisplayInfo() {
        return String.format(
            "Staff ID: %-10s | Name: %-20s | Role: %-15s | Dept: %s",
            getId(), getName(), role, department
        );
    }
}
