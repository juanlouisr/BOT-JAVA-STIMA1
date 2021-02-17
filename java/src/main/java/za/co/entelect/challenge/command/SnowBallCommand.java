package za.co.entelect.challenge.command;

import za.co.entelect.challenge.enums.Direction;

public class SnowBallCommand implements Command {

    private final int x;
    private final int y;

    public SnowBallCommand(int x, int y) { this.x = x; this.y = y;}


    @Override
    public String render() {
        return String.format("snowball %d %d", x, y);
    }

//    @Override
//    public String toString() {
//        return "snowball " + x + " " + y;
//    }

}
