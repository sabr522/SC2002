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

        System.out.println("1. Access Role");
        System.out.println("2. Change Passsword");
        System.out.print("Please enter which action you would like to perform: ");
        



    }
    
}
