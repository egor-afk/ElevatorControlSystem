import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class Dispatcher implements Runnable{

    private final int countFloors;
    private final int countElevators;
    private final List<Elevator> elevators = new ArrayList<>();
    private final List<Request> requests = new ArrayList<>();
    Scanner scan = new Scanner(System.in);

    public Dispatcher(int countFloors, int countElevators){
        this.countFloors = countFloors;
        this.countElevators = countElevators;
        for(int i = 0; i < countElevators; i++){
            elevators.add(new Elevator(i+1));
        }
    }

    public void run(){
        while(true){
            boolean foundWaiting = false;
            for(Elevator elevator : elevators){
                if(elevator.isWaitingCommand()){
                    foundWaiting = true;
                    System.out.println("Лифт " + elevator.getId() + " ждет команду");
                    System.out.println("Укажите направление для лифта " + elevator.getId());
                    String direction = getString(scan);
                    System.out.println("Укажите этаж для лифта " + elevator.getId());
                    int floor = getInt(scan);
                    scan.nextLine();
                    Direction direction1 = null;
                    if (direction.equals("Up")) {
                        direction1 = Direction.UP;
                    } else if (direction.equals("Down")) {
                        direction1 = Direction.DOWN;
                    }
                    if(elevator.addCommand(direction1, floor)){
                        System.out.println("Команда лифту " + elevator.getId() + " успешно передана");
                    } else{
                        System.out.println("Лифт " + elevator.getId() + " больше не ждет команду");
                    }
                }
            }

            if(!foundWaiting){
                System.out.println("Нет лифтов ждущих команду");
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    break;
                }
            }

        }
    }

    public void sendRequest(){
        Request request = requests.removeFirst();
        Elevator bestElevator = selectElevator(request, elevators);
        if(bestElevator != null){
            bestElevator.addRequest(request);
            System.out.println("Запрос отдан лифту " + bestElevator.getId());
        }
        else{
            System.out.println("Все лифты переполнены или пока нет подходящего лифта");
            requests.add(request);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            sendRequest();
        }
    }

    public void addRequest(Request request) {
        this.requests.add(request);
    }

    public Elevator selectElevator(Request request, List<Elevator> elevators){
        Elevator bestElevator = null;
        float minValue = 1000;
        for(Elevator elevator : elevators){
            if(elevator.getCountPeople() < 8) {
                float value = calculateValue(request, elevator);
                if (value < minValue) {
                    minValue = value;
                    bestElevator = elevator;
                }
            }
        }
        return bestElevator;
    }

    public float calculateValue(Request request, Elevator elevator){
        if(elevator.getStatus() == ElevatorStatus.STOPPED || elevator.getStatus() == ElevatorStatus.CLOSE_DOORS || elevator.getStatus() == ElevatorStatus.OPEN_DOORS){
            return Math.abs(elevator.getCurrentFloor() - request.getCurrentFloor());
        }
        if((elevator.getDirection() == Direction.UP && elevator.getCurrentFloor() < request.getCurrentFloor()) ||
                (elevator.getDirection() == Direction.DOWN && elevator.getCurrentFloor() > request.getCurrentFloor())){
            return (float) ((Math.abs((elevator.getCurrentFloor() - request.getCurrentFloor()))) * 0.5);
        }
        if((elevator.getDirection() == Direction.UP && elevator.getCurrentFloor() > request.getCurrentFloor()) ||
                (elevator.getDirection() == Direction.DOWN && elevator.getCurrentFloor() < request.getCurrentFloor())){
            return (elevator.getCurrentFloor() + request.getCurrentFloor()) - 2;
        }
        return 2000;
    }

    private int getInt(Scanner scan){
        while(true) {
            try {
                int value = scan.nextInt();
                if(value < 1 || value > countFloors){
                    System.out.println("Номер этажа должет быть от 1 до " + countFloors);
                    continue;
                }
                return value;
            } catch (InputMismatchException e) {
                System.out.println("Ошибка: введите номер этажа");
                scan.nextLine();
            }
        }
    }

    private String getString(Scanner scan){
        while(true) {
            try {
                String str = scan.nextLine();
                if(str.isEmpty()){
                    System.out.println("Строка не может быть пустой");
                    continue;
                }
                if(!str.equals("Up") && !str.equals("Down")){
                    System.out.println("Направление должно быть Up или Down");
                    continue;
                }
                return str;
            } catch (InputMismatchException e) {
                System.out.println("Ошибка: введите направление для лифта");
                scan.nextLine();
            }
        }
    }
    public int getCountFloors(){
        return countFloors;
    }
    public List<Elevator> getElevators(){
        return elevators;
    }
}
