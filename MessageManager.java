package quickchat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Manages QuickChat message collections and JSON storage. */
public class MessageManager {
    private static final String SENT = "Sent";
    private static final String STORED = "Stored";
    private static final String DISCARDED = "Discarded";

    public final List<String> sentMessages = new ArrayList<>();
    public final List<String> disregardedMessages = new ArrayList<>();
    public final List<String> storedMessages = new ArrayList<>();
    public final List<String> messageHashes = new ArrayList<>();
    public final List<String> messageIDs = new ArrayList<>();
    private final List<Message> allMessages = new ArrayList<>();
    private final String sender;

    public MessageManager(String sender) { this.sender = sender == null ? "" : sender.trim(); }
    public int count() { return allMessages.size(); }

    private List<String> arrayFor(String status) {
        switch (status) {
            case SENT: return sentMessages;
            case STORED: return storedMessages;
            case DISCARDED: return disregardedMessages;
            default: return null;
        }
    }

    public void add(Message message) {
        if (message == null) return;
        allMessages.add(message);
        messageHashes.add(message.getMessageHash());
        messageIDs.add(message.getMessageID());
        List<String> target = arrayFor(message.getStatus());
        if (target != null) target.add(message.getMessageText());
    }

    public String displaySenderAndRecipient() {
        StringBuilder out = new StringBuilder();
        for (Message m : allMessages) if (STORED.equals(m.getStatus()))
            out.append("Sender: ").append(sender).append(" | Recipient: ").append(m.getRecipient()).append("\n");
        return out.length() == 0 ? "No stored messages." : out.toString();
    }

    private Message findLongest() {
        Message longest = null;
        for (Message m : allMessages)
            if (longest == null || m.getMessageText().length() > longest.getMessageText().length()) longest = m;
        return longest;
    }

    public String longestMessage() {
        Message m = findLongest();
        return m == null ? "No messages." : m.getMessageText();
    }
    public String longestMessageDetails() {
        Message m = findLongest();
        return m == null ? "No messages." : m.printMessages();
    }

    public String searchByMessageID(String id) {
        if (id == null) return "Message ID not found.";
        int index = messageIDs.indexOf(id.trim());
        if (index < 0) return "Message ID not found.";
        Message m = allMessages.get(index);
        return m.getRecipient() + ", " + m.getMessageText();
    }

    public List<String> searchByRecipient(String recipient) {
        List<String> found = new ArrayList<>();
        if (recipient == null) return found;
        for (Message m : allMessages) {
            boolean relevant = SENT.equals(m.getStatus()) || STORED.equals(m.getStatus());
            if (relevant && m.getRecipient().equals(recipient.trim())) found.add(m.getMessageText());
        }
        return found;
    }

    public String deleteByHash(String hash) {
        if (hash == null) return "Message hash not found.";
        int index = messageHashes.indexOf(hash.trim().toUpperCase(Locale.ROOT));
        if (index < 0) return "Message hash not found.";
        Message removed = allMessages.remove(index);
        messageHashes.remove(index);
        messageIDs.remove(index);
        List<String> source = arrayFor(removed.getStatus());
        if (source != null) source.remove(removed.getMessageText());
        return "Message: \"" + removed.getMessageText() + "\" successfully deleted.";
    }

    public String report() {
        if (allMessages.isEmpty()) return "No messages to report.";
        StringBuilder out = new StringBuilder("===== MESSAGE REPORT =====\n");
        for (Message m : allMessages) out.append("Message Hash: ").append(m.getMessageHash()).append("\n")
                .append("Message ID:   ").append(m.getMessageID()).append("\n")
                .append("Recipient:    ").append(m.getRecipient()).append("\n")
                .append("Message:      ").append(m.getMessageText()).append("\n")
                .append("Status:       ").append(m.getStatus()).append("\n--------------------------\n");
        return out.toString();
    }

    public String displayStored() {
        StringBuilder out = new StringBuilder();
        for (Message m : allMessages) if (STORED.equals(m.getStatus())) out.append(m.printMessages()).append("\n\n");
        return out.length() == 0 ? "No stored messages." : out.toString();
    }

    public String recentlySent() {
        StringBuilder out = new StringBuilder();
        for (Message m : allMessages) if (SENT.equals(m.getStatus()))
            out.append("Sender: ").append(sender).append(" | Recipient: ").append(m.getRecipient())
                    .append(" | Message: ").append(m.getMessageText()).append("\n");
        return out.length() == 0 ? "No sent messages yet." : out.toString();
    }

    public void saveStored(Path file) throws IOException {
        List<String> objects = new ArrayList<>();
        for (Message m : allMessages) if (STORED.equals(m.getStatus())) objects.add("  " + m.toJson());
        String json = "[\n" + String.join(",\n", objects) + "\n]\n";
        if (file.getParent() != null) Files.createDirectories(file.getParent());
        Files.write(file, json.getBytes(StandardCharsets.UTF_8));
    }

    public int loadStored(Path file) throws IOException {
        if (!Files.exists(file)) return 0;
        String json = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
        int loaded = 0;
        for (String object : splitObjects(json)) {
            String id = getString(object, "messageID");
            String recipient = getString(object, "recipient");
            String text = getString(object, "message");
            int number = getNumber(object, "messageNumber");
            if (id == null || recipient == null || text == null || messageIDs.contains(id)) continue;
            Message message = new Message(number, recipient, text, id);
            message.setStatus(STORED);
            add(message);
            loaded++;
        }
        return loaded;
    }

    private static List<String> splitObjects(String json) {
        List<String> objects = new ArrayList<>();
        boolean inString = false, escaped = false;
        int depth = 0, start = -1;
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (inString) {
                if (escaped) escaped = false;
                else if (c == '\\') escaped = true;
                else if (c == '"') inString = false;
            } else if (c == '"') inString = true;
            else if (c == '{') { if (depth++ == 0) start = i; }
            else if (c == '}' && depth > 0) { if (--depth == 0 && start >= 0) { objects.add(json.substring(start, i + 1)); start = -1; } }
        }
        return objects;
    }

    private static String getString(String object, String key) {
        String regex = "\\\"" + Pattern.quote(key) + "\\\"\\s*:\\s*\\\"((?:\\\\\\.|[^\\\"\\\\])*)\\\"";
        Matcher matcher = Pattern.compile(regex).matcher(object);
        return matcher.find() ? unescape(matcher.group(1)) : null;
    }

    private static int getNumber(String object, String key) {
        String regex = "\\\"" + Pattern.quote(key) + "\\\"\\s*:\\s*(\\d+)";
        Matcher matcher = Pattern.compile(regex).matcher(object);
        if (!matcher.find()) return 0;
        try { return Integer.parseInt(matcher.group(1)); }
        catch (NumberFormatException ex) { return 0; }
    }

    private static String unescape(String text) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c != '\\' || i + 1 >= text.length()) { out.append(c); continue; }
            char next = text.charAt(++i);
            switch (next) {
                case '"': out.append('"'); break;
                case '\\': out.append('\\'); break;
                case '/': out.append('/'); break;
                case 'b': out.append('\b'); break;
                case 'f': out.append('\f'); break;
                case 'n': out.append('\n'); break;
                case 'r': out.append('\r'); break;
                case 't': out.append('\t'); break;
                case 'u':
                    if (i + 4 < text.length()) {
                        try { out.append((char) Integer.parseInt(text.substring(i + 1, i + 5), 16)); i += 4; }
                        catch (NumberFormatException ex) { out.append("\\u"); }
                    } else out.append("\\u");
                    break;
                default: out.append(next);
            }
        }
        return out.toString();
    }
}
