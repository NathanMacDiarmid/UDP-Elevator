
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class SchedulerTest {

    private Scheduler scheduler;
    private Thread elevatorThread;
    private Thread floorThread;
    private Elevator elevator;
    private Floor floor;
    private InputData testDataPoint1;

    @BeforeEach
    public void Setup() {
        scheduler = new Scheduler();
        elevator = new Elevator(scheduler);
        floor = new Floor(scheduler);
        floorThread = new Thread(floor, "floor");
        elevatorThread = new Thread(elevator, "elevator1");
        scheduler = new Scheduler();

    }

    /**
     * This test verifies that the readData() input from the Floor class is
     * properly parsing the text file.
     * 
     * THIS TEST CASE ONLY WORKS IN ECLIPSE NOT VSCODE
     */
    // @Test
    // public void testReadData() {
    // Scheduler scheduler = new Scheduler();
    // Floor floor = new Floor(scheduler);
    // floor.readData();
    // ArrayList<InputData> elevatorQueue = floor.getElevatorQueue();

    // InputData data0 = elevatorQueue.get(0);

    // assertEquals(50715000, data0.getTimeOfRequest());
    // assertEquals(2, data0.getFloor());
    // assertEquals(true, data0.isDirectionUp());
    // assertEquals(5, data0.getCarRequest());

    // InputData data1 = elevatorQueue.get(1);
    // assertEquals(50717000, data1.getTimeOfRequest());
    // assertEquals(7, data1.getFloor());
    // assertEquals(false, data1.isDirectionUp());
    // assertEquals(1, data1.getCarRequest());

    // InputData data2 = elevatorQueue.get(2);
    // assertEquals(50725900, data2.getTimeOfRequest());
    // assertEquals(1, data2.getFloor());
    // assertEquals(true, data2.isDirectionUp());
    // assertEquals(7, data2.getCarRequest());

    // InputData data3 = elevatorQueue.get(3);
    // assertEquals(50760000, data3.getTimeOfRequest());
    // assertEquals(5, data3.getFloor());
    // assertEquals(false, data3.isDirectionUp());
    // assertEquals(3, data3.getCarRequest());
    // }

    /**
     * This test ensures that the currentFloor and nextFloor members of the
     * Scheduler class are being properly updated when the Scheduler class calls
     * it's get() and put() methods.
     */
    @Test
    public void testPutAndGet() {
        Scheduler scheduler = new Scheduler();
        testDataPoint1 = new InputData(60, 1, true, 2);
        InputData testDataPoint2 = new InputData(60, 3, true, 5);
        ArrayList<InputData> data = new ArrayList<>();
        data.add(testDataPoint1);
        data.add(testDataPoint2);

        scheduler.moveElevator(data, 5);
        assertEquals(5, scheduler.getCurrentFloor());

        scheduler.moveElevator(data, 1);

        assertEquals(1, scheduler.getCurrentFloor());

    }

    /**
     * Test ensuring that when an InputData is added to the scheduler, the scheduler
     * is able to maintain the integrity of the data
     * 
     * @author Michael Kyrollos
     * @version 24/02/2023
     */
    @Test
    public void testAddingAFloorRequest() {
        testDataPoint1 = new InputData(60, 1, true, 2);
        ArrayList<InputData> data = new ArrayList<>();
        data.add(testDataPoint1);
        scheduler.putFloorRequest(testDataPoint1, false);
        Map<Integer, ArrayList<InputData>> testdata = new HashMap<>();
        testdata.put(1, data);
        assertEquals(testdata.get(1), scheduler.getFloorQueues().get(1));

        InputData testDataPoint2 = new InputData(60, 3, true, 5);
        data.add(testDataPoint2);

    }

    /**
     * Test ensuring that the field noMoreRequests is updated properly during the
     * program runtime.
     * 
     * @author Michael Kyrollos
     * @version 24/02/2023
     */
    @Test
    public void testNoMoreRequestsAsFalse() {
        testDataPoint1 = new InputData(75, 1, true, 4);
        ArrayList<InputData> data = new ArrayList<>();
        // must be false when there is no data in the requests
        assertFalse(scheduler.isNoMoreRequests());

        data.add(testDataPoint1);
        // adding a request that is the last request in the list
        scheduler.putFloorRequest(testDataPoint1, true);
        Map<Integer, ArrayList<InputData>> testdata = new HashMap<>();
        testdata.put(1, data);
        // insuring the putting of the data was done correclty (similar to test above)
        assertEquals(testdata.get(1), scheduler.getFloorQueues().get(1));
        // this should be the last data point
        assertTrue(scheduler.isNoMoreRequests());

    }

    /**
     * Test ensuring that the field noMoreRequests is updated properly during the
     * program runtime.
     * 
     * @author Michael Kyrollos
     * @version 24/02/2023
     */
    @Test
    public void testNoMoreRequestsAsTrue() {
        testDataPoint1 = new InputData(25, 7, false, 2);
        ArrayList<InputData> data = new ArrayList<>();
        // must be false when there is no data in the requests
        assertFalse(scheduler.isNoMoreRequests());

        data.add(testDataPoint1);
        // adding a request that is not the last request in the list
        scheduler.putFloorRequest(testDataPoint1, false);
        Map<Integer, ArrayList<InputData>> testdata = new HashMap<>();
        testdata.put(1, data);
        // insuring the putting of the data was done correclty (similar to test above)
        assertEquals(testdata.get(1), scheduler.getRequestQueue());
        // this should not be the last data point
        assertFalse(scheduler.isNoMoreRequests());

    }

}
