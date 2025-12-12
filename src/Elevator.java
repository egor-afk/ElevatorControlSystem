import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class Elevator implements Runnable {
    private final int id;
    private Dispatcher dispatcher;
    private int currentFloor;
    private Direction direction;
    private ElevatorStatus status;
    private final List<Request> requests = new ArrayList<>();
    private final List<Command> commands = new ArrayList<>();
    private final TreeSet<Integer> goalFloors = new TreeSet<>();
    private int countPeople;
    private Command command = null;
    private boolean waitingCommand = false;
    private final Object lock = new Object();


    public Elevator(int id){
        this.id = id;
        this.countPeople = 0;
        this.currentFloor = 1;
        this.direction = Direction.NONE;
        this.status = ElevatorStatus.STOPPED;
    };

    public void run() {
        while(true) {
            synchronized (lock) {
                if (!requests.isEmpty()) {
                    if (currentFloor < requests.getFirst().getCurrentFloor()) {
                        status = ElevatorStatus.MOVING_UP;
                    } else if (currentFloor > requests.getFirst().getCurrentFloor()) {
                        status = ElevatorStatus.MOVING_DOWN;
                    } else {
                        status = ElevatorStatus.STOPPED;
                    }
                    while (currentFloor != requests.getFirst().getCurrentFloor()) {
                        ElevatorStatus currentStatus = status;
                        if(goalFloors.contains(currentFloor)){
                            System.out.println("cicle");
                            stop();
                        }
                        try {
                            lock.wait(1000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        status = currentStatus;
                        move();
                    }
                    stop();
                    System.out.println("Направление комманды " + commands.getLast().getDirection());
                    if (commands.getLast().getDirection() == Direction.UP) {
                        direction = Direction.UP;
                        status = ElevatorStatus.MOVING_UP;
                    } else if (commands.getLast().getDirection() == Direction.DOWN) {
                        direction = Direction.DOWN;
                        status = ElevatorStatus.MOVING_DOWN;
                    }
                    Command mainCommand = commands.getLast();
                    while (currentFloor != mainCommand.getTargetFloor()) {
                        try {
                            lock.wait(1000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        ElevatorStatus currentStatus = status;
                        move();
                        if(goalFloors.contains(currentFloor)){
                            stop();
                        }
                        status = currentStatus;
                    }
                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void move(){
        if(currentFloor < dispatcher.getCountFloors() && status == ElevatorStatus.MOVING_UP){
            currentFloor++;
            direction = Direction.UP;
            System.out.println("лифт " + id + " на " + currentFloor + " этаже");
        }
        else if(currentFloor > 1 && status == ElevatorStatus.MOVING_DOWN){
            currentFloor--;
            direction = Direction.DOWN;
            System.out.println("лифт на " + currentFloor + " этаже");
        }
    }

    public void stop(){
        this.status = ElevatorStatus.STOPPED;
        this.direction = Direction.NONE;
        System.out.println("Лифт " + id + " приехал на " + currentFloor + " этаж");
        openDoors();
        try {
            lock.wait(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        List<Command> removeCommand = new ArrayList<>();
        for(Command itCommand : commands){
            if(itCommand.getTargetFloor() == currentFloor){
                disembarkingPeople(itCommand);
                removeCommand.add(itCommand);
            }
        }
        commands.removeAll(removeCommand);
        List<Request> usefulRequest = new ArrayList<>();
        for(Request itRequest : requests){
            if(itRequest.getCurrentFloor() == currentFloor && countPeople < 8){
                usefulRequest.add(itRequest);
            }
        }

        for(Request itRequest : usefulRequest) {
            int count = countPeople;
            boardingPeople(itRequest);
            waitingCommand = true;
            while (command == null) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            if(itRequest.getCountPeople() + count > 8) {
                commands.add(new Command(command.getDirection(), command.getTargetFloor(), 8 - count));
            }
            else if(itRequest.getCountPeople() + count <= 8){
                commands.add(new Command(command.getDirection(), command.getTargetFloor(), itRequest.getCountPeople()));
            }
            System.out.println("Команда добавлена в список команд");
            command = null;
            waitingCommand = false;
            requests.remove(itRequest);
        }
        goalFloors.remove(currentFloor);
        closeDoors();
    }

    public void openDoors(){
        this.status = ElevatorStatus.OPEN_DOORS;
        System.out.println("Двери открыты");
    }

    public void closeDoors(){
        this.status = ElevatorStatus.CLOSE_DOORS;
        System.out.println("Двери закрыты");
    }

    public void boardingPeople(Request request){
        if(request.getCountPeople() + countPeople <= 8) {
            countPeople += request.getCountPeople();
            System.out.println("В лифт зашло " + request.getCountPeople() + " человек");
        }
        else if(countPeople == 8){
            Request additionalRequest = new Request(request.getCurrentFloor(), request.getCountPeople());
            dispatcher.addRequest(additionalRequest);
            dispatcher.sendRequest();
        }
        else if(request.getCountPeople() + countPeople > 8){
            System.out.println("В лифт зашло " + (8 - countPeople) + " человек");
            Request additionalRequest = new Request(request.getCurrentFloor(), request.getCountPeople() - 8 + countPeople);
            dispatcher.addRequest(additionalRequest);
            dispatcher.sendRequest();
            countPeople = 8;
        }
    }

    public void disembarkingPeople(Command command){
        countPeople -= command.getCountPeople();
        System.out.println("Из лифта вышло " + command.getCountPeople() + " человек");
    }

    public boolean addCommand(Direction direction, int targetFloor) {
        synchronized(lock) {
            if(waitingCommand) {
                command = new Command(direction, targetFloor, 0);
                this.goalFloors.add(targetFloor);
                lock.notifyAll();
                return true;
            }
            return false;
        }
    }

    public boolean isWaitingCommand(){
        synchronized (lock){
            return waitingCommand;
        }
    }

    public void setDispatcher(Dispatcher dispatcher){
        this.dispatcher = dispatcher;
    }
    public void setStatus(ElevatorStatus status) {
        this.status = status;
    }

    public void addRequest(Request request) {
        synchronized (lock) {
            this.requests.add(request);
            this.goalFloors.add(request.getCurrentFloor());
        }
    }

    public int getId(){
        return id;
    }

    public int getCurrentFloor(){
        return currentFloor;
    }

    public Direction getDirection(){
        return direction;
    }

    public ElevatorStatus getStatus() {
        return status;
    }

    public int getCountPeople() {
        return countPeople;
    }
}
