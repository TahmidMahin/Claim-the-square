package com.codingame.game;

import com.codingame.game.action.ActionType;
import com.codingame.gameengine.module.entities.*;
import com.google.inject.Inject;

import com.codingame.game.action.Action;

public class Grid {
    @Inject
    private GraphicEntityModule graphicEntityModule;
    private Sprite[][] spriteMap = new Sprite[Config.GRIDSIZE][Config.GRIDSIZE];

    private String[] images = { "pieces.png", "circle.png" };

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
        return grid;
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
        else if (grid[action.srcRow][action.srcCol] != index) {
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
                    if (grid[i][j] == 0) {
                        grid[i][j] = 2;
                        spriteMap[i][j] = drawPiece(i, j, 1);
                    }
        }
        // Fill remaining empty cells with player 1
        else if (counter2 == 0) {
            for (int i = 0; i < Config.GRIDSIZE; i++)
                for (int j = 0; j < Config.GRIDSIZE; j++)
                    if (grid[i][j] == 0) {
                        grid[i][j] = 1;
                        spriteMap[i][j] = drawPiece(i, j, 0);
                    }

        }
        return counter1 == 0 || counter2 == 0;
    }

    public void draw(int origX, int origY, int cellSize, int lineWidth, int lineColor) {
        this.origX = origX;
        this.origY = origY;
        this.cellSize = cellSize;
        this.entity = graphicEntityModule.createGroup();
    }

    public Sprite drawPiece(int row, int col, int playerIndex)
    {
        int color = 0xFFFFFF;
        if(playerIndex == 0)
        {
            color = 0x88E079;
        }
        else
        {
            color = 0xff9d5c;
        }
        System.err.println("DRAWING");
        Sprite avatar = graphicEntityModule.createSprite()
                .setX(convert(origX, cellSize, row))
                .setY(convert(origY, cellSize, col))
                .setImage(images[1])
                .setBaseWidth((int) (0.8 * cellSize))
                .setBaseHeight((int) (0.8 * cellSize))
                .setTint(color)
                .setAnchor(0.5);
        return avatar;
    }

    public void drawPlay(Action action) {

        if(action.actionType == ActionType.REPL)
        {
            Sprite avatar = graphicEntityModule.createSprite()
                    .setX(convert(origX, cellSize, action.destCol))
                    .setY(convert(origY, cellSize, action.destRow))
                    .setImage(images[1])
                    .setBaseWidth((int) (0.8 * cellSize))
                    .setBaseHeight((int) (0.8 * cellSize))
                    .setTint(action.player.getColorToken())
                    .setAnchor(0.5);
            spriteMap[action.destRow][action.destCol] = avatar;


            // Animate arrival
            avatar.setScale(0);
            graphicEntityModule.commitEntityState(0.2, avatar);
            avatar.setScale(1, Curve.ELASTIC);
            graphicEntityModule.commitEntityState(1, avatar);
            this.entity.add(avatar);
            for (int i = 0; i < 4; i++) {
                int r = action.destRow + dr[2*i];
                int c = action.destCol + dc[2*i];
                if (r>=0  && r< Config.GRIDSIZE && c>=0 && c< Config.GRIDSIZE && spriteMap[r][c] != null)
                {
                    spriteMap[r][c].setTint(action.player.getColorToken());
                }
            }
        }
        else if(action.actionType == ActionType.JUMP)
        {
            if(spriteMap[action.srcRow][action.srcCol] == null)
            {
                System.err.println("IS NULL");
                Sprite avatar = graphicEntityModule.createSprite()
                        .setX(convert(origX, cellSize, action.destCol))
                        .setY(convert(origY, cellSize, action.destRow))
                        .setImage(images[1])
                        .setBaseWidth((int) (0.8 * cellSize))
                        .setBaseHeight((int) (0.8 * cellSize))
                        .setTint(action.player.getColorToken())
                        .setAnchor(0.5);
                spriteMap[action.destRow][action.destCol] = avatar;
                spriteMap[action.srcRow][action.srcCol] = null;

                // Animate arrival
                avatar.setScale(0);
                graphicEntityModule.commitEntityState(0.2, avatar);
                avatar.setScale(1, Curve.ELASTIC);
                graphicEntityModule.commitEntityState(1, avatar);
                this.entity.add(avatar);
                for (int i = 0; i < 4; i++) {
                    int r = action.destRow + dr[2*i];
                    int c = action.destCol + dc[2*i];
                    if (spriteMap[r][c] != null)
                    {
                        spriteMap[r][c].setTint(action.player.getColorToken());
                    }
                }
//                this.entity.add(avatar);
            }
            else {
                Sprite avatar = spriteMap[action.srcRow][action.srcCol];
                spriteMap[action.destRow][action.destCol] = avatar;
                spriteMap[action.srcRow][action.srcCol] = null;
                avatar.setX(convert(origX, cellSize, action.destCol))
                        .setY(convert(origY, cellSize, action.destRow));

                for (int i = 0; i < 4; i++) {
                    int r = action.destRow + dr[2*i];
                    int c = action.destCol + dc[2*i];
                    if (r>=0  && r< Config.GRIDSIZE && c>=0 && c< Config.GRIDSIZE && spriteMap[r][c] != null)
                    {
                        spriteMap[r][c].setTint(action.player.getColorToken());
                    }
                }
//                this.entity.add(avatar);
            }
        }

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

    public void print()
    {
        for(int i=0; i<Config.GRIDSIZE; i++)
        {
            for (int j=0; j<Config.GRIDSIZE; j++)
            {
                System.err.print(grid[i][j]);
            }
            System.err.println("");
        }
    }
}
