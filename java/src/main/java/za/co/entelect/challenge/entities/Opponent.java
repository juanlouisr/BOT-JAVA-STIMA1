package za.co.entelect.challenge.entities;

import com.google.gson.annotations.SerializedName;

public class Opponent {
    @SerializedName("id")
    public int id;

    @SerializedName("currentWormId")
    public int currId;

    @SerializedName("score")
    public int score;

    @SerializedName("worms")
    public Worm[] worms;

    @SerializedName("previousCommand")
    public String previousCommand;

    public boolean isShoting(){
        return previousCommand.contains("shoot");
    }

//    public int whoIsShooting(){
//        if (isShoting()){
//            if (currId
//        }
//        return 0;
//    }
}
