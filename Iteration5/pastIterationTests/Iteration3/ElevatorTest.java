package Iteration5.tests.Iteration3;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import src.Elevator;
import src.Floor;
import src.Scheduler;

import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.util.concurrent.TimeUnit;

class ElevatorTest {
    private Scheduler scheduler;
    private Elevator elevator1, elevator2;
    private Floor floor;

    @BeforeEach
    public void Setup() {
        scheduler = new Scheduler(2);
        elevator1 = new Elevator(1, 1, "up");
        elevator2 = new Elevator(2, 1, "up");
        floor = new Floor();
    }

    @AfterEach
    public void tearDown() {
   	 scheduler.closeSockets();
    }
    
    
    /**
     * This tests the sendRequest() method in the elavator class by checking
     * that the data received is the same as whats being sent.
     * 
     * @author Matthew Belanger 101144323
     */
    @Test
    public void testSendRequest(){
        elevator1.sendRequest();
        scheduler.receiveElevatorRequest();
        String received = new String(scheduler.getReceivePacket69().getData(), 0, scheduler.getReceivePacket69().getLength());   
        assertEquals("Elevator car #: 1 Floor: 1 Num of people: 0 Serviced: 0 Direction: up", received);
    }

    /**
     * This tests the receiveInstruction() method in the elavator class by checking
     * that the data received is the same as whats being sent.
     * 
     * @author Matthew Belanger 101144323
     */
    @Test
    public void testReceiveInstruction(){
        int elevatorPort;
        floor.readData("dataForTests.txt");
        floor.sendInstruction(floor.getElevatorQueue().get(0), false);
        scheduler.receiveInstructionFromFloor();        
        scheduler.sendFloorAcknowledgement();
        floor.receiveAcknowledgement();
        elevator1.sendRequest();
        elevator2.sendRequest();
        for(int i = 1; i < scheduler.getNumOfCars() + 1; i++){
            elevatorPort = scheduler.receiveElevatorRequest();
            scheduler.elevatorAndTheirPortsPut(i, elevatorPort);
        }  
        scheduler.sendToElevators(); 
        elevator1.receiveInstruction();
        elevator2.receiveInstruction();
        String received1 = new String(elevator1.getReceivePacket().getData(), 0, elevator1.getReceivePacket().getLength());   
        assertEquals("InputData [currentTime=03:22:43.900, floor=3, isDirectionUp=true, car button=5]: ", received1.split("false")[0]);
        String received2 = new String(elevator2.getReceivePacket().getData(), 0, elevator2.getReceivePacket().getLength());   
        assertEquals("No current requests", received2);
    }

    /**
     * Helper function helping when writing test cases that allows the program to be
     * to sleep.
     * 
     * @param time The amount of time for the program to sleep in seconds
     * @author Michael Kyrollos
     */
    private void sleepProgram(double time) {
        try {
            TimeUnit.SECONDS.sleep((long) time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Helper function helping when writing test cases that involve time conversion,
     * allows a more readable format.
     * 
     * @param hour       The hour part of the time
     * @param minute     The second part of the time
     * @param second     The minute part of the time
     * @param nanosecond The nanosecond part of the time
     * @return The time reprsented as a an integer
     * @author Michael Kyrollos
     */
    public int convertTimeToLong(int hour, int minute, int second, int nanosecond) {
        LocalTime time = LocalTime.of(hour, minute, second, nanosecond);
        // converting the LocalTime to an integer, will stored as an int that represents
        // the millisecond of the day
        return time.get(ChronoField.MILLI_OF_DAY);
    }

}


