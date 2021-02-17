package za.co.entelect.challenge.entities;

import com.google.gson.annotations.SerializedName;

public class Occupier {
    @SerializedName("playerId")
    public int playerId;

    @SerializedName("id")
    public int id;

    @SerializedName("health")
    public int health;

    @SerializedName("roundsUntilUnfrozen")
    public int roundsUntilUnfrozen;

}
