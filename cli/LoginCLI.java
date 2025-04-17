package cli;

import Login.Login;
import data.DataManager;
import Actors.User;
import cli.*;

import java.io.IOException;
import java.util.Map;
import java.util.Scanner;

public class LoginCLI {
    
            
    public static void main(String[] args) {
        DataManager dm = new DataManager();
        Scanner scan = new Scanner(System.in);
        Map<String, User> users = null;

        try {
            users = dm.loadUsers();
        } catch (IOException e) {
            System.err.println("Failed to load user data.");
            e.printStackTrace();
            return;
        }

        System.out.println("1. Applicant");
        System.out.println("2. Officer");
        System.out.println("3. Manager");
        System.out.print("Choose a role: ");

        int role = scan.nextInt();
        scan.nextLine();

        String nric;
        User matchedUser = null;

        // NRIC Check
        System.out.print("Please enter NRIC: ");
        do {
            nric = scan.nextLine();
            matchedUser = Login.checkNRIC(nric, users);
        } while (matchedUser == null);

        // Password Check
        System.out.print("Please enter password: ");
        boolean validPassword;
        do {
            String password = scan.nextLine();
            validPassword = Login.checkPassword(password, matchedUser);
        } while (!validPassword);

        // Action menu
        System.out.println("\n1. Access Role");
        System.out.println("2. Change Password");
        System.out.print("Please enter which action you would like to perform: ");

        int choice;
        do {
            choice = scan.nextInt();
            scan.nextLine();

            if (choice != 1 && choice != 2) {
                System.out.print("Please enter valid choice: ");
                continue;
            }

            switch (choice) {
                case 1:
                    System.out.println("Accessing Role...");
                    switch (role) {
                        case 1:
                            ApplicantCLI.main(new String[]{});
                            break;
                        case 2:
                            OfficerCLI.main(new String[]{});
                            break;
                        case 3:
                            ManagerCLI.main(new String[]{});
                            break;
                        default:
                            System.out.println("Invalid role.");
                    }
                    break;

                case 2:
                    String newPass, confirmPass;
                    boolean success;
                    do {
                        System.out.print("Please enter new password: ");
                        newPass = scan.nextLine();
                        System.out.print("Confirm new password: ");
                        confirmPass = scan.nextLine();

                        success = Login.changePassword(matchedUser, newPass, confirmPass, users, "/users.csv");
                        if (success) {
                            try {
                                dm.saveUsers(users);
                            } catch (IOException e) {
                                System.err.println("Failed to save updated user data.");
                                e.printStackTrace();
                            }
                        }
                    } while (!success);
                    break;
            }

        } while (choice != 1 && choice != 2);
    }
}
