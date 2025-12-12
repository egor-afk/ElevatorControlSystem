public class Request {
    private final int currentFloor;
    private final int countPeople;

    public Request(int currentFloor, int countPeople){
        this.currentFloor = currentFloor;
        this.countPeople = countPeople;
    }

    public int getCurrentFloor(){
        return currentFloor;
    }

    public int getCountPeople() {
        return countPeople;
    }
}
