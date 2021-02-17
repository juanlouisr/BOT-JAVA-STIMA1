package za.co.entelect.challenge.enums;

public enum Kuarta {

    K1(1, 1),
    K2(-1, 1),
    K3(-1, -1),
    K4(1, -1);


    public final int x;
    public final int y;

    Kuarta(int x, int y) {
        if (x >= 0 && x <=17)
            this.x = -1;
        else
            this.x = 1;
        if (y >= 0 && y <= 17)
            this.y = 1;
        else
            this.y = -1;
    }
}


