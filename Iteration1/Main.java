package Iteration1;

public class Main {
    public static void main(String[] args) {
        Scheduler scheduler = new Scheduler();
        Floor floor = new Floor(scheduler);
        Elevator elevator1 = new Elevator(scheduler, 1);
        Elevator elevator2 = new Elevator(scheduler, 2);
        Elevator elevator3 = new Elevator(scheduler, 3);

        Thread agentThread = new Thread(floor);
        Thread breadThread = new Thread(elevator1);
        Thread jamThread = new Thread(elevator2);
        Thread peanutThread = new Thread(elevator3);

        agentThread.start();
        breadThread.start();
        jamThread.start();
        peanutThread.start();
    }
}
