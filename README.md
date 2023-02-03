# SYSC3303A-Project
Elevator Control System and Simulator
Iteration 1 simulates a elevator using java threads represented with string prompts in the terminal. 
Users are notified when the elevator has finished a request/move and where that request is. 

Installation
    Unzip Iteration1_Group6.zip and open the folder in Eclipse. 

Floor.java accepts events from InputData.java. Each event consists of the current time, the floor request, 
the direction of travel and the car button pressed. These events are sent to Scheduler.java

Elevator.java waits for the Scheduler to receive an event. It becomes active once the Scheulder.java is __ 
with an event in elevatorQueue ArrayList. The move is executed and the new data is sent to Scheduler.java.

Scheduler.java is a organized messanger between Floor.java and Elevator.java. It accepts events from Floor.java
and sends this event to Elevator.java. New data is recieved from Elevator.java and is sent to Floor.java. 


