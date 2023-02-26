
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class ElevatorTest {
    private Scheduler scheduler;
    private Thread elevatorThread;
    private Thread floorThread;
    private Elevator elevator;
    private Floor floor;
    private InputData testDataPoint1;

    @BeforeEach
    public void setUp() {
        scheduler = new Scheduler();
        elevator = new Elevator(scheduler);
        floor = new Floor(scheduler);
        floorThread = new Thread(floor, "floor");
        elevatorThread = new Thread(elevator, "elevator1");

    }

    @Test
    void testGetCurrentFloor() throws InterruptedException {
        Elevator controller = new Elevator(scheduler);
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        // create a new elevator instruction to add to the queue

        // execute the add request logic on a separate thread
        // ensure that the instruction was added to the floor queue and the main request
        // queue
        Thread.sleep(1000); // wait for the instruction to be added
        // assertTrue(controller.getFloorQueue(1).contains(instruction));
    }

    @Test
    public void testNoMoreRequestsAsFalsse() {
        scheduler.setQueueInUse(false);
        testDataPoint1 = new InputData(60, 1, true, 2);
        ArrayList<InputData> data = new ArrayList<>();
        data.add(testDataPoint1);
        scheduler.putFloorRequest(testDataPoint1, false);
        Map<Integer, ArrayList<InputData>> testdata = new HashMap<>();
        testdata.put(1, data);
        assertEquals(testdata.get(1), scheduler.getFloorQueues().get(1));

        InputData testDataPoint2 = new InputData(60, 3, true, 5);
        data.add(testDataPoint2);
        elevator.initiateElevator();
        elevator.getRequestQueue().add(testDataPoint1);
        assertEquals(1, elevator.getRequestQueue().size());

    }

    @Test
    void testGetDoorOpen() {
        assertEquals(false, elevator.getDoorOpen());
    }

    @Test
    void testGetFloorButtons() {

    }

    // @Test
    // void testGetFloorButtonsLamps() {

    // }

    // @Test
    // void testGetMotorMoving() {

    // }

    // @Test
    // void testGetRequestQueue() {

    // }

    // @Test
    // void testGetScheduler() {

    // }

    // @Test
    // void testRun() {

    // }

    // @Test
    // void testSetDoorOpen() {

    // }

    // @Test
    // void testSetFloorButton() {

    // }

    // @Test
    // void testSetFloorButtonLamps() {

    // }

    // @Test
    // void testSetMotorMoving() {

    // }

}
