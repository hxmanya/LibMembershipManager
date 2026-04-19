package model;

public class UserAccount {

    private String username;
    private String passwordHash;
    private Role role;

    public UserAccount(String username, String passwordHash, Role role) {
        this.username     = username;
        this.passwordHash = passwordHash;
        this.role         = role;
    }

    public String getUsername()     { return username; }
    public String getPasswordHash() { return passwordHash; }
    public Role   getRole()         { return role; }

    public boolean isAdmin() { return role == Role.ADMIN; }

    public String toCsv() {
        return username + "," + passwordHash + "," + role.name();
    }

    public static UserAccount fromCsv(String line) {
        String[] p = line.split(",");
        return new UserAccount(p[0].trim(), p[1].trim(), Role.valueOf(p[2].trim()));
    }
}
