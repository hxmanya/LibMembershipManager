package util;

import model.Member;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class FileManager {

    private static final String DATA_DIR  = "data";
    private static final String DATA_FILE = DATA_DIR + "/members.csv";

    public static void ensureDataDirectory() {
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
        } catch (IOException e) {
            throw new RuntimeException("Cannot create data directory: " + e.getMessage());
        }
    }

    public static void saveMembers(List<Member> members) {
        ensureDataDirectory();
        try (PrintWriter pw = new PrintWriter(new FileWriter(DATA_FILE))) {
            pw.println(Member.csvHeader());
            for (Member m : members) pw.println(m.toCsv());
        } catch (IOException e) {
            throw new RuntimeException("Error saving data: " + e.getMessage());
        }
    }

    public static List<Member> loadMembers() {
        ensureDataDirectory();
        List<Member> members = new ArrayList<>();
        Path path = Paths.get(DATA_FILE);
        if (!Files.exists(path)) return members;

        try (BufferedReader br = new BufferedReader(new FileReader(DATA_FILE))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) { firstLine = false; continue; }
                if (line.trim().isEmpty()) continue;
                try {
                    members.add(Member.fromCsv(line));
                } catch (Exception e) {
                    System.err.println("Skipping invalid record: " + line);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error loading data: " + e.getMessage());
        }
        return members;
    }

    public static void exportToCsv(List<Member> members, String filePath) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath))) {
            pw.println(Member.csvHeader());
            for (Member m : members) pw.println(m.toCsv());
        }
    }

    public static List<Member> importFromCsv(String filePath) throws IOException {
        List<Member> members = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) { firstLine = false; continue; }
                if (line.trim().isEmpty()) continue;
                members.add(Member.fromCsv(line));
            }
        }
        return members;
    }
}
