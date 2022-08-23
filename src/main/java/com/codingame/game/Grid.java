package com.codingame.game;

import com.codingame.game.action.ActionType;
import com.codingame.gameengine.module.entities.*;
import com.google.inject.Inject;

import com.codingame.game.action.Action;

public class Grid {
    @Inject
    private GraphicEntityModule graphicEntityModule;

    private String[] images = { "cross.png", "circle.png" };

    private final int[] dr = {-1, -1, 0, 1, 1, 1, 0, -1};
    private final int[] dc = {0, -1, -1, -1, 0, 1, 1, 1};

    private Group entity;

    private int origX;
    private int origY;
    private int cellSize;
    private int[][] grid = new int[Config.GRIDSIZE][Config.GRIDSIZE];
    protected int winner = 0;

    public Grid() {
        init();
    }

    public void init() {
        for (int i = 0; i < Config.GRIDSIZE; i++) {
            for (int j = 0; j < Config.GRIDSIZE; j++) {
                grid[i][j] = 0;
            }
        }
        // Initial player positions
        grid[1][1] = 1;
        grid[1][5] = 2;
        grid[5][1] = 2;
        grid[5][5] = 1;

        // Blocked cells;
        grid[2][2] = -1;
        grid[2][4] = -1;
        grid[4][2] = -1;
        grid[4][4] = -1;
    }

    public int[][] getGridState() {
        if(winner == 0)
            return grid;
        return null;
    }

    public int play(Action action) throws InvalidAction {
        Cell src = new Cell(action.srcRow, action. srcCol);
        Cell dest = new Cell(action.destRow, action.destCol);
        ActionType actionType = action.actionType;
        int index = action.player.getIndex() + 1;

        if (!src.isValid()) {
            throw new InvalidAction("Source position is outside of the grid");
        }
        else if (!dest.isValid()) {
            throw new InvalidAction("Destination position is outside of the grid");
        }
        else if (grid[dest.r][dest.c] == -1) {
            throw new InvalidAction("Destination position is blocked");
        }
        else if (grid[action.srcCol][action.srcRow] != index) {
            throw new InvalidAction("Source position does not contain player's piece");
        }
        else if (grid[action.destRow][action.destCol] != 0) {
            throw new InvalidAction("Destination position is not empty");
        }
        else if (actionType != ActionType.REPL && actionType != ActionType.JUMP) {
            throw new InvalidAction("Invalid command");
        }
        else if (actionType == ActionType.REPL && !src.isDiagonal(dest)) {
            throw new InvalidAction("Invalid move");
        }
        else if (actionType == ActionType.JUMP && !src.isAdjacent(dest)) {
            throw new InvalidAction("Invalid move");
        }

        // update grid
        if (actionType == ActionType.JUMP) {
            grid[action.srcRow][action.srcCol] = 0;
        }
        grid[action.destRow][action.destCol] = index;
        for (int i = 0; i < 4; i++) {
            int r = dest.r + dr[2*i];
            int c = dest.c + dc[2*i];
            if (new Cell(r, c).isValid()) {
                int index2 = grid[r][c];
                if (index2 > 0 && index2 != index) {
                    grid[r][c] = index;
                }
            }
        }

        winner = checkWinner();
        drawPlay(action);
        return winner;
    }

    private int checkWinner() {
        if (!checkTermination())
            return 0;
        int counter1 = 0, counter2 = 0;
        for (int i = 0; i < Config.GRIDSIZE; i++) {
            for (int j = 0; j < Config.GRIDSIZE; j++) {
                if (grid[i][j] == 1)
                    counter1++;
                else if (grid[i][j] == 2)
                    counter2++;
            }
        }
        if (counter1 > counter2)
            return 1;
        return 2;
    }

    private boolean checkTermination() {
        int counter1 = 0;
        int counter2 = 0;
        for (int i = 0; i < Config.GRIDSIZE; i++) {
            for (int j = 0; j < Config.GRIDSIZE; j++) {
                for (int dir = 0; dir < 8; dir++) {
                    int r = i + dr[dir];
                    int c = i + dc[dir];
                    if (new Cell(r, c).isValid() && grid[i][j] > 0) {
                        if (grid[r][c] == 0 && grid[i][j] == 1) {
                            counter1++;
                        }
                        else {
                            counter2++;
                        }
                    }
                }
            }
        }
        // Fill remaining empty cells with player 2
        if (counter1 == 0) {
            for (int i = 0; i < Config.GRIDSIZE; i++)
                for (int j = 0; j < Config.GRIDSIZE; j++)
                    if (grid[i][j] == 0)
                        grid[i][j] = 2;
        }
        // Fill remaining empty cells with player 1
        else if (counter2 == 0) {
            for (int i = 0; i < Config.GRIDSIZE; i++)
                for (int j = 0; j < Config.GRIDSIZE; j++)
                    if (grid[i][j] == 0)
                        grid[i][j] = 1;
        }
        return counter1 == 0 || counter2 == 0;
    }
    public void draw(int origX, int origY, int cellSize, int lineWidth, int lineColor) {
        this.origX = origX;
        this.origY = origY;
        this.cellSize = cellSize;
        this.entity = graphicEntityModule.createGroup();

        double xs[] = new double[] { 0, 0, 1, 2 };
        double x2s[] = new double[] { 2, 2, 0, 1 };
        double ys[] = new double[] { 1, 2, 0, 0 };
        double y2s[] = new double[] { 0, 1, 2, 2 };

        for (int i = 0; i < 4; ++i) {
            Line line = graphicEntityModule.createLine()
                    .setX(convert(origX, cellSize, xs[i] - 0.5))
                    .setX2(convert(origX, cellSize, x2s[i] + 0.5))
                    .setY(convert(origY, cellSize, ys[i] - 0.5))
                    .setY2(convert(origY, cellSize, y2s[i] + 0.5))
                    .setLineWidth(lineWidth)
                    .setLineColor(lineColor);
            entity.add(line);
        }
    }

    // TODO animate for move command
    public void drawPlay(Action action) {
        Sprite avatar = graphicEntityModule.createSprite()
                .setX(convert(origX, cellSize, action.destCol))
                .setY(convert(origY, cellSize, action.destRow))
                .setImage(images[action.player.getIndex()])
                .setBaseWidth((int) (0.8 * cellSize))
                .setBaseHeight((int) (0.8 * cellSize))
                .setTint(action.player.getColorToken())
                .setAnchor(0.5);

        // Animate arrival
        avatar.setScale(0);
        graphicEntityModule.commitEntityState(0.2, avatar);
        avatar.setScale(1, Curve.ELASTIC);
        graphicEntityModule.commitEntityState(1, avatar);

        this.entity.add(avatar);
    }

    private int convert(int orig, int cellSize, double unit) {
        return (int) (orig + unit * cellSize);
    }

    public void hide() {
        this.entity.setAlpha(0);
        this.entity.setVisible(false);
    }

    public void activate() {
        this.entity.setAlpha(1, Curve.NONE);
        graphicEntityModule.commitEntityState(1, entity);
    }

    public void deactivate() {
        if (winner == 0) {
            this.entity.setAlpha(0.5, Curve.NONE);
            graphicEntityModule.commitEntityState(1, entity);
        }
    }
}
