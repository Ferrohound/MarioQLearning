package ch.idsia.agents.controllers;

import ch.idsia.agents.Agent;
import ch.idsia.agents.BasicMarioAIAgent;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.environments.Environment;

/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy
 * Date: Apr 25, 2009
 * Time: 12:27:07 AM
 * Package: ch.idsia.agents.controllers;
 */

public class ForwardJumpingAgent extends BasicMarioAIAgent implements Agent
{
    public ForwardJumpingAgent()
    {
        super("ForwardJumpingAgent");
        reset();
    }

    public boolean[] getAction()
    {
    	if(getEnemyFieldCellValue(0,-1) != 0 || getEnemyFieldCellValue(0,-2) != 0 || getEnemyFieldCellValue(0,-3) != 0 )
    	{
    		System.out.println("There is an enemy.");
    	}
        action[Mario.KEY_SPEED] = action[Mario.KEY_JUMP] = isMarioAbleToJump || !isMarioOnGround;
        return action;
    }

    public void reset()
    {
        action = new boolean[Environment.numberOfButtons];
        action[Mario.KEY_RIGHT] = true;
        action[Mario.KEY_SPEED] = true;
    }
    
    public int getEnemyFieldCellValue(int x, int y)
    {
    	x+=11;	// account for Mario being in the middle of the screen
    	y+=11;	// account for Mario being in the middle of the screen
    	
        if (x < 0 || x >= enemies.length || y < 0 || y >= enemies[0].length)
            return 0;

        return enemies[x][y];
    }
}
