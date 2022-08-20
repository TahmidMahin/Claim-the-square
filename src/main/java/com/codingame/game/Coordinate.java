package com.codingame.game;

public class Coordinate {
    // 1, 3, 5, 7 are diagonals
    private final int[] dr = {-1, -1, 0, 1, 1, 1, 0, -1};
    private final int[] dc = {0, -1, -1, -1, 0, 1, 1, 1};
    public final int r;
    public final int c;

    public Coordinate(int r, int c) {
        this.r = r;
        this.c = c;
    }

    public boolean equals(Coordinate p) {
        return r == p.r && c == p.c;
    }

    public boolean isValid() {
        return (r >= 0 && r < Config.GRIDSIZE && c >= 0 && c < Config.GRIDSIZE);
    }

    public boolean isDiagonal(Coordinate p) {
        if (!p.isValid())
            return false;
        for (int i = 0; i < 4; i++) {
            if (p.equals(new Coordinate(r + dr[2*i + 1], c + dc[2*i + 1])))
                return true;
        }
        return false;
    }

    public boolean isAdjacent(Coordinate p) {
        if (!p.isValid())
            return false;
        for (int i = 0; i < 4; i++) {
            if (p.equals(new Coordinate(r + dr[2*i], c + dc[2*i])))
                return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return r + " " + c;
    }
}
