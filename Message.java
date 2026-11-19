package quickchat;

import java.security.SecureRandom;
import java.util.Locale;

/** Represents one QuickChat message. */
public class Message {
    private static final int MAX_LENGTH = 250;
    private static final SecureRandom RANDOM = new SecureRandom();
    private final String messageID;
    private final int messageNumber;
    private final String recipient;
    private final String messageText;
    private final String messageHash;
    private String status = "Pending";

    public Message(int number, String recipient, String text) {
        this(number, recipient, text, generateID());
    }

    public Message(int number, String recipient, String text, String id) {
        this.messageNumber = number;
        this.recipient = recipient == null ? "" : recipient.trim();
        this.messageText = text == null ? "" : text.trim();
        this.messageID = id == null || id.trim().isEmpty() ? generateID() : id.trim();
        this.messageHash = createMessageHash();
    }

    public static String generateID() {
        long number = 1_000_000_000L + (long) (RANDOM.nextDouble() * 9_000_000_000L);
        return String.valueOf(number);
    }

    public String getMessageID() { return messageID; }
    public int getMessageNumber() { return messageNumber; }
    public String getRecipient() { return recipient; }
    public String getMessageText() { return messageText; }
    public String getMessageHash() { return messageHash; }
    public String getStatus() { return status; }
    public void setStatus(String status) { if (status != null) this.status = status; }

    public boolean checkMessageID() { return messageID.matches("\\d{1,10}"); }

    public static String checkRecipientCell(String cell) {
        if (cell != null) {
            String value = cell.trim();
            if (value.matches("\\+27[1-9]\\d{8}") || value.matches("0[1-9]\\d{8}"))
                return "Cell phone number successfully captured.";
        }
        return "Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.";
    }

    public static String validateMessageLength(String text) {
        if (text == null || text.trim().isEmpty()) return "Message cannot be empty.";
        int length = text.trim().length();
        if (length <= MAX_LENGTH) return "Message ready to send.";
        return "Message exceeds 250 characters by " + (length - MAX_LENGTH) + ", please reduce size.";
    }

    public String checkRecipientCell() { return checkRecipientCell(recipient); }
    public String validateMessage() { return validateMessageLength(messageText); }

    public final String createMessageHash() {
        String prefix = messageID.length() >= 2 ? messageID.substring(0, 2) : messageID;
        String trimmed = messageText.trim();
        if (trimmed.isEmpty()) return (prefix + ":" + messageNumber + ":").toUpperCase(Locale.ROOT);
        String[] words = trimmed.split("\\s+");
        String punctuation = "^[^\\p{L}\\p{N}]+|[^\\p{L}\\p{N}]+$";
        String first = words[0].replaceAll(punctuation, "");
        String last = words[words.length - 1].replaceAll(punctuation, "");
        return (prefix + ":" + messageNumber + ":" + first + last).toUpperCase(Locale.ROOT);
    }

    public String SentMessage(int choice) {
        switch (choice) {
            case 1: status = "Sent"; return "Message successfully sent.";
            case 2: status = "Discarded"; return "Message disregarded.";
            case 3: status = "Stored"; return "Message successfully stored.";
            default: return "Invalid option.";
        }
    }

    public String printMessages() {
        return "Message ID: " + messageID + "\nMessage Hash: " + messageHash
                + "\nRecipient: " + recipient + "\nMessage: " + messageText;
    }

    public String printMessageDetails() { return printMessages(); }
    public int returnTotalMessages() { return "Sent".equals(status) ? 1 : 0; }
    public String storeMessage() { status = "Stored"; return "Message successfully stored."; }

    public String toJson() {
        return "{\"messageID\":\"" + escape(messageID) + "\",\"messageNumber\":" + messageNumber
                + ",\"recipient\":\"" + escape(recipient) + "\",\"message\":\"" + escape(messageText)
                + "\",\"messageHash\":\"" + escape(messageHash) + "\"}";
    }

    static String escape(String value) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '\\': out.append("\\\\"); break;
                case '"': out.append("\\\""); break;
                case '\n': out.append("\\n"); break;
                case '\r': out.append("\\r"); break;
                case '\t': out.append("\\t"); break;
                case '\b': out.append("\\b"); break;
                case '\f': out.append("\\f"); break;
                default:
                    if (c < 0x20) out.append(String.format(Locale.ROOT, "\\u%04x", (int)c));
                    else out.append(c);
            }
        }
        return out.toString();
    }
}
