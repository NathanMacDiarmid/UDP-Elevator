# SYSC3303A-Project
Elevator Control System and Simulator

Iteration 1 simulates a elevator using java threads represented with string prompts in the terminal. 
Users are notified when the elevator has finished a request/move and where that request is. 

## Contributions:
🥇 Amanda Piazza (101143004)
  * UML class and sequence diagram
  * README
     
🥇 Juanita Rodelo (101141857) 

   * UML class and sequence diagram
     
🥇 Matthew Belanger (101144323)

   * UnitTests
     
🥇 Michael Kyrollos (101183521)  

   * Parsing and importing the txt, README    
     
🥇 Nathan MacDiarmid (101098993) 

   * Communication between floor, elevator, and scheduler

### Structure 
   * Floor.java accepts events from InputData.java. Each event consists of the current time, the floor request, 
   the direction of travel and the car button pressed. These events are sent to Scheduler.java

   * Elevator.java waits for the Scheduler to receive an event. It becomes active once the Scheulder.java is __ 
   with an event in elevatorQueue ArrayList. The move is executed and the new data is sent to Scheduler.java.

   * Scheduler.java is a organized messanger between Floor.java and Elevator.java. It accepts events from Floor.java
   and sends this event to Elevator.java. New data is recieved from Elevator.java and is sent to Floor.java. 

## Running the test cases in VS Code 
Open the settings.json file in VS code and modify according to the image below.
![image](https://user-images.githubusercontent.com/83596468/221445499-d3fae8da-e41a-457e-83e5-9443eeff65b3.png)

### How to run the program (if you are using VS Code, scroll to the bottom)

1. Open Eclipse and create a new project (File -> New -> Java Project) 
![image](https://user-images.githubusercontent.com/83596468/216786219-4e559573-85a0-4100-81df-c2c23d15ea32.png)

2. Name the project `Iteration1` and click Finish
![image](https://user-images.githubusercontent.com/83596468/216786386-3ffaf643-faab-4255-9908-d77b35eca975.png)


3. Create a package by right clicking on the `/src` directory within the project folder in Eclipse, name it `Iteration1`
![image](https://user-images.githubusercontent.com/83596468/216786372-91076533-1801-4994-bf42-1e1a536fa466.png)

4. Unzip `LA1G6_Iteration1.zip`

5. Right click on the package and click import. 
![image](https://user-images.githubusercontent.com/83596468/216786495-5d985799-9387-400c-9259-7408bfebccbc.png)
 
 6. Import as *File Systems*
![image](https://user-images.githubusercontent.com/83596468/216786536-9a76c9f4-8cd5-4bbf-a2cf-cd02b018e413.png)

7. Locate the directory in which the `Java` src files can be found. (From the Extracted zip in Step 4). From the GitHub Repo, it would be *\SYSC3303A-Project\Iteration1\src\Iteration1*
![image](https://user-images.githubusercontent.com/83596468/216787074-54510204-1e15-4962-bf5e-0ccc8cf7e359.png)

Include the following files 
  * Elevator.java

  * Floor.java

  * InputData.java

  * Main.java

  * Scheduler.java

  * UnitTests.java
    
8. Run the program with the green play button at the top
![image](https://user-images.githubusercontent.com/83596468/216787181-8ad4004f-394d-4835-aacc-642752549049.png)

### If JUnit tests are not working after importing the project 
If there is an *x* symbol beside `UnitTests.java`, follow the intructions below. 
1. Open `UnitTests.java` and hover the mouse over the `@Test` code that is underlined red. Click on *Add JUnit 5 library to the build path* 
![image](https://user-images.githubusercontent.com/83596468/216787312-73facca9-c433-4b90-a1af-33095390789c.png)
2. Hover over the import statement at the top of the file and click on the circled button in the picture below. 
![image](https://user-images.githubusercontent.com/83596468/216787347-d2ad945f-54fb-4d6e-a3af-02d162ca3b9c.png)

3. Hover over the `@Test` code that is underlined red. Click on *Fix Project Setup*. Click on *Add JUnit 5 library to the build path* -> choose *org.junit.jupiter.api.Test*
![image](https://user-images.githubusercontent.com/83596468/216787518-42d8ac40-b375-4fc7-a69d-2f32f2eca610.png)

4. Navigate to the *module-info.java* file and open it. Hover over the red underlined code and click on the option to change the build path. The tests should be working now. 
