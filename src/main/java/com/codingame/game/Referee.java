package com.codingame.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.codingame.game.action.ActionType;
import com.codingame.gameengine.core.AbstractPlayer.TimeoutException;
import com.codingame.gameengine.core.AbstractReferee;
import com.codingame.gameengine.core.GameManager;
import com.codingame.gameengine.core.MultiplayerGameManager;
import com.codingame.gameengine.module.entities.GraphicEntityModule;
import com.codingame.gameengine.module.entities.Sprite;
import com.codingame.gameengine.module.entities.Text;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.codingame.game.action.Action;

public class Referee extends AbstractReferee {
    @Inject private MultiplayerGameManager<Player> gameManager;
    @Inject private GraphicEntityModule graphicEntityModule;
    @Inject private Provider<Grid> gridProvider;

    private Grid grid;
    private List<Cell> cells;
    private Random random;

    // TODO import these parameters from CONFIG
    @Override
    public void init() {
        random = new Random(gameManager.getSeed());

        drawBackground();
        drawHud();
        drawGrids();
        drawPieces();

        gameManager.setFrameDuration(600);
        gameManager.setMaxTurns(300);

        cells = getCells();
        sendInitialData();
    }

    private void drawPieces() {
    }

    void sendInitialData() {
        for(Player player: gameManager.getPlayers()) {
            player.sendInputLine(Integer.toString(player.getIndex() + 1));
        }
    }

    private void drawBackground() {
        graphicEntityModule.createSprite()
                .setImage("Background.jpg")
                .setAnchor(0);
        graphicEntityModule.createSprite()
                .setImage("logo.png")
                .setX(280)
                .setY(915)
                .setAnchor(0.5);
        graphicEntityModule.createSprite()
                .setImage("logoCG.png")
                .setX(1920 - 280)
                .setY(915)
                .setAnchor(0.5);
    }

    //TODO import parameters from CONFIG
    private void drawGrids() {
        int bigCellSize = 100;
        int bigOrigX = (int) Math.round(1920/4 + 178);
        int bigOrigY = (int) Math.round(1080/4 - 20);
        grid = gridProvider.get();
//        grid.draw(bigOrigX, bigOrigY, bigCellSize, 5, 0xf9b700);

        graphicEntityModule
            .createSprite()
            .setImage("board_border.png")
            .setX(1920 / 2)
            .setY(1080 / 2)
            .setAnchor(0.5);
        grid.draw(bigOrigX, bigOrigY, bigCellSize, 5, 0xf9b700);
        grid.drawPlay(new Action(gameManager.getPlayer(0),
                1, 1, "REPL", 1, 1));
        grid.drawPlay(new Action(gameManager.getPlayer(0),
                5, 5, "REPL", 5, 5));
        grid.drawPlay(new Action(gameManager.getPlayer(1),
                5, 1, "REPL", 1, 5));
        grid.drawPlay(new Action(gameManager.getPlayer(1),
                5, 1, "REPL", 5, 1));
    }
    // TODO import parameters from CONFIG
    private void drawHud() {
        for (Player player : gameManager.getPlayers()) {
            int x = player.getIndex() == 0 ? 280 : 1920 - 280;
            int y = 220;

            graphicEntityModule
                    .createRectangle()
                    .setWidth(140)
                    .setHeight(140)
                    .setX(x - 70)
                    .setY(y - 70)
                    .setLineWidth(0)
                    .setFillColor(player.getColorToken());

            graphicEntityModule
                    .createRectangle()
                    .setWidth(120)
                    .setHeight(120)
                    .setX(x - 60)
                    .setY(y - 60)
                    .setLineWidth(0)
                    .setFillColor(0xffffff);

            Text text = graphicEntityModule.createText(player.getNicknameToken())
                    .setX(x)
                    .setY(y + 120)
                    .setZIndex(20)
                    .setFontSize(40)
                    .setFillColor(0xffffff)
                    .setAnchor(0.5);

            Sprite avatar = graphicEntityModule.createSprite()
                    .setX(x)
                    .setY(y)
                    .setZIndex(20)
                    .setImage(player.getAvatarToken())
                    .setAnchor(0.5)
                    .setBaseHeight(116)
                    .setBaseWidth(116);

            player.hud = graphicEntityModule.createGroup(text, avatar);
        }
    }

    private void sendInputs(Player player, List<Cell> cells) {
        player.sendInputLine((Integer.toString(cells.size())));
        for (Cell cell : cells) {
            player.sendInputLine(cell.toString());
        }
    }

    private void setWinner(Player player) {
        gameManager.addToGameSummary(GameManager.formatSuccessMessage(player.getNicknameToken() + " won!"));
        int score = 0;
        for (int i = 0; i < Config.GRIDSIZE; i++) {
            for (int j = 0; j < Config.GRIDSIZE; j++) {
                if (grid.getGridState()[i][j] == player.getIndex() + 1)
                    score++;
            }
        }
        player.setScore(score);
        endGame();
    }

    private List<Cell> getCells() {
        List<Cell> cells = new ArrayList<>();
        int[][] gridState = grid.getGridState();
        for (int i = 0; i < Config.GRIDSIZE; i++) {
            for (int j = 0; j < Config.GRIDSIZE; j++) {
                cells.add(new Cell(i, j, gridState[i][j]));
            }
        }
        return cells;
    }

    @Override
    public void gameTurn(int turn) {
        Player player = gameManager.getPlayer(turn % gameManager.getPlayerCount());
        sendInputs(player, cells);
        player.execute();

        // Read inputs
        try {
            final Action action = player.getAction();
            gameManager.addToGameSummary(String.format("Player %s played (%d %d %s %d %d)",
                    action.player.getNicknameToken(), action.srcRow, action.srcCol, action.actionType, action.destRow, action.destCol));
            int winner = grid.play(action);
            if (winner != 0) {
                setWinner(player);
            }

            cells = getCells();
            if (cells.isEmpty()) {
                endGame();
            }
        } catch (NumberFormatException e) {
            player.deactivate("Wrong output!");
            player.setScore(-1);
            endGame();
        } catch (TimeoutException e) {
            gameManager.addToGameSummary(GameManager.formatErrorMessage(player.getNicknameToken() + " timeout!"));
            player.deactivate(player.getNicknameToken() + " timeout!");
            player.setScore(-1);
            endGame();
        } catch (InvalidAction e) {
            player.deactivate(e.getMessage());
            player.setScore(-1);
            endGame();
        }
    }

    private void endGame() {
        gameManager.endGame();

        Player p0 = gameManager.getPlayers().get(0);
        Player p1 = gameManager.getPlayers().get(1);
        if (p0.getScore() > p1.getScore()) {
            p1.hud.setAlpha(0.3);
        }
        if (p0.getScore() < p1.getScore()) {
            p0.hud.setAlpha(0.3);
        }
    }
}
