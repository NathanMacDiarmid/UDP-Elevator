package Iteration5.tests.Iteration5;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import src.Floor;
import src.InputData;
import src.Scheduler;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.util.ArrayList;

public class FloorTest {
    private Scheduler scheduler;
    private Floor floor;

    @BeforeEach
    public void Setup() {
        scheduler = new Scheduler(2);
        floor = new Floor();

    }

    @AfterEach
    public void tearDown() {
        scheduler.closeSockets();
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
        assertEquals(5, floor.getElevatorQueue().get(0).getCarRequest());
        assertEquals(3, floor.getElevatorQueue().get(0).getFloor());
        scheduler.closeSockets();

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
        assertEquals(5, floor.getElevatorQueue().get(0).getCarRequest());
        // checking random data point
        assertEquals(3, floor.getElevatorQueue().get(8).getFloor());
        assertEquals(3, floor.getElevatorQueue().get(10).getCarRequest());

        // checking random data point
        assertEquals(4, floor.getElevatorQueue().get(4).getFloor());
        // Make sure the invalid data is skipped - original input size is 13 and 
        // 2 of them are invalid thus the size of the queue should be 11
        assertEquals(11, floor.getElevatorQueue().size());
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
        assertTrue(floor.getElevatorQueue().get(1).getIsDirectionUp());
    }

    /**
     * Testing that the data integrity is maintained when reading a file.
     * I.e. time of request for each line is associated with the right floor to floor 
     * request. 
     * 
     * @author Michael Kyrollos
     */
    @Test
    public void testActualFileReadCorrect() {
        floor.readData("dataForTests.txt");
        ArrayList<InputData> elevatorQueue = floor.getElevatorQueue();

        InputData data0 = elevatorQueue.get(0);
        assertEquals(12163900, data0.getTimeOfRequest());
        assertEquals(3, data0.getFloor());
        assertEquals(true, data0.isDirectionUp());
        assertEquals(5, data0.getCarRequest());
        assertEquals(false, data0.getDoorNotCloseError());
        assertEquals(false, data0.getDoorNotOpenError());
        assertEquals(false, data0.getElevatorStuckError());

        InputData data1 = elevatorQueue.get(1);
        assertEquals(12164000, data1.getTimeOfRequest());
        assertEquals(5, data1.getFloor());
        assertEquals(true, data1.isDirectionUp());
        assertEquals(7, data1.getCarRequest());
        assertEquals(false, data1.getDoorNotCloseError());
        assertEquals(false, data1.getDoorNotOpenError());
        assertEquals(false, data1.getElevatorStuckError());

        InputData data2 = elevatorQueue.get(2);
        assertEquals(12269600, data2.getTimeOfRequest());
        assertEquals(3, data2.getFloor());
        assertEquals(true, data2.isDirectionUp());
        assertEquals(7, data2.getCarRequest());
        assertEquals(true, data2.getDoorNotCloseError());
        assertEquals(false, data2.getDoorNotOpenError());
        assertEquals(false, data2.getElevatorStuckError());

        InputData data3 = elevatorQueue.get(3);
        assertEquals(12269600, data3.getTimeOfRequest());
        assertEquals(6, data3.getFloor());
        assertEquals(false, data3.isDirectionUp());
        assertEquals(1, data3.getCarRequest());
        assertEquals(false, data3.getDoorNotCloseError());
        assertEquals(false, data3.getDoorNotOpenError());
        assertEquals(false, data3.getElevatorStuckError());

        InputData data4 = elevatorQueue.get(4);
        assertEquals(12269600, data4.getTimeOfRequest());
        assertEquals(4, data4.getFloor());
        assertEquals(false, data4.isDirectionUp());
        assertEquals(1, data4.getCarRequest());
        assertEquals(false, data4.getDoorNotCloseError());
        assertEquals(false, data4.getDoorNotOpenError());
        assertEquals(false, data4.getElevatorStuckError());

        InputData data5 = elevatorQueue.get(5);
        assertEquals(12570200, data5.getTimeOfRequest());
        assertEquals(4, data5.getFloor());
        assertEquals(true, data5.isDirectionUp());
        assertEquals(6, data5.getCarRequest());
        assertEquals(false, data5.getDoorNotCloseError());
        assertEquals(false, data5.getDoorNotOpenError());
        assertEquals(false, data5.getElevatorStuckError());

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
        assertEquals("up", floor.getDirectionLamp());
        floor.setDirectionLamp("down");
        assertEquals("down", floor.getDirectionLamp());
    }

    /**
     * Testing that the request up button field is intact after running.
     * 
     * @author Michael Kyrollos
     */
    @Test
    void testSetRequestUpButton() {
        floor.setRequestUpButton(true);
        assertTrue(floor.getRequestUpButton());

    }

    /**
    * Test that the floor can send the right instruction in the packet. 
    * 
    * @author Michael Kyrollos
    * @version 10/04/2023
    */
    @Test
    void testSendPacketDataInSendInstruction() {
        floor.readData("dataForTests.txt");

        // First item in the queue
        floor.sendInstruction(floor.getElevatorQueue().get(0), false);
        byte[] data = floor.getSendPacket().getData();
        int len = floor.getSendPacket().getLength();
        String received = new String(data, 0, len);
        // InputData data0 = new InputData(len, len, null, len)
        String expectedString = "InputData [currentTime=03:22:43.900, floor=3, isDirectionUp=true, car button=5, doorNotOpenError=false, doorNotCloseError=false, elevatorStuckError=false]: false";
        assertEquals(expectedString, received);

        // second item in the queue 
        floor.sendInstruction(floor.getElevatorQueue().get(1), false);
        data = floor.getSendPacket().getData();
        len = floor.getSendPacket().getLength();
        received = new String(data, 0, len);
        // InputData data0 = new InputData(len, len, null, len)
        expectedString = "InputData [currentTime=03:22:44.0, floor=5, isDirectionUp=true, car button=7, doorNotOpenError=false, doorNotCloseError=false, elevatorStuckError=false]: false";
        assertEquals(expectedString, received);

        // last item 
        floor.sendInstruction(floor.getElevatorQueue().get(10), true);
        data = floor.getSendPacket().getData();
        len = floor.getSendPacket().getLength();
        received = new String(data, 0, len);
        // InputData data0 = new InputData(len, len, null, len)
        expectedString = "InputData [currentTime=16:18:18.400, floor=7, isDirectionUp=false, car button=3, doorNotOpenError=true, doorNotCloseError=true, elevatorStuckError=true]: true";
        assertEquals(expectedString, received);
    }

    /**
    * Test that the packet is sent on the correct port (23)
    *  
    * @author Michael Kyrollos
    * @version 10/04/2023
    */
    @Test
    void testSendPacketPortInSendInstruction() throws UnknownHostException {
        floor.readData("dataForTests.txt");
        floor.sendInstruction(floor.getElevatorQueue().get(0), false);
        // test that port it is being sent over is port 23
        assertEquals(23, floor.getSendPacket().getPort());

        floor.sendInstruction(floor.getElevatorQueue().get(10), true);
        // test that port it is being sent over is port 23
        assertEquals(23, floor.getSendPacket().getPort());
    }

    /**
     * Test that the packet is sent on the correct address
     *  
     * @author Michael Kyrollos
     * @version 10/04/2023
     */
    @Test
    void testLocalAddressforSendPacketInSendInstruction() throws UnknownHostException {
        floor.readData("dataForTests.txt");
        floor.sendInstruction(floor.getElevatorQueue().get(0), false);
        // testing the address is the current address of the computer 
        assertEquals(InetAddress.getLocalHost(), floor.getSendPacket().getAddress());
    }

    /**
     * Test that the scheduler is receiving the correct data from the Floor.
     *  
     * @author Michael Kyrollos
     * @version 10/04/2023
     */
    @Test
    void testDataSendReceiveSocketInSendInstruction() throws UnknownHostException {
        floor.readData("dataForTests.txt");
        floor.sendInstruction(floor.getElevatorQueue().get(0), false);
        scheduler.receiveInstructionFromFloor();
        byte[] data = scheduler.getReceivePacket23().getData();
        int len = scheduler.getReceivePacket23().getLength();
        String received = new String(data, 0, len);
        // InputData data0 = new InputData(len, len, null, len)
        String expectedString = "InputData [currentTime=03:22:43.900, floor=3, isDirectionUp=true, car button=5, doorNotOpenError=false, doorNotCloseError=false, elevatorStuckError=false]: false";
        assertEquals(expectedString, received);
        assertEquals(InetAddress.getLocalHost(), scheduler.getReceivePacket23().getAddress());
        assertEquals(161, scheduler.getReceivePacket23().getLength());
    }

    /**
     * Test that the floor receives the data from the acknowledgment of the 
     * scheduler in proper format.
     *  
     * @author Michael Kyrollos
     * @version 10/04/2023
     */
    @Test
    void receiveAcknowledgementinFloor() throws UnknownHostException {
        floor.readData("dataForTests.txt");
        floor.sendInstruction(floor.getElevatorQueue().get(0), false);
        scheduler.receiveInstructionFromFloor();
        scheduler.sendFloorAcknowledgement();
        floor.receiveAcknowledgement();

        byte[] data = floor.getReceivePacket().getData();
        int len = floor.getReceivePacket().getLength();

        String received = new String(data, 0, len);
        assertEquals(InetAddress.getLocalHost(), floor.getReceivePacket().getAddress());
        assertEquals(39, len);
        assertEquals("The Scheduler has accepted the message.", received);

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
