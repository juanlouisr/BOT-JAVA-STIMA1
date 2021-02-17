package za.co.entelect.challenge.entities;

public class CellTerjauhDari3Musuh implements Comparable<CellTerjauhDari3Musuh> {
    public Cell cell;
    public int jarak1;
    public int jarak2;
    public int jarak3;


    public CellTerjauhDari3Musuh(Cell cell, int jarak1, int jarak2, int jarak3){
        this.cell = cell;
        this.jarak1 = jarak1;
        this.jarak2 = jarak2;
        this.jarak3 = jarak3;
    }

    @Override
    public int compareTo(CellTerjauhDari3Musuh compareCell) {
        return (compareCell.jarak1+compareCell.jarak2+compareCell.jarak3) - (this.jarak1+this.jarak2+this.jarak3);
    }
}
