import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Userclass {
    private final UserDatabase userDbase;
    private final Scanner scanner;
    private final Random random;
    
    public Userclass() {
        this.userDbase = new UserDatabase();
        this.scanner = new Scanner(System.in);
        this.random = new Random();
    }
    
    public void add() {
        System.out.print("Enter first name of the user: ");
        String fname = scanner.nextLine();

        System.out.print("Enter last name of the user: ");
        String lname = scanner.nextLine();

        String pnumber;
        do {
            System.out.print("Enter phone number (10 digits): ");
            pnumber = scanner.nextLine();
            if (!pnumber.matches("\\d{10}")) {
                System.out.println("Invalid phone number! Must be exactly 10 digits.");
            }
        } while (!pnumber.matches("\\d{10}"));

        System.out.print("Enter home address of the user: ");
        String haddress = scanner.nextLine();

        System.out.print("Enter email address of the user: ");
        String eaddress = scanner.nextLine();

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
            System.out.println("Submission successful! Your user ID is: " + userId);
        } else {
            System.out.println("Submission cancelled.");
        }
    }

    public void modify() {
        System.out.println("Enter user ID to modify:");
        int id = scanner.nextInt();
        scanner.nextLine();
        
        User user = userDbase.getUser(id);
        if (user != null) {
            System.out.println("Current values:");
            System.out.println(user);
            
            System.out.println("Enter new values (leave blank to keep current):");
            
            System.out.print("First name (" + user.getFirstName() + "): ");
            String fname = scanner.nextLine();
            
            System.out.print("Last name (" + user.getLastName() + "): ");
            String lname = scanner.nextLine();
            
            String pnumber;
            do {
                System.out.print("Phone (" + user.getPhoneNumber() + "): ");
                pnumber = scanner.nextLine();
                if (!pnumber.isEmpty() && !pnumber.matches("\\d{10}")) {
                    System.out.println("Invalid phone number! Must be exactly 10 digits.");
                }
            } while (!pnumber.isEmpty() && !pnumber.matches("\\d{10}"));
            
            System.out.print("Address (" + user.getHomeAddress() + "): ");
            String haddress = scanner.nextLine();
            
            System.out.print("Email (" + user.getEmailAddress() + "): ");
            String eaddress = scanner.nextLine();
            
            User updatedUser = new User(
                user.getId(),
                fname.isEmpty() ? user.getFirstName() : fname,
                lname.isEmpty() ? user.getLastName() : lname,
                pnumber.isEmpty() ? user.getPhoneNumber() : pnumber,
                haddress.isEmpty() ? user.getHomeAddress() : haddress,
                eaddress.isEmpty() ? user.getEmailAddress() : eaddress
            );
            
            userDbase.updateUser(id, updatedUser);
            System.out.println("User updated successfully!");
        } else {
            System.out.println("User not found!");
        }
    }

    public void delete() {
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
                System.out.println("User deleted successfully!");
            } else {
                System.out.println("Deletion cancelled.");
            }
        } else {
            System.out.println("User not found!");
        }
    }

    public void showMenu() {
        while (true) {
            System.out.println("\nUser Management System");
            System.out.println("1. Add User");
            System.out.println("2. Modify User");
            System.out.println("3. Delete User");
            System.out.println("4. Exit");
            System.out.print("Enter your choice: ");
            
            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number!");
                continue;
            }
            
            switch (choice) {
                case 1 -> add();
                case 2 -> modify();
                case 3 -> delete();
                case 4 -> {
                    System.out.println("Exiting...");
                    return;
                }
                default -> System.out.println("Invalid choice!");
            }
        }
    }

    public static void main(String[] args) {
        Userclass userManager = new Userclass();
        userManager.showMenu();
    }
}

class UserDatabase {
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
}

class User {
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