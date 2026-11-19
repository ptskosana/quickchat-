package quickchat;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

/** Console entry point for QuickChat. */
public class Main {
    private static final java.nio.file.Path STORED_FILE = Paths.get("stored_messages.json");

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Login login = new Login();

        System.out.println("=== QUICKCHAT REGISTRATION ===");
        System.out.print("Enter first name: "); login.setFirstName(scanner.nextLine());
        System.out.print("Enter last name: "); login.setLastName(scanner.nextLine());

        String username, password, cell, result;
        do {
            System.out.print("Enter username (max 5 characters and must contain _): "); username = scanner.nextLine();
            System.out.print("Enter password (8+ characters, uppercase, lowercase, number and special character): "); password = scanner.nextLine();
            System.out.print("Enter SA cellphone number (+27XXXXXXXXX): "); cell = scanner.nextLine();
            result = login.registerUser(username, password, cell);
            System.out.println(result);
        } while (!result.equals("Username and password successfully captured."));

        System.out.println("\n=== LOGIN ===");
        System.out.print("Username: "); String loginUsername = scanner.nextLine();
        System.out.print("Password: "); String loginPassword = scanner.nextLine();
        boolean loggedIn = login.loginUser(loginUsername, loginPassword);
        System.out.println(login.returnLoginStatus(loggedIn));
        if (!loggedIn) { scanner.close(); return; }

        String sender = login.getFirstName() + " " + login.getLastName();
        MessageManager manager = new MessageManager(sender);
        try { System.out.println("Loaded stored messages: " + manager.loadStored(STORED_FILE)); }
        catch (IOException ex) { System.out.println("Could not load stored messages: " + ex.getMessage()); }

        boolean running = true;
        while (running) {
            System.out.println("\n=== QUICKCHAT MENU ===");
            System.out.println("1. Send messages");
            System.out.println("2. Show recently sent messages");
            System.out.println("3. Part 3 message tools");
            System.out.println("4. Quit");
            int choice = readInt(scanner, "Choose an option: ");
            switch (choice) {
                case 1: sendMessages(scanner, manager); break;
                case 2: System.out.println(manager.recentlySent()); break;
                case 3: partThreeMenu(scanner, manager); break;
                case 4:
                    try { manager.saveStored(STORED_FILE); System.out.println("Stored messages saved."); }
                    catch (IOException ex) { System.out.println("Could not save stored messages: " + ex.getMessage()); }
                    running = false;
                    break;
                default: System.out.println("Invalid choice.");
            }
        }
        scanner.close();
    }

    private static void sendMessages(Scanner scanner, MessageManager manager) {
        int amount = readInt(scanner, "How many messages would you like to enter? ");
        if (amount < 1) { System.out.println("Enter at least one message."); return; }
        for (int i = 0; i < amount; i++) {
            System.out.println("\n--- Message " + (i + 1) + " ---");
            String recipient;
            do {
                System.out.print("Recipient cellphone (+27XXXXXXXXX): "); recipient = scanner.nextLine();
                System.out.println(Message.checkRecipientCell(recipient));
            } while (!Message.checkRecipientCell(recipient).equals("Cell phone number successfully captured."));
            String text;
            do {
                System.out.print("Message (maximum 250 characters): "); text = scanner.nextLine();
                System.out.println(Message.validateMessageLength(text));
            } while (!Message.validateMessageLength(text).equals("Message ready to send."));

            Message message = new Message(manager.count(), recipient, text);
            System.out.println("1. Send\n2. Disregard\n3. Store");
            int action = readInt(scanner, "Choose an action: ");
            if (action < 1 || action > 3) { System.out.println("Invalid action; message not added."); continue; }
            System.out.println(message.SentMessage(action));
            manager.add(message);
            if (action == 1) System.out.println(message.printMessages());
        }
    }

    private static void partThreeMenu(Scanner scanner, MessageManager manager) {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- PART 3 MESSAGE TOOLS ---");
            System.out.println("1. Display sender and recipient of stored messages");
            System.out.println("2. Display longest message");
            System.out.println("3. Search by message ID");
            System.out.println("4. Search messages by recipient");
            System.out.println("5. Delete message using hash");
            System.out.println("6. Display full message report");
            System.out.println("7. Display stored messages");
            System.out.println("8. Save stored messages now");
            System.out.println("9. Back to main menu");
            int choice = readInt(scanner, "Choose an option: ");
            switch (choice) {
                case 1: System.out.println(manager.displaySenderAndRecipient()); break;
                case 2: System.out.println(manager.longestMessageDetails()); break;
                case 3:
                    System.out.print("Enter message ID: ");
                    System.out.println(manager.searchByMessageID(scanner.nextLine())); break;
                case 4:
                    System.out.print("Enter recipient cellphone: ");
                    List<String> matches = manager.searchByRecipient(scanner.nextLine());
                    if (matches.isEmpty()) System.out.println("No messages found for that recipient.");
                    else for (String item : matches) System.out.println(item);
                    break;
                case 5:
                    System.out.print("Enter message hash: ");
                    System.out.println(manager.deleteByHash(scanner.nextLine())); break;
                case 6: System.out.println(manager.report()); break;
                case 7: System.out.println(manager.displayStored()); break;
                case 8:
                    try { manager.saveStored(STORED_FILE); System.out.println("Stored messages saved."); }
                    catch (IOException ex) { System.out.println("Save failed: " + ex.getMessage()); }
                    break;
                case 9: back = true; break;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    private static int readInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine();
            try { return Integer.parseInt(input.trim()); }
            catch (NumberFormatException ex) { System.out.println("Please enter a valid whole number."); }
        }
    }
}
