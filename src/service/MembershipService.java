package service;

import model.Member;
import model.MembershipStatus;
import model.MembershipType;
import util.DatabaseManager;
import util.FileManager;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class MembershipService {

    public MembershipService() {
        DatabaseManager.initialize();
    }

    public void addMember(Member member) {
        if (DatabaseManager.existsById(member.getId()))
            throw new IllegalArgumentException("Member ID already exists: " + member.getId());
        DatabaseManager.insertMember(member);
    }

    public Optional<Member> findById(String id) {
        Member m = DatabaseManager.selectById(id);
        if (m != null) m.refreshStatus();
        return Optional.ofNullable(m);
    }

    public List<Member> findAll() {
        List<Member> all = DatabaseManager.selectAll();
        all.forEach(Member::refreshStatus);
        return all;
    }

    public List<Member> findByName(String query) {
        String q = query.toLowerCase();
        return findAll().stream()
            .filter(m -> m.getName().toLowerCase().contains(q))
            .collect(Collectors.toList());
    }

    public List<Member> findByStatus(MembershipStatus status) {
        return findAll().stream().filter(m -> m.getStatus() == status).collect(Collectors.toList());
    }

    public void updateMember(String id, String name, String email, String phone, MembershipType type) {
        Member m = getOrThrow(id);
        if (name  != null && !name.isBlank())  m.setName(name);
        if (email != null && !email.isBlank()) m.setEmail(email);
        if (phone != null && !phone.isBlank()) m.setPhone(phone);
        if (type  != null)                     m.setMembershipType(type);
        DatabaseManager.updateMember(m);
    }

    public void renewMembership(String id, int months) {
        Member m = getOrThrow(id);
        m.renew(months);
        DatabaseManager.updateMember(m);
    }

    public void reactivateMembership(String id, int months, MembershipType newType) {
        Member m = getOrThrow(id);
        m.reactivate(months, newType);
        DatabaseManager.updateMember(m);
    }

    public void deleteMember(String id) {
        if (!DatabaseManager.existsById(id))
            throw new NoSuchElementException("Member not found: " + id);
        DatabaseManager.deleteMember(id);
    }

    public void cancelMembership(String id) {
        Member m = getOrThrow(id);
        m.cancel();
        DatabaseManager.updateMember(m);
    }

    public Map<String, Long> getStatusSummary() {
        List<Member> all = findAll();
        Map<String, Long> summary = new LinkedHashMap<>();
        for (MembershipStatus s : MembershipStatus.values())
            summary.put(s.name(), all.stream().filter(m -> m.getStatus() == s).count());
        summary.put("TOTAL", (long) all.size());
        return summary;
    }

    public void exportToCsv(String filePath) throws Exception {
        FileManager.exportToCsv(findAll(), filePath);
    }

    public int importFromCsv(String filePath) throws Exception {
        List<Member> imported = FileManager.importFromCsv(filePath);
        int count = 0;
        for (Member m : imported) {
            if (!DatabaseManager.existsById(m.getId())) {
                DatabaseManager.insertMember(m);
                count++;
            }
        }
        return count;
    }

    private Member getOrThrow(String id) {
        return findById(id).orElseThrow(() -> new NoSuchElementException("Member not found: " + id));
    }

    public static Member buildMember(String id, String name, String email,
                                     String phone, MembershipType type,
                                     LocalDate joinDate, int durationMonths) {
        return new Member(id, name, email, phone, type, joinDate, joinDate.plusMonths(durationMonths));
    }
}
