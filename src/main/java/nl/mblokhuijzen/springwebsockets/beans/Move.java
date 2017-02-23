package nl.mblokhuijzen.springwebsockets.beans;

import java.io.Serializable;

public class Move implements Serializable {
    private int player;
    private int position;

    public Move() {

    }

    public Move(int player, int position) {
        this.player = player;
        this.position = position;
    }

    public int getPlayer() {
        return player;
    }

    public void setPlayer(int player) {
        this.player = player;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
