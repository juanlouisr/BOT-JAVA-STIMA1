package za.co.entelect.challenge.command;


public class BananaBombCommand implements Command {

    private int x;
    private int y;

    public BananaBombCommand(int x, int y) { this.x = x; this.y = y;}

    @Override
    public String render() {
        return String.format("banana %d %d", x, y);
    }

//    @Override
//    public String toString() {
//        return "banana " + x + " " + y;
//    }
}
