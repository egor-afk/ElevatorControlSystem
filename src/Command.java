public class Command {
    private final Direction direction;
    private final int targetFloor;
    private final int countPeople;

    public Command(Direction direction, int targetFloor, int countPeople){
        this.direction = direction;
        this.targetFloor = targetFloor;
        this.countPeople = countPeople;
    }

    public Direction getDirection() {
        return direction;
    }

    public int getTargetFloor(){
        return targetFloor;
    }

    public int getCountPeople() {
        return countPeople;
    }
}
