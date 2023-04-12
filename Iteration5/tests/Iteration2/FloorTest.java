package Iteration5.tests.Iteration2;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

public class FloorTest {
    private Scheduler scheduler;
    private Thread elevatorThread;
    private Thread floorThread;
    private Elevator elevator;
    private Floor floor;

    @BeforeEach
    public void Setup() {
        scheduler = new Scheduler();
        elevator = new Elevator(scheduler);
        floor = new Floor(scheduler);
        floorThread = new Thread(floor, "floor");
        elevatorThread = new Thread(elevator, "elevator1");
        scheduler = new Scheduler();

    }

    @AfterEach
    public void tearDown() {
        elevatorThread.interrupt();
        floorThread.interrupt();
    }

    /**
     * Tests that the file has been read properly, using the first line only.
     * Tests below will cover other cases.
     * 
     * @author Michael Kyrollos
     */
    @Test
    public void testFileReadCorrect() {
        floor.readData("dataForTests.txt");
        assertEquals(7, floor.getElevatorQueue().get(0).getCarRequest());
        assertEquals(3, floor.getElevatorQueue().get(0).getFloor());

    }

    /**
     * Tests that the file order is correct after parsing.
     * 
     * @author Michael Kyrollos
     */
    @Test
    public void testFileOrder() {
        floor.readData("dataForTests.txt");
        // checking that the original 2nd datapoint is now moved to the last
        assertEquals(3, floor.getElevatorQueue().get(0).getFloor());
        assertEquals(7, floor.getElevatorQueue().get(0).getCarRequest());
        // checking random data point
        assertEquals(3, floor.getElevatorQueue().get(1).getFloor());
        assertEquals(1, floor.getElevatorQueue().get(1).getCarRequest());

    }

    /**
     * Testing that the String has been converted (up/down) is converted to boolean
     * correctly.
     * 
     * @author Michael Kyrollos
     */
    @Test
    public void testUpperLowerCase() {
        floor.readData("dataForTests.txt");
        // checking that the up/down String is converted properly
        assertFalse(floor.getElevatorQueue().get(1).getIsDirectionUp());
    }

    /**
     * Testing that the file (used for main program) is read correctly, line by
     * line.
     * correctly.
     * 
     * @author Michael Kyrollos
     */
    @Test
    public void testActualFileReadCorrect() {
        floor.readData("data.txt");
        ArrayList<InputData> elevatorQueue = floor.getElevatorQueue();

        InputData data0 = elevatorQueue.get(0);
        assertEquals(60480000, data0.getTimeOfRequest());
        assertEquals(1, data0.getFloor());
        assertEquals(true, data0.isDirectionUp());
        assertEquals(4, data0.getCarRequest());

        InputData data1 = elevatorQueue.get(1);
        assertEquals(60484000, data1.getTimeOfRequest());
        assertEquals(3, data1.getFloor());
        assertEquals(true, data1.isDirectionUp());
        assertEquals(5, data1.getCarRequest());

        InputData data2 = elevatorQueue.get(2);
        assertEquals(60490000, data2.getTimeOfRequest());
        assertEquals(6, data2.getFloor());
        assertEquals(false, data2.isDirectionUp());
        assertEquals(3, data2.getCarRequest());

    }

    /**
     * Tests that the down lamp field is maintained during the running of the thread
     * 
     * @author Michael Kyrollos
     * @version 23/02/2023
     */
    @Test
    public void testSetRequestDownLamp() {
        floor.setRequestDownButtonLamp(false);
        elevatorThread.start();
        floorThread.start();
        assertFalse(floor.getRequestDownButton());
    }

    /**
     * Tests that the direction lamp field is maintained during the running of the
     * thread
     * 
     * @author Michael Kyrollos
     * @version 23/02/2023
     */
    @Test
    void testSetDirectionLamp() {
        floor.setDirectionLamp("up");
        elevatorThread.start();
        floorThread.start();
        assertEquals("up", floor.getDirectionLamp());
        floor.setDirectionLamp("down");
        assertEquals("down", floor.getDirectionLamp());
    }

    /**
     * Testing that the request up button field is intact after thread running.
     * 
     * @author Michael Kyrollos
     */
    @Test
    void testSetRequestUpButton() {
        floor.setRequestUpButton(true);
        elevatorThread.start();
        floorThread.start();
        assertTrue(floor.getRequestUpButton());

    }

}
