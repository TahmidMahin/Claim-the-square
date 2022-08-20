package com.codingame.game;

import com.codingame.game.action.ActionType;
import com.codingame.game.action.Action;
import com.codingame.gameengine.core.AbstractMultiplayerPlayer;
import com.codingame.gameengine.module.entities.Group;

public class Player extends AbstractMultiplayerPlayer {
    public Group hud;
    
    @Override
    public int getExpectedOutputLines() {
        return 1;
    }

    public Action getAction() throws TimeoutException, NumberFormatException {
        String[] output = getOutputs().get(0).split(" ");
        return new Action(this,
                Integer.parseInt(output[0]),
                Integer.parseInt(output[1]),
                output[2],
                Integer.parseInt(output[3]),
                Integer.parseInt(output[4]));
    }
}
