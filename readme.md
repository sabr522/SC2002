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