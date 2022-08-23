package com.codingame.game;

public class Cell {
    // 1, 3, 5, 7 are diagonals
    private final int[] dr = {-1, -1, 0, 1, 1, 1, 0, -1};
    private final int[] dc = {0, -1, -1, -1, 0, 1, 1, 1};
    public final int r;
    public final int c;

    public int state = 0;

    public Cell(int r, int c) {
        this.r = r;
        this.c = c;
    }

    public Cell(int r, int c, int state) {
        this.r = r;
        this.c = c;
        this.state = state;
    }

    public boolean equals(Cell p) {
        return r == p.r && c == p.c;
    }

    public boolean isValid() {
        return (r >= 0 && r < Config.GRIDSIZE && c >= 0 && c < Config.GRIDSIZE);
    }

    public boolean isDiagonal(Cell p) {
        if (!p.isValid())
            return false;
        for (int i = 0; i < 4; i++) {
            if (p.equals(new Cell(r + dr[2*i + 1], c + dc[2*i + 1])))
                return true;
        }
        return false;
    }

    public boolean isAdjacent(Cell p) {
        if (!p.isValid())
            return false;
        for (int i = 0; i < 4; i++) {
            if (p.equals(new Cell(r + dr[2*i], c + dc[2*i])))
                return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return r + " " + c + " " + state;
    }
}
