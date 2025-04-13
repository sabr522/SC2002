package Login;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Login{
    public static ArrayList<String[]> chooseRole(int role){
         ArrayList<String[]> userData = null;

        switch (role) {
            case 1:
                userData = readCSV("ApplicantList.csv");
                break;
            case 2:
                userData = readCSV("OfficerList.csv");
                break;
            case 3:
                userData = readCSV("ManagerList.csv");
                break;

        }
        return userData;
    }


    private static ArrayList<String[]> readCSV(String filePath) {
        ArrayList<String[]> data = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;

            while ((line = br.readLine()) != null) {
                data.add(line.split(","));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return data;
    }

    public static String[] checkNRIC(String NRIC, ArrayList<String[]> userData) { 
        if (userData == null) {
            System.out.println("Error: No data loaded.");
            return null; 
        }
    
        for (int i = 1; i < userData.size(); i++) {
            String[] row = userData.get(i);
            if (row.length > 1 && NRIC.equals(row[1])) {
                System.out.println("Valid NRIC");
                return row; 
            }
        }
    
        System.out.println("Invalid NRIC. Please enter valid NRIC:");
        return null; 
    }


    public static boolean checkPassword(String password, String[] userRow) { 
        if (userRow == null) {
            System.out.println("Error: No data loaded."); 
            return false;
        }
    
        if (userRow.length > 4 && password.equals(userRow[4])) { 
            System.out.println("You have logged in!");
            return true;
        }
    
        System.out.println("Invalid password. Please enter valid password:");
        return false;
    }
}


