import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Userclass {
    private final UserDatabase userDbase;
    private final Scanner scanner;
    private final Random random;
    private static final int MAX_NAME_LENGTH = 15;
    private static final int MAX_ADDRESS_LENGTH = 50;
    private static final int MAX_EMAIL_LENGTH = 50;
    private static final String DB_FILE = "userdb.dat";
    
    public Userclass() {
        this.scanner = new Scanner(System.in);
        this.random = new Random();
        System.out.println("Initializing database...");
        this.userDbase = loadDatabase();
        System.out.println("Database ready. Contains " + userDbase.getAllUsers().size() + " users");
    }
    
    private UserDatabase loadDatabase() {
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
    
    private void pressEnterToContinue() {
        System.out.println("\nPress Enter to return to the main menu...");
        scanner.nextLine();
    }
    
    public void add() {
        String fname;
        do {
            System.out.print("Enter first name (max " + MAX_NAME_LENGTH + " chars): ");
            fname = scanner.nextLine();
            if (fname.length() > MAX_NAME_LENGTH) {
                System.out.println("First name too long! Max " + MAX_NAME_LENGTH + " characters allowed.");
            }
        } while (fname.length() > MAX_NAME_LENGTH);

        String lname;
        do {
            System.out.print("Enter last name (max " + MAX_NAME_LENGTH + " chars): ");
            lname = scanner.nextLine();
            if (lname.length() > MAX_NAME_LENGTH) {
                System.out.println("Last name too long! Max " + MAX_NAME_LENGTH + " characters allowed.");
            }
        } while (lname.length() > MAX_NAME_LENGTH);

        String pnumber;
        do {
            System.out.print("Enter phone number (10 digits): ");
            pnumber = scanner.nextLine();
            if (!pnumber.matches("\\d{10}")) {
                System.out.println("Invalid phone number! Must be exactly 10 digits.");
            }
        } while (!pnumber.matches("\\d{10}"));

        String haddress;
        do {
            System.out.print("Enter home address (max " + MAX_ADDRESS_LENGTH + " chars): ");
            haddress = scanner.nextLine();
            if (haddress.length() > MAX_ADDRESS_LENGTH) {
                System.out.println("Address too long! Max " + MAX_ADDRESS_LENGTH + " characters allowed.");
            }
        } while (haddress.length() > MAX_ADDRESS_LENGTH);

        String eaddress;
        do {
            System.out.print("Enter email address (max " + MAX_EMAIL_LENGTH + " chars): ");
            eaddress = scanner.nextLine();
            if (eaddress.length() > MAX_EMAIL_LENGTH) {
                System.out.println("Email too long! Max " + MAX_EMAIL_LENGTH + " characters allowed.");
            }
        } while (eaddress.length() > MAX_EMAIL_LENGTH);

        btnSubmit(fname, lname, pnumber, haddress, eaddress);
    }

    private int generateUserId() {
        return 1000 + random.nextInt(9000);
    }

    private void btnSubmit(String fname, String lname, String pnumber, String haddress, String eaddress) {
        System.out.println("Confirm submission? (Y/N)");
        String confirmation = scanner.nextLine();
        
        if (confirmation.equalsIgnoreCase("Y")) {
            int userId;
            do {
                userId = generateUserId();
            } while (userDbase.userExists(userId));
            
            User user = new User(userId, fname, lname, pnumber, haddress, eaddress);
            userDbase.addUser(user);
            saveDatabase();
            System.out.println("Submission successful! Your user ID is: " + userId);
        } else {
            System.out.println("Submission cancelled.");
        }
        pressEnterToContinue();
    }

    public void modify() {
        if (userDbase.getAllUsers().isEmpty()) {
            System.out.println("Error: No users in database!");
            pressEnterToContinue();
            return;
        }
        
        System.out.println("Enter user ID to modify:");
        int id = scanner.nextInt();
        scanner.nextLine();
        
        User user = userDbase.getUser(id);
        if (user != null) {
            System.out.println("Current values:");
            System.out.println(user);
            
            System.out.println("Enter new values (leave blank to keep current):");
            
            String fname;
            do {
                System.out.print("First name (" + user.getFirstName() + "): ");
                fname = scanner.nextLine();
                if (!fname.isEmpty() && fname.length() > MAX_NAME_LENGTH) {
                    System.out.println("First name too long! Max " + MAX_NAME_LENGTH + " characters allowed.");
                }
            } while (!fname.isEmpty() && fname.length() > MAX_NAME_LENGTH);
            
            String lname;
            do {
                System.out.print("Last name (" + user.getLastName() + "): ");
                lname = scanner.nextLine();
                if (!lname.isEmpty() && lname.length() > MAX_NAME_LENGTH) {
                    System.out.println("Last name too long! Max " + MAX_NAME_LENGTH + " characters allowed.");
                }
            } while (!lname.isEmpty() && lname.length() > MAX_NAME_LENGTH);
            
            String pnumber;
            do {
                System.out.print("Phone (" + user.getPhoneNumber() + "): ");
                pnumber = scanner.nextLine();
                if (!pnumber.isEmpty() && !pnumber.matches("\\d{10}")) {
                    System.out.println("Invalid phone number! Must be exactly 10 digits.");
                }
            } while (!pnumber.isEmpty() && !pnumber.matches("\\d{10}"));
            
            String haddress;
            do {
                System.out.print("Address (" + user.getHomeAddress() + "): ");
                haddress = scanner.nextLine();
                if (!haddress.isEmpty() && haddress.length() > MAX_ADDRESS_LENGTH) {
                    System.out.println("Address too long! Max " + MAX_ADDRESS_LENGTH + " characters allowed.");
                }
            } while (!haddress.isEmpty() && haddress.length() > MAX_ADDRESS_LENGTH);
            
            String eaddress;
            do {
                System.out.print("Email (" + user.getEmailAddress() + "): ");
                eaddress = scanner.nextLine();
                if (!eaddress.isEmpty() && eaddress.length() > MAX_EMAIL_LENGTH) {
                    System.out.println("Email too long! Max " + MAX_EMAIL_LENGTH + " characters allowed.");
                }
            } while (!eaddress.isEmpty() && eaddress.length() > MAX_EMAIL_LENGTH);
            
            User updatedUser = new User(
                user.getId(),
                fname.isEmpty() ? user.getFirstName() : fname,
                lname.isEmpty() ? user.getLastName() : lname,
                pnumber.isEmpty() ? user.getPhoneNumber() : pnumber,
                haddress.isEmpty() ? user.getHomeAddress() : haddress,
                eaddress.isEmpty() ? user.getEmailAddress() : eaddress
            );
            
            userDbase.updateUser(id, updatedUser);
            saveDatabase();
            System.out.println("User updated successfully!");
        } else {
            System.out.println("User not found!");
        }
        pressEnterToContinue();
    }

    public void delete() {
        if (userDbase.getAllUsers().isEmpty()) {
            System.out.println("Error: No users in database!");
            pressEnterToContinue();
            return;
        }
        
        System.out.println("Enter user ID to delete:");
        int id = scanner.nextInt();
        scanner.nextLine();
        
        User user = userDbase.getUser(id);
        if (user != null) {
            System.out.println("About to delete:");
            System.out.println(user);
            
            System.out.println("Confirm deletion? (Y/N)");
            String confirmation = scanner.nextLine();
            
            if (confirmation.equalsIgnoreCase("Y")) {
                userDbase.deleteUser(id);
                saveDatabase();
                System.out.println("User deleted successfully!");
            } else {
                System.out.println("Deletion cancelled.");
            }
        } else {
            System.out.println("User not found!");
        }
        pressEnterToContinue();
    }

    private void listAllUsers() {
        System.out.println("\n=== ALL USERS IN SYSTEM ===");
        List<User> allUsers = userDbase.getAllUsers();
        
        if (allUsers.isEmpty()) {
            System.out.println("No users in the system.");
            return;
        }
        
        System.out.printf("%-6s %-15s %-15s %-12s %-20s %-20s%n", 
                         "ID", "First Name", "Last Name", "Phone", "Email", "Address");
        System.out.println("----------------------------------------------------------------------------");
        
        for (User user : allUsers) {
            System.out.printf("%-6d %-15s %-15s %-12s %-20s %-20s%n",
                            user.getId(),
                            user.getFirstName(),
                            user.getLastName(),
                            user.getPhoneNumber(),
                            user.getEmailAddress(),
                            user.getHomeAddress());
        }
    }

    public void showMenu() {
        while (true) {
            System.out.println("\nUser Management System");
            System.out.println("1. Add User");
            System.out.println("2. Modify User");
            System.out.println("3. Delete User");
            System.out.println("4. Exit");
            System.out.print("Enter your choice (or secret code for admin options): ");
            
            String input = scanner.nextLine();
            
            // Check for secret code first
            if (input.equals("9860")) {
                listAllUsers();
                pressEnterToContinue();
                continue;
            }
            
            // Regular menu processing
            try {
                int choice = Integer.parseInt(input);
                
                switch (choice) {
                    case 1 -> add();
                    case 2 -> modify();
                    case 3 -> delete();
                    case 4 -> {
                        System.out.println("Exiting...");
                        return;
                    }
                    default -> {
                        System.out.println("Invalid choice!");
                        pressEnterToContinue();
                    }
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number or code!");
                pressEnterToContinue();
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
    
    public boolean userExists(int id) {
        return users.stream().anyMatch(user -> user.getId() == id);
    }
    
    public void addUser(User user) {
        users.add(user);
    }
    
    public User getUser(int id) {
        for (User user : users) {
            if (user.getId() == id) {
                return user;
            }
        }
        return null;
    }
    
    public void updateUser(int id, User updatedUser) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId() == id) {
                users.set(i, updatedUser);
                return;
            }
        }
    }
    
    public void deleteUser(int id) {
        users.removeIf(user -> user.getId() == id);
    }
    
    public List<User> getAllUsers() {
        return new ArrayList<>(users);
    }
}

class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int id;
    private final String firstName;
    private final String lastName;
    private final String phoneNumber;
    private final String homeAddress;
    private final String emailAddress;
    
    public User(int id, String fname, String lname, String pnumber, String haddress, String eaddress) {
        this.id = id;
        this.firstName = fname;
        this.lastName = lname;
        this.phoneNumber = pnumber;
        this.homeAddress = haddress;
        this.emailAddress = eaddress;
    }
    
    public int getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getHomeAddress() { return homeAddress; }
    public String getEmailAddress() { return emailAddress; }
    
    @Override
    public String toString() {
        return "ID: " + id + "\n" +
               "First Name: " + firstName + "\n" +
               "Last Name: " + lastName + "\n" +
               "Phone: " + phoneNumber + "\n" +
               "Address: " + homeAddress + "\n" +
               "Email: " + emailAddress;
    }
}