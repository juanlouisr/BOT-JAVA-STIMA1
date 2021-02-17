package za.co.entelect.challenge.entities;

import com.google.gson.annotations.SerializedName;

import za.co.entelect.challenge.enums.Profession;

public class MyWorm extends Worm implements Comparable<MyWorm>{
    @SerializedName("weapon")
    public Weapon weapon;

    @SerializedName("profession")
    public Profession profession;

    @SerializedName("bananaBombs")
    public BananaBombs bananaBombs;

    @SerializedName("snowballs")
    public Snowballs snowballs;

    public int jarak;

    public boolean dalambahaya = false;

    public boolean canBananaBomb(){
        if (bananaBombs != null){
            return bananaBombs.count > 0;
        }
        return false;
    }

    public boolean canSnowBalls(){
        if (snowballs != null){
            return snowballs.count > 0;
        }
        return false;
    }




    @Override
    public int compareTo(MyWorm o) {
        return this.jarak - o.jarak;
    }
}
