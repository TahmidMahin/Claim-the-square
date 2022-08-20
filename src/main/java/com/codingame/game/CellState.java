package com.codingame.game;

public class CellState {

    private Coordinate p;
    private int state;

    public CellState(Coordinate p, int state) {
        this.p = p;
        this.state = state;
    }

    @Override
    public String toString() {
        return p.toString() + " " + Integer.toString(state);
    }
}
