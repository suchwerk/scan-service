package de.suchwerk;

import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@ApplicationScoped
public class ScanLogger {
    private final List<ScanLogEntry> logs = Collections.synchronizedList(new ArrayList<>());
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void log(String command, String status, String file) {
        logs.add(0, new ScanLogEntry(
            LocalDateTime.now().format(formatter),
            command,
            status,
            file
        ));
        if (logs.size() > 50) logs.remove(logs.size() - 1);
    }

    public List<ScanLogEntry> getLogs() {
        return logs;
    }
}
