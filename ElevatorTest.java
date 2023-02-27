
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

class ElevatorTest {
    private Scheduler scheduler;
    private Thread elevatorThread;
    private Thread floorThread;
    private Elevator elevator;
    private Floor floor;

    @BeforeEach
    public void setUp() {
        scheduler = new Scheduler();
        elevator = new Elevator(scheduler);
        floor = new Floor(scheduler);
        floorThread = new Thread(floor, "floor");
        elevatorThread = new Thread(elevator, "elevator1");

    }

    @Test
    public void testNoMoreRequestsAsFalsse() {
        elevatorThread.start();
        floorThread.start();
        sleepProgram(20);

        ArrayList<InputData> expectedList = new ArrayList<>();
        expectedList.add(new InputData(convertTimeToLong(16, 48, 00, 0), 1, true, 4));

        expectedList.add(new InputData(convertTimeToLong(16, 48, 04, 0), 3, true, 5));
        assertEquals(expectedList.get(0).getTimeOfRequest(), elevator.getRequestQueue().get(0).getTimeOfRequest());

    }

    @Test
    void testGetDoorOpen() {
        elevatorThread.start();
        floorThread.start();
        // make the program sleep for a few seconds allowing the assert statements to
        // get accurate data
        sleepProgram(20);
        assertFalse(elevator.getDoorOpen());
    }

    @Test
    void testGetMotorMoving() {
        elevatorThread.start();
        floorThread.start();
        // make the program sleep for a few seconds allowing the assert statements to
        // get accurate data
        sleepProgram(10);
        assertFalse(elevator.getMotorMoving());
    }

    @Test
    void testGetMotorMovingTrue() {
        elevatorThread.start();
        floorThread.start();
        // make the program sleep for a few seconds allowing the assert statements to
        // get accurate data
        sleepProgram(5);
        assertTrue(elevator.getMotorMoving());
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
