package Iteration1;

public class Main {
    public static void main(String[] args) {
        Scheduler scheduler = new Scheduler();
        Floor floor = new Floor(scheduler);
        Elevator elevator1 = new Elevator(scheduler);

        Thread floorThread = new Thread(floor);
        Thread elevatorThread1 = new Thread(elevator1);

        floorThread.start();;
        elevatorThread1.start();;
    }
}

