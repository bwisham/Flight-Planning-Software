import java.io.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class Userclass {
    private final UserDatabase userDbase;
    private static final int MAX_NAME_LENGTH = 15;
    private static final int MAX_ADDRESS_LENGTH = 50;
    private static final int MAX_EMAIL_LENGTH = 50;
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MAX_PASSWORD_LENGTH = 20;
    private static final String DB_FILE = "userdb.dat";
    
    public Userclass() {
        System.out.println("Initializing database...");
        this.userDbase = loadDatabase();
        System.out.println("Database ready. Contains " + userDbase.getAllUsers().size() + " users");
    }
    
    public UserDatabase loadDatabase() {
        File dbFile = new File(DB_FILE);
        System.out.println("Database file location: " + dbFile.getAbsolutePath());
        
        if (!dbFile.exists()) {
            System.out.println("No database found. Creating new database file...");
            UserDatabase newDb = new UserDatabase();
            saveDatabase(newDb);
            return newDb;
        }
        
        if (dbFile.length() == 0) {
            System.out.println("Empty database file detected. Creating new database...");
            UserDatabase newDb = new UserDatabase();
            saveDatabase(newDb);
            return newDb;
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(DB_FILE))) {
            UserDatabase loadedDb = (UserDatabase) ois.readObject();
            System.out.println("Database loaded successfully");
            return loadedDb;
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading database: " + e.getMessage());
            System.out.println("Creating new database as recovery...");
            UserDatabase newDb = new UserDatabase();
            saveDatabase(newDb);
            return newDb;
        }
    }
    
    private void saveDatabase(UserDatabase db) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DB_FILE))) {
            oos.writeObject(db);
            System.out.println("Database saved successfully");
        } catch (IOException e) {
            System.out.println("CRITICAL ERROR: Failed to save database!");
            System.out.println("Error details: " + e.getMessage());
        }
    }
    
    private void saveDatabase() {
        saveDatabase(this.userDbase);
    }
    
    public void add() {
        String fname;
        do {
            fname = JOptionPane.showInputDialog("Enter first name (max " + MAX_NAME_LENGTH + " chars):");
            if (fname == null) return;
            if (fname.length() > MAX_NAME_LENGTH) {
                JOptionPane.showMessageDialog(null, "First name too long! Max " + MAX_NAME_LENGTH + " characters allowed.");
            }
        } while (fname.length() > MAX_NAME_LENGTH);

        String lname;
        do {
            lname = JOptionPane.showInputDialog("Enter last name (max " + MAX_NAME_LENGTH + " chars):");
            if (lname == null) return;
            if (lname.length() > MAX_NAME_LENGTH) {
                JOptionPane.showMessageDialog(null, "Last name too long! Max " + MAX_NAME_LENGTH + " characters allowed.");
            }
        } while (lname.length() > MAX_NAME_LENGTH);

        String pnumber;
        do {
            pnumber = JOptionPane.showInputDialog("Enter phone number (10 digits):");
            if (pnumber == null) return;
            if (!pnumber.matches("\\d{10}")) {
                JOptionPane.showMessageDialog(null, "Invalid phone number! Must be exactly 10 digits.");
            } else if (userDbase.phoneNumberExists(pnumber)) {
                JOptionPane.showMessageDialog(null, "This phone number already exists in the system!");
                pnumber = "";
            }
        } while (!pnumber.matches("\\d{10}"));

        String haddress;
        do {
            haddress = JOptionPane.showInputDialog("Enter home address (max " + MAX_ADDRESS_LENGTH + " chars):");
            if (haddress == null) return;
            if (haddress.length() > MAX_ADDRESS_LENGTH) {
                JOptionPane.showMessageDialog(null, "Address too long! Max " + MAX_ADDRESS_LENGTH + " characters allowed.");
            }
        } while (haddress.length() > MAX_ADDRESS_LENGTH);

        String eaddress;
        do {
            eaddress = JOptionPane.showInputDialog("Enter email address (max " + MAX_EMAIL_LENGTH + " chars):");
            if (eaddress == null) return;
            if (eaddress.length() > MAX_EMAIL_LENGTH) {
                JOptionPane.showMessageDialog(null, "Email too long! Max " + MAX_EMAIL_LENGTH + " characters allowed.");
            } else if (userDbase.emailExists(eaddress)) {
                JOptionPane.showMessageDialog(null, "This email address already exists in the system!");
                eaddress = "";
            }
        } while (eaddress.length() > MAX_EMAIL_LENGTH || eaddress.isEmpty());

        String password;
        do {
            password = JOptionPane.showInputDialog("Enter password (" + MIN_PASSWORD_LENGTH + "-" + MAX_PASSWORD_LENGTH + " chars):");
            if (password == null) return;
            if (password.length() < MIN_PASSWORD_LENGTH || password.length() > MAX_PASSWORD_LENGTH) {
                JOptionPane.showMessageDialog(null, "Password must be between " + MIN_PASSWORD_LENGTH + 
                    " and " + MAX_PASSWORD_LENGTH + " characters!");
            }
        } while (password.length() < MIN_PASSWORD_LENGTH || password.length() > MAX_PASSWORD_LENGTH);

        if (userDbase.userExists(fname, lname, pnumber, haddress, eaddress)) {
            JOptionPane.showMessageDialog(null, "Error: A user with these exact details already exists in the system!");
            return;
        }

        btnSubmit(fname, lname, pnumber, haddress, eaddress, password);
    }

    public void btnSubmit(String fname, String lname, String pnumber, String haddress, String eaddress, String password) {
        int confirmation = JOptionPane.showConfirmDialog(null, 
            "Confirm submission?\nFirst Name: " + fname + "\nLast Name: " + lname + 
            "\nPhone: " + pnumber + "\nAddress: " + haddress + "\nEmail: " + eaddress,
            "Confirm", JOptionPane.YES_NO_OPTION);
        
        if (confirmation == JOptionPane.YES_OPTION) {
            User user = new User(fname, lname, pnumber, haddress, eaddress, password);
            userDbase.addUser(user);
            saveDatabase();
            JOptionPane.showMessageDialog(null, "Registration successful!");
        } else {
            JOptionPane.showMessageDialog(null, "Registration cancelled.");
        }
    }

    public void modify() {
        if (userDbase.getAllUsers().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Error: No users in database!");
            return;
        }
        
        String email = JOptionPane.showInputDialog("Enter your email address:");
        if (email == null || email.isEmpty()) return;
        
        User user = userDbase.getUserByEmail(email);
        if (user == null) {
            JOptionPane.showMessageDialog(null, "User not found!");
            return;
        }
        
        String password = JOptionPane.showInputDialog("Enter your password:");
        if (password == null || !user.verifyPassword(password)) {
            JOptionPane.showMessageDialog(null, "Incorrect password!");
            return;
        }
        
        StringBuilder currentValues = new StringBuilder("Current values:\n");
        currentValues.append(user.toString());
        JOptionPane.showMessageDialog(null, currentValues.toString());
        
        JOptionPane.showMessageDialog(null, "Enter new values (leave blank to keep current):");
        
        String fname;
        do {
            fname = JOptionPane.showInputDialog("First name (" + user.getFirstName() + "):");
            if (fname == null) return;
            if (!fname.isEmpty() && fname.length() > MAX_NAME_LENGTH) {
                JOptionPane.showMessageDialog(null, "First name too long! Max " + MAX_NAME_LENGTH + " characters allowed.");
            }
        } while (!fname.isEmpty() && fname.length() > MAX_NAME_LENGTH);
        
        String lname;
        do {
            lname = JOptionPane.showInputDialog("Last name (" + user.getLastName() + "):");
            if (lname == null) return;
            if (!lname.isEmpty() && lname.length() > MAX_NAME_LENGTH) {
                JOptionPane.showMessageDialog(null, "Last name too long! Max " + MAX_NAME_LENGTH + " characters allowed.");
            }
        } while (!lname.isEmpty() && lname.length() > MAX_NAME_LENGTH);
        
        String pnumber;
        do {
            pnumber = JOptionPane.showInputDialog("Phone (" + user.getPhoneNumber() + "):");
            if (pnumber == null) return;
            if (!pnumber.isEmpty()) {
                if (!pnumber.matches("\\d{10}")) {
                    JOptionPane.showMessageDialog(null, "Invalid phone number! Must be exactly 10 digits.");
                } else if (userDbase.phoneNumberExists(pnumber) && !pnumber.equals(user.getPhoneNumber())) {
                    JOptionPane.showMessageDialog(null, "This phone number already exists in the system!");
                    pnumber = "";
                }
            }
        } while (!pnumber.isEmpty() && !pnumber.matches("\\d{10}"));
        
        String haddress;
        do {
            haddress = JOptionPane.showInputDialog("Address (" + user.getHomeAddress() + "):");
            if (haddress == null) return;
            if (!haddress.isEmpty() && haddress.length() > MAX_ADDRESS_LENGTH) {
                JOptionPane.showMessageDialog(null, "Address too long! Max " + MAX_ADDRESS_LENGTH + " characters allowed.");
            }
        } while (!haddress.isEmpty() && haddress.length() > MAX_ADDRESS_LENGTH);
        
        String eaddress;
        do {
            eaddress = JOptionPane.showInputDialog("Email (" + user.getEmailAddress() + "):");
            if (eaddress == null) return;
            if (!eaddress.isEmpty()) {
                if (eaddress.length() > MAX_EMAIL_LENGTH) {
                    JOptionPane.showMessageDialog(null, "Email too long! Max " + MAX_EMAIL_LENGTH + " characters allowed.");
                } else if (userDbase.emailExists(eaddress) && !eaddress.equalsIgnoreCase(user.getEmailAddress())) {
                    JOptionPane.showMessageDialog(null, "This email address already exists in the system!");
                    eaddress = "";
                }
            }
        } while (!eaddress.isEmpty() && (eaddress.length() > MAX_EMAIL_LENGTH || 
               (userDbase.emailExists(eaddress) && !eaddress.equalsIgnoreCase(user.getEmailAddress()))));
        
        User updatedUser = new User(
            fname.isEmpty() ? user.getFirstName() : fname,
            lname.isEmpty() ? user.getLastName() : lname,
            pnumber.isEmpty() ? user.getPhoneNumber() : pnumber,
            haddress.isEmpty() ? user.getHomeAddress() : haddress,
            eaddress.isEmpty() ? user.getEmailAddress() : eaddress,
            user.getPassword()
        );
        
        userDbase.updateUser(user, updatedUser);
        saveDatabase();
        JOptionPane.showMessageDialog(null, "Profile updated successfully!");
    }

    public void delete() {
        if (userDbase.getAllUsers().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Error: No users in database!");
            return;
        }
        
        String email = JOptionPane.showInputDialog("Enter your email address:");
        if (email == null || email.isEmpty()) return;
        
        User user = userDbase.getUserByEmail(email);
        if (user == null) {
            JOptionPane.showMessageDialog(null, "User not found!");
            return;
        }
        
        String password = JOptionPane.showInputDialog("Enter your password:");
        if (password == null || !user.verifyPassword(password)) {
            JOptionPane.showMessageDialog(null, "Incorrect password!");
            return;
        }
        
        int confirmation = JOptionPane.showConfirmDialog(null, 
            "About to delete your account permanently!\nConfirm deletion?", 
            "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        
        if (confirmation == JOptionPane.YES_OPTION) {
            userDbase.deleteUser(user);
            saveDatabase();
            JOptionPane.showMessageDialog(null, "Account deleted successfully!");
        } else {
            JOptionPane.showMessageDialog(null, "Deletion cancelled.");
        }
    }

    private void listAllUsers() {
        StringBuilder sb = new StringBuilder("=== ALL USERS IN SYSTEM ===\n");
        List<User> allUsers = userDbase.getAllUsers();
        
        if (allUsers.isEmpty()) {
            sb.append("No users in the system.");
        } else {
            sb.append(String.format("%-15s %-15s %-12s %-20s %-20s%n", 
                                 "First Name", "Last Name", "Phone", "Email", "Address"));
            sb.append("----------------------------------------------------------------------------\n");
            
            for (User user : allUsers) {
                sb.append(String.format("%-15s %-15s %-12s %-20s %-20s%n",
                                user.getFirstName(),
                                user.getLastName(),
                                user.getPhoneNumber(),
                                user.getEmailAddress(),
                                user.getHomeAddress()));
            }
        }
        
        JOptionPane.showMessageDialog(null, new JTextArea(sb.toString()), "All Users", JOptionPane.INFORMATION_MESSAGE);
    }

    public void showMenu() {
        while (true) {
            String input = JOptionPane.showInputDialog(null, 
                "User Management System\n1. Add User\n2. Modify User\n3. Delete User\n4. Exit\n\nEnter your choice (or secret code for admin options):");
            
            if (input == null || input.equals("4")) {
                JOptionPane.showMessageDialog(null, "Exiting...");
                return;
            }
            
            if (input.equals("9860")) {
                listAllUsers();
                continue;
            }
            
            try {
                int choice = Integer.parseInt(input);
                
                switch (choice) {
                    case 1 -> add();
                    case 2 -> modify();
                    case 3 -> delete();
                    case 4 -> {
                        JOptionPane.showMessageDialog(null, "Exiting...");
                        return;
                    }
                    default -> JOptionPane.showMessageDialog(null, "Invalid choice!");
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Please enter a valid number or code!");
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("Starting User Management System...");
        Userclass userManager = new Userclass();
        userManager.showMenu();
    }
}

class UserDatabase implements Serializable {
    private static final long serialVersionUID = 1L;
    private final List<User> users;
    
    public UserDatabase() {
        this.users = new ArrayList<>();
    }
    
    public boolean userExists(String fname, String lname, String pnumber, String haddress, String eaddress) {
        return users.stream().anyMatch(user -> 
            user.getFirstName().equalsIgnoreCase(fname) &&
            user.getLastName().equalsIgnoreCase(lname) &&
            user.getPhoneNumber().equals(pnumber) &&
            user.getHomeAddress().equalsIgnoreCase(haddress) &&
            user.getEmailAddress().equalsIgnoreCase(eaddress)
        );
    }
    
    public boolean phoneNumberExists(String phoneNumber) {
        return users.stream().anyMatch(user -> user.getPhoneNumber().equals(phoneNumber));
    }
    
    public boolean emailExists(String email) {
        return users.stream().anyMatch(user -> user.getEmailAddress().equalsIgnoreCase(email));
    }
    
    public void addUser(User user) {
        users.add(user);
    }
    
    public User getUserByEmail(String email) {
        return users.stream()
            .filter(user -> user.getEmailAddress().equalsIgnoreCase(email))
            .findFirst()
            .orElse(null);
    }
    
    public void updateUser(User oldUser, User updatedUser) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getEmailAddress().equalsIgnoreCase(oldUser.getEmailAddress())) {
                users.set(i, updatedUser);
                return;
            }
        }
    }
    
    public void deleteUser(User user) {
        users.removeIf(u -> u.getEmailAddress().equalsIgnoreCase(user.getEmailAddress()));
    }
    
    public List<User> getAllUsers() {
        return new ArrayList<>(users);
    }
}

class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String firstName;
    private final String lastName;
    private final String phoneNumber;
    private final String homeAddress;
    private final String emailAddress;
    private final String password;
    
    public User(String fname, String lname, String pnumber, String haddress, String eaddress, String password) {
        this.firstName = fname;
        this.lastName = lname;
        this.phoneNumber = pnumber;
        this.homeAddress = haddress;
        this.emailAddress = eaddress;
        this.password = password;
    }
    
    public boolean verifyPassword(String inputPassword) {
        return this.password.equals(inputPassword);
    }
    
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getHomeAddress() { return homeAddress; }
    public String getEmailAddress() { return emailAddress; }
    public String getPassword() { return password; }
    
    @Override
    public String toString() {
        return "First Name: " + firstName + "\n" +
               "Last Name: " + lastName + "\n" +
               "Phone: " + phoneNumber + "\n" +
               "Address: " + homeAddress + "\n" +
               "Email: " + emailAddress;
    }
}