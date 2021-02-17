package za.co.entelect.challenge.entities;

public class CellWithDistance implements Comparable<CellWithDistance> {
    public Cell cell;
    public int jarak;

    public CellWithDistance(Cell cell, int jarak){
        this.cell = cell;
        this.jarak = jarak;
    }
    public int compareTo(CellWithDistance compareCell){
        return this.jarak -compareCell.jarak;
    }

}
