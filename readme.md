## Expected project structure

 ```
 root folder/
|-- src/                       # Source code root
|   |-- main/                  # Main application package
|   |   |-- MainApp.java      
|   |
|   |-- actors/               # Base logic classes package
|   |   |-- Manager.java      
|   |   |-- Applicant.java    
|   |   |-- Officer.java      
|   |   |-- User.java         
|   |   |-- Enquiry.java      
|   |
|   |-- project/              # Project-related logic
|   |   |-- Project.java     
|   |
|   |-- cli/                   # Command Line Interface package
|   |   |-- LoginCLI.java
|   |   |-- ManagerCLI.java
|   |   |-- ApplicantCLI.java
|   |   |-- OfficerCLI.java   
|   |   |-- EnquiryCLI.java  
|   |
|   |-- data/                  # Data handling package
|   |   |-- DataManager.java 
|
|-- data_files/              # Folder for CSV files (outside src)
    |-- users.csv
    |-- projects.csv
    |-- applicants.csv
    |-- officers.csv
    |-- enquiries.csv
```

Login successfully (any role).
Choose "Change Password".
Enter non-matching new/confirm passwords -> Expected: Fails, prompts again or returns to menu.
Enter matching, valid new passwords -> Expected: Success message, user is logged out (forced re-login).
Try logging in with old password -> Expected: Fails (password check).
Log in with new password -> Expected: Succeeds.
