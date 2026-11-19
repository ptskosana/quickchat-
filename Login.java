package quickchat;

/** Handles QuickChat registration and login. */
public class Login {
    private String firstName = "";
    private String lastName = "";
    private String username = "";
    private String password = "";
    private String cellNumber = "";

    public void setFirstName(String firstName) {
        this.firstName = firstName == null ? "" : firstName.trim();
    }

    public void setLastName(String lastName) {
        this.lastName = lastName == null ? "" : lastName.trim();
    }

    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }

    public boolean checkUserName(String username) {
        return username != null && username.length() <= 5 && username.contains("_");
    }

    public boolean checkPasswordComplexity(String password) {
        if (password == null || password.length() < 8) return false;
        boolean upper = false, lower = false, digit = false, special = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) upper = true;
            else if (Character.isLowerCase(c)) lower = true;
            else if (Character.isDigit(c)) digit = true;
            else special = true;
        }
        return upper && lower && digit && special;
    }

    public boolean checkCellPhoneNumber(String cellNumber) {
        return cellNumber != null && cellNumber.matches("\\+27[1-9]\\d{8}");
    }

    public String registerUser(String username, String password, String cellNumber) {
        if (!checkUserName(username)) {
            return "Username is not correctly formatted, please ensure that your username contains an underscore and is no more than five characters in length.";
        }
        if (!checkPasswordComplexity(password)) {
            return "Password is not correctly formatted; please ensure that the password contains at least eight characters, a capital letter, a number and a special character.";
        }
        if (!checkCellPhoneNumber(cellNumber)) {
            return "Cell phone number incorrectly formatted or does not contain international code.";
        }
        this.username = username;
        this.password = password;
        this.cellNumber = cellNumber;
        return "Username and password successfully captured.";
    }

    public boolean loginUser(String username, String password) {
        return this.username.equals(username) && this.password.equals(password);
    }

    public String returnLoginStatus(boolean success) {
        if (success) return "Welcome " + firstName + " " + lastName + ", it is great to see you again.";
        return "Username or password incorrect, please try again.";
    }
}
