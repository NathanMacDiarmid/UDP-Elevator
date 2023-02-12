package src;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;


class UnitTests {


    /**
     * This test verifies that the readData() input from the Floor class is
     * properly parsing the text file.
     * 
     * THIS TEST CASE ONLY WORKS IN ECLIPSE NOT VSCODE
     */
    @Test
    public void testReadData(){
        Scheduler scheduler = new Scheduler();
        Floor floor = new Floor(scheduler);
        floor.readData();
        ArrayList<InputData> elevatorQueue = floor.getElevatorQueue();

        InputData data0 = elevatorQueue.get(0);
        assertEquals(50715000, data0.getTimeOfRequest());
        assertEquals(2, data0.getFloor());
        assertEquals(true, data0.isDirectionUp());
        assertEquals(5, data0.getCarRequest());

        InputData data1 = elevatorQueue.get(1);
        assertEquals(50717000, data1.getTimeOfRequest());
        assertEquals(7, data1.getFloor());
        assertEquals(false, data1.isDirectionUp());
        assertEquals(1, data1.getCarRequest());

        InputData data2 = elevatorQueue.get(2);
        assertEquals(50725900, data2.getTimeOfRequest());
        assertEquals(1, data2.getFloor());
        assertEquals(true, data2.isDirectionUp());
        assertEquals(7, data2.getCarRequest());

        InputData data3 = elevatorQueue.get(3);
        assertEquals(50760000, data3.getTimeOfRequest());
        assertEquals(5, data3.getFloor());
        assertEquals(false, data3.isDirectionUp());
        assertEquals(3, data3.getCarRequest());
    }

    /**
     * This test ensures that the currentFloor and nextFloor members of the Scheduler
     * class are being properly updated when the Scheduler class calls it's get() and
     * put() methods.
     */
    @Test
    public void testPutAndGet(){
        Scheduler scheduler = new Scheduler();
        InputData dummyData = new InputData(60, 1, true, 2);

        scheduler.putFloorRequest(dummyData);

        assertEquals(1, scheduler.getCurrentFloor());
        assertEquals(2, scheduler.getNextFloor());

        int result = scheduler.getFloorRequest(3);

        assertEquals(2, result);
        assertEquals(3, scheduler.getCurrentFloor());
    }
}

