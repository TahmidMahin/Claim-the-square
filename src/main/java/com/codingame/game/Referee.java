package com.codingame.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

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
    private List<Action> validActions;
    private List<CellState> cellStates;
    private Random random;

    // TODO import these parameters from CONFIG
    @Override
    public void init() {
        random = new Random(gameManager.getSeed());

        drawBackground();
        drawHud();
        drawGrids();

        gameManager.setFrameDuration(600);

        if (gameManager.getLeagueLevel() == 1) {
            gameManager.setMaxTurns(9);
        } else {
            gameManager.setMaxTurns(9 * 9);
        }
        cellStates = getCellStates();
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

    //TODO import parameter from CONFIG
    private void drawGrids() {
        int bigCellSize = 240;
        int bigOrigX = (int) Math.round(1920 / 2 - bigCellSize);
        int bigOrigY = (int) Math.round(1080 / 2 - bigCellSize);
        grid = gridProvider.get();
        grid.draw(bigOrigX, bigOrigY, bigCellSize, 5, 0xf9b700);

        graphicEntityModule
            .createSprite()
            .setImage("board_border.png")
            .setX(1920 / 2)
            .setY(1080 / 2)
            .setAnchor(0.5);
    }
    // TODO import these parameters from CONFIG
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

    private void sendInputs(Player player, List<CellState> cellStates) {
        player.sendInputLine((Integer.toString(cellStates.size())));
        for (CellState cellState : cellStates) {
            player.sendInputLine(cellState.toString());
        }
    }

    private void setWinner(Player player) {
        gameManager.addToGameSummary(GameManager.formatSuccessMessage(player.getNicknameToken() + " won!"));
        //TODO implement score calculation
        player.setScore(10);
        endGame();
    }

    private List<CellState> getCellStates() {
        List<CellState> cellStates = new ArrayList<>();
        int[][] gridState = grid.getGridState();
        for (int i = 0; i < Config.GRIDSIZE; i++) {
            for (int j = 0; j < Config.GRIDSIZE; j++) {
                cellStates.add(new CellState(new Coordinate(i, j), gridState[i][j]));
            }
        }
        return cellStates;
    }

    @Override
    public void gameTurn(int turn) {
        Player player = gameManager.getPlayer(turn % gameManager.getPlayerCount());
        sendInputs(player, cellStates);
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

            cellStates = getCellStates();
            if (cellStates.isEmpty()) {
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
