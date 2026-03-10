package Validations;



import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AuditLog {

    public record AuditEntry(
            String timestamp,
            String action,
            String performer,
            String target,
            String details
    ) {
        public String format() {
            return String.format("[%s] %s | %s | %s | %s",
                    timestamp, action, performer, target, details);
        }
    }

    private final List<AuditEntry> entries;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public AuditLog() {
        this.entries = new ArrayList<>();
    }

    public void log(String action, String performer, String target, String details) {
        String timestamp = LocalDateTime.now().format(formatter);
        AuditEntry entry = new AuditEntry(timestamp, action, performer, target, details);
        entries.add(entry);
    }

    public List<AuditEntry> getAll() {
        return new ArrayList<>(entries);
    }

    public List<AuditEntry> getByPerformer(String performer) {
        return entries.stream()
                .filter(e -> e.performer.equalsIgnoreCase(performer))
                .collect(Collectors.toList());
    }

    public List<AuditEntry> getByAction(String action) {
        return entries.stream()
                .filter(e -> e.action.equalsIgnoreCase(action))
                .collect(Collectors.toList());
    }

    public void printLog() {
        if (entries.isEmpty()) {
            System.out.println("No audit entries found.");
            return;
        }

        System.out.println("\n" + FormatUtils.formatHeader("AUDIT LOG"));

        String[] headers = {"TIMESTAMP", "ACTION", "PERFORMER", "TARGET", "DETAILS"};
        List<String[]> rows = new ArrayList<>();

        for (AuditEntry entry : entries) {
            rows.add(new String[]{
                    entry.timestamp,
                    entry.action,
                    entry.performer,
                    FormatUtils.truncate(entry.target, 20),
                    FormatUtils.truncate(entry.details, 30)
            });
        }

        System.out.println(FormatUtils.formatTable(headers, rows));
        System.out.printf("\nTotal entries: %d\n", entries.size());
    }

    public void saveToFile(String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("TIMESTAMP,ACTION,PERFORMER,TARGET,DETAILS");
            for (AuditEntry entry : entries) {
                writer.printf("%s,%s,%s,%s,%s\n",
                        entry.timestamp,
                        entry.action,
                        entry.performer,
                        entry.target,
                        entry.details.replace(",", ";"));
            }
            System.out.println("Audit log saved to: " + filename);
        } catch (IOException e) {
            System.err.println("Error saving audit log: " + e.getMessage());
        }
    }
}