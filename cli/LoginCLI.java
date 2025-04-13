package cli;

import java.util.ArrayList;
import java.util.Scanner;
import Login.Login;

public class LoginCLI {
    public static void main(String[] args) {

    Scanner scan = new Scanner(System.in);  
        System.out.println("1. Applicant");
        System.out.println("2. Officer");
        System.out.println("3. Manager");

        System.out.print("Choose a role:");
        
        int role = scan.nextInt();
        scan.nextLine();
        ArrayList<String[]> data = new ArrayList<>();
        data=Login.chooseRole(role);

        String[] matchedUser = null; 
        boolean validNRIC = false;
        
        System.out.print("Please enter NRIC: ");
        do {
            String NRIC = scan.nextLine();
            matchedUser = Login.checkNRIC(NRIC, data); 
            validNRIC = (matchedUser != null); 
        } while (!validNRIC);
        
        System.out.print("Please enter password: ");
        boolean validPassword;
        do {
            String password = scan.nextLine();
            validPassword = Login.checkPassword(password, matchedUser); 
        } while (!validPassword);

        System.out.println("");
        System.out.println("1. Access Role");
        System.out.println("2. Change Passsword");
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
                            // ApplicantCLI.main(new String[]{});
                            break;
                        case 2:
                            OfficerCLI.main(new String[]{});
                            break;
                        case 3:
                            // ManagerCLI.main(new String[]{});
                            break;
                    }
                    break;
        
                case 2:
                    String newPass;
                    String confirmPass;
                    boolean samePassword;
                    do {
                        System.out.println("Please enter new password:");
                        newPass = scan.nextLine();
                        System.out.println("Confirm new password:");
                        confirmPass = scan.nextLine();
                        String filePath = "";
                        switch (role) {
                            case 1:
                                filePath = "ApplicantList.csv";
                                break;
                            case 2:
                                filePath = "OfficerList.csv";
                                break;
                            case 3:
                                filePath = "ManagerList.csv";
                                break;
                        }
        
                        samePassword = Login.changePassword(matchedUser, newPass, confirmPass, data, filePath);        
                    } while (!samePassword);
                    break; 
        
            }
        
        } while (choice != 1 && choice != 2);
}

}
