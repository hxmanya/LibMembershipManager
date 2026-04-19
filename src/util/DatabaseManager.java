package util;

import model.Member;
import model.MembershipStatus;
import model.MembershipType;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles all SQLite database operations using JDBC.
 * Replaces CSV as the primary data store.
 * CSV is still used for export/import only.
 */
public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:data/library.db";

    // Called once at startup — creates the tables if they don't exist
    public static void initialize() {
        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS members (
                    id              TEXT PRIMARY KEY,
                    name            TEXT NOT NULL,
                    email           TEXT NOT NULL,
                    phone           TEXT NOT NULL,
                    membershipType  TEXT NOT NULL,
                    status          TEXT NOT NULL,
                    joinDate        TEXT NOT NULL,
                    expiryDate      TEXT NOT NULL
                )
            """);
        } catch (SQLException e) {
            throw new RuntimeException("DB init failed: " + e.getMessage(), e);
        }
    }

    public static Connection connect() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite driver not found. Make sure sqlite-jdbc jar is in your classpath.", e);
        }
        return DriverManager.getConnection(DB_URL);
    }

    // INSERT
    public static void insertMember(Member m) {
        String sql = "INSERT INTO members VALUES (?,?,?,?,?,?,?,?)";
        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, m);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Insert failed: " + e.getMessage(), e);
        }
    }

    // SELECT ALL
    public static List<Member> selectAll() {
        List<Member> list = new ArrayList<>();
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM members")) {
            while (rs.next()) list.add(fromResultSet(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Select failed: " + e.getMessage(), e);
        }
        return list;
    }

    // SELECT BY ID
    public static Member selectById(String id) {
        String sql = "SELECT * FROM members WHERE id = ?";
        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? fromResultSet(rs) : null;
        } catch (SQLException e) {
            throw new RuntimeException("Select by ID failed: " + e.getMessage(), e);
        }
    }

    // UPDATE
    public static void updateMember(Member m) {
        String sql = """
            UPDATE members SET name=?, email=?, phone=?,
            membershipType=?, status=?, joinDate=?, expiryDate=?
            WHERE id=?
        """;
        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, m.getName());
            ps.setString(2, m.getEmail());
            ps.setString(3, m.getPhone());
            ps.setString(4, m.getMembershipType().name());
            ps.setString(5, m.getStatus().name());
            ps.setString(6, m.getJoinDate().toString());
            ps.setString(7, m.getExpiryDate().toString());
            ps.setString(8, m.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Update failed: " + e.getMessage(), e);
        }
    }

    // DELETE
    public static void deleteMember(String id) {
        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM members WHERE id=?")) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Delete failed: " + e.getMessage(), e);
        }
    }

    public static boolean existsById(String id) {
        return selectById(id) != null;
    }

    private static void setParams(PreparedStatement ps, Member m) throws SQLException {
        ps.setString(1, m.getId());
        ps.setString(2, m.getName());
        ps.setString(3, m.getEmail());
        ps.setString(4, m.getPhone());
        ps.setString(5, m.getMembershipType().name());
        ps.setString(6, m.getStatus().name());
        ps.setString(7, m.getJoinDate().toString());
        ps.setString(8, m.getExpiryDate().toString());
    }

    private static Member fromResultSet(ResultSet rs) throws SQLException {
        Member m = new Member(
            rs.getString("id"),
            rs.getString("name"),
            rs.getString("email"),
            rs.getString("phone"),
            MembershipType.valueOf(rs.getString("membershipType")),
            LocalDate.parse(rs.getString("joinDate")),
            LocalDate.parse(rs.getString("expiryDate"))
        );
        m.setStatus(MembershipStatus.valueOf(rs.getString("status")));
        return m;
    }
}
