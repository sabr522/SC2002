package Login;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
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

    public static boolean changePassword(String[] userRow, String newPass, String confirmPass, ArrayList<String[]> data, String filePath) {
        if (!newPass.equals(confirmPass)) {
            System.out.println("Passwords do not match.");
            return false;
        }
    
        userRow[4] = newPass;
        System.out.println("Password updated successfully.");  
    
        saveDataToCSV(filePath, data); 
        return true;
    }
    
    private static void saveDataToCSV(String filePath, ArrayList<String[]> data) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            for (String[] row : data) {
                StringBuilder sb = new StringBuilder();
                for (String field : row) {
                    sb.append(field).append(",");
                }
                sb.setLength(sb.length() - 1); 
                bw.write(sb.toString());
                bw.newLine();
            }
            System.out.println("Data successfully written to CSV.");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to write data to CSV.");
        }
    }

}
