import java.lang.Thread;
import java.util.List;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Dispatcher dispatcher = new Dispatcher(10, 2);
        List<Elevator> elevators = dispatcher.getElevators();

        System.out.println("=== СИСТЕМА УПРАВЛЕНИЯ ЛИФТАМИ ===\n");

        for(Elevator elevator : elevators) {
            elevator.setDispatcher(dispatcher);
            new Thread(elevator).start();
            System.out.println("Запущен лифт #" + elevator.getId());
        }
        Thread.sleep(5000);
        Thread dispatcherTread = new Thread(dispatcher);
        dispatcherTread.start();
        Thread.sleep(5000);

        System.out.println("\n=== СОЗДАЕМ ЗАПРОСЫ ===");
        Request[] requests = {
                new Request(5, 3),
                new Request(3, 4),
                new Request(2, 2),
                new Request(8,5)
        };

        for(Request request : requests) {
            System.out.println("Добавлен запрос: этаж " +
                    request.getCurrentFloor() + " → ?");
            dispatcher.addRequest(request);
            dispatcher.sendRequest();
            Thread.sleep(1000);
        }
        dispatcher.setAllRequestsProcessed(true);
        System.out.println("\n=== СИСТЕМА ЗАПУЩЕНА ===");
        System.out.println("Центр управления будет запрашивать направления\n");
        dispatcherTread.join();
    }
}
