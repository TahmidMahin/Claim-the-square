package com.codingame.game.action;

import com.codingame.game.Player;

public class Action {
    public Player player;
    public final int srcRow;
    public final int srcCol;
    public final ActionType actionType;
    public final int destRow;
    public final int destCol;

    public Action(Player player, int srcRow, int srcCol, String actionType, int destRow, int destCol) {
        this.player = player;
        this.srcRow = srcRow;
        this.srcCol = srcCol;
        this.actionType = ActionType.valueOf(actionType);
        this.destRow = destRow;
        this.destCol = destCol;
    }

    @Override
    public String toString() {
        return srcRow + " " + srcCol + " " + actionType + " " + destRow + " " + destCol;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof com.codingame.game.action.Action) {
            com.codingame.game.action.Action other = (com.codingame.game.action.Action) obj;
            return srcRow == other.srcRow && srcCol == other.srcCol &&
                    destRow == other.destRow && destCol == other.destCol
                    && actionType == other.actionType;
        } else {
            return false;
        }
    }
}
