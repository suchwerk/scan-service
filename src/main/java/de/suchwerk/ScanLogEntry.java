package de.suchwerk;

public class ScanLogEntry {
    public final String timestamp;
    public final String command;
    public final String status;
    public final String file;

    public ScanLogEntry(String timestamp, String command, String status, String file) {
        this.timestamp = timestamp;
        this.command = command;
        this.status = status;
        this.file = file;
    }
}
