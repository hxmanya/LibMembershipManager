package model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Represents a library member.
 * CHILD class of Person — demonstrates INHERITANCE.
 * Uses private fields with getters/setters — ENCAPSULATION.
 */
public class Member extends Person {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private MembershipType membershipType;
    private MembershipStatus status;
    private LocalDate joinDate;
    private LocalDate expiryDate;

    public Member(String id, String name, String email, String phone,
                  MembershipType membershipType, LocalDate joinDate, LocalDate expiryDate) {
        super(id, name, email, phone);
        this.membershipType = membershipType;
        this.status = MembershipStatus.ACTIVE;
        this.joinDate = joinDate;
        this.expiryDate = expiryDate;
        refreshStatus();
    }

    // Getters
    public MembershipType getMembershipType() { return membershipType; }
    public MembershipStatus getStatus()       { return status; }
    public LocalDate getJoinDate()            { return joinDate; }
    public LocalDate getExpiryDate()          { return expiryDate; }

    // Setters
    public void setMembershipType(MembershipType membershipType) { this.membershipType = membershipType; }
    public void setStatus(MembershipStatus status)               { this.status = status; }
    public void setExpiryDate(LocalDate expiryDate)              { this.expiryDate = expiryDate; }

    public void refreshStatus() {
        if (status != MembershipStatus.CANCELLED) {
            status = LocalDate.now().isAfter(expiryDate)
                    ? MembershipStatus.EXPIRED
                    : MembershipStatus.ACTIVE;
        }
    }

    // Renew: extends from current expiry (or today if already expired)
    public void renew(int months) {
        LocalDate base = expiryDate.isAfter(LocalDate.now()) ? expiryDate : LocalDate.now();
        this.expiryDate = base.plusMonths(months);
        this.status = MembershipStatus.ACTIVE;
    }

    // Reactivate: starts fresh from today, can also change membership type
    public void reactivate(int months, MembershipType newType) {
        this.expiryDate = LocalDate.now().plusMonths(months);
        this.membershipType = newType;
        this.status = MembershipStatus.ACTIVE;
    }

    public void cancel() {
        this.status = MembershipStatus.CANCELLED;
    }

    // POLYMORPHISM: overrides abstract method from Person
    @Override
    public String getDisplayInfo() {
        return String.format(
            "ID: %-10s | Name: %-20s | Type: %-8s | Status: %-9s | Expiry: %s",
            getId(), getName(), membershipType.getLabel(), status, expiryDate.format(FMT)
        );
    }

    public String toCsv() {
        return String.join(",",
            getId(), getName(), getEmail(), getPhone(),
            membershipType.name(), status.name(),
            joinDate.format(FMT), expiryDate.format(FMT)
        );
    }

    public static Member fromCsv(String line) {
        String[] p = line.split(",", -1);
        if (p.length < 8) throw new IllegalArgumentException("Invalid CSV record: " + line);
        Member m = new Member(
            p[0].trim(), p[1].trim(), p[2].trim(), p[3].trim(),
            MembershipType.valueOf(p[4].trim()),
            LocalDate.parse(p[6].trim(), FMT),
            LocalDate.parse(p[7].trim(), FMT)
        );
        m.setStatus(MembershipStatus.valueOf(p[5].trim()));
        return m;
    }

    public static String csvHeader() {
        return "id,name,email,phone,membershipType,status,joinDate,expiryDate";
    }
}
