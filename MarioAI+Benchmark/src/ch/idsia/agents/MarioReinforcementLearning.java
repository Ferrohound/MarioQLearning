package ch.idsia.agents;
import java.util.Hashtable;

import ch.idsia.agents.Agent;
import ch.idsia.agents.BasicMarioAIAgent;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.environments.Environment;
import ch.idsia.benchmark.tasks.ProgressTask;

public class MarioReinforcementLearning extends BasicMarioAIAgent implements LearningAgent
{
	boolean evaluating = false;
	double epsilon = 0.3;
	double alpha = 0.8;
	double gamma = 0.6;
	
	float difference_threshold = 0.1f;
	String current_state = "";
	boolean testing_state_1 = true;
	float[] previous_pos;
	
	//hash table of Q values
	//input a string, get a hashtable of all the possible actions from that string
	//and their respective Q values
	//bool[] instead of string?
	Hashtable<String, Hashtable<String, Integer>> Q;
	
	
	
	public MarioReinforcementLearning(String name)
	{
		super(name);

		previous_pos = marioFloatPos;
		Q = new Hashtable<String, Hashtable<String, Integer>>();
		
	}
	
	
	public boolean[] getAction()
	{
		if(testing_state_1)
		{
			current_state = getState();
			if(evaluating)
			{
				/* choose the most rewarding action from the current state according to Q table
				 * and return that action from this function */
				return getBestAction();
				
			}
			else
			{
				/* Get random value, if it is less than epsilon, then pick a random action
				 * Otherwise, follow the path - "do what you're supposed to"
				 */
				
						
			}
		}
		
		
		
		return null;	// temporary so Eclipse will shut up
	}

	
	public String getState()
	{
		String state = "";
		state += encodeMarioMode() + "|";
		state += encodeMarioDirection() + "|";
		state += encodeMarioJump() + "|";
		state += encodeMarioGround() + "|";
		state += encodeNearbyEnemies() /*+ "|"*/;
		return state;
	}
	
	public boolean[] getBestAction()
	{
		boolean[] act = getEmptyAction();
		// TO DO: Make it actually choose its best action from the Q table
		
		//get the current state 
		String currentState = getState();
		
		//get the list of actions from that state
		Hashtable<String, Integer> options = Q.get(currentState);
		
		//get the list of actions
		String[] actions = getActionsFromState(currentState);
		
		//iterate over all of the actions and check their Q values, set nextState to
		//the highest of all of them
		int j = 0;
		float maxQ = 0;
		for(int i=0; i<actions.length;i++)
		{
			if(options.get(actions[i])>maxQ)
			{
				j = i;
				maxQ = options.get(actions[i]);
			}
		}
		
		String nextState = actions[j];
		
		act = encodeAction(currentState, nextState);
		//encode the best action from options and store it in act
		
		return act;
	}
	
	//get all of the actions possible from the current state
	public String[] getActionsFromState(String state)
	{
		String[] actions = new String[100];
		//5 actions for movement
		//to jump or not to jump
		//to shoot or not to shoot
		//take first bit of the input, add options then add last bit of input
		
		/*
		 * bla|    | | bla
		 * 
		 * |0000|0|stay still
		 * |1100|0|go left
		 * |1000|0|go right
		 * |0000|0|go left
		 * 
		 */
		
		
		
		
		return actions;
	}
	
	//encode the action to get to state from prevState
	public boolean [] encodeAction(String prevState, String state)
	{
		boolean[] act = getEmptyAction();
		
		return act;
	}
	
	public boolean[] getEmptyAction()
	{
		boolean[] act = new boolean[Environment.numberOfButtons];
		for(int i=0; i<Environment.numberOfButtons; i++)
			act[i] = false;
		return act;
	}
	
	public String encodeMarioMode()
	{
		String msg = "";
		switch(marioMode)
		{
			case 2: msg += "10";		// fire mario
					break;
			case 1: msg += "01";		// big mario
					break;
			default: msg += "00";		// small mario
					break;
		}
		return msg;
		
	}
	
	public String encodeMarioDirection()
	{
		String msg = "";
		
		// make x direction encoding
		if( Math.abs(previous_pos[0] - marioFloatPos[0]) < difference_threshold )
		{
			msg += "00";
		}
		else if(marioFloatPos[0] > previous_pos[0])
		{
			msg += "11";
		}
		else
		{
			msg += "10";
		}
		
		// make y direction encoding
		if( Math.abs(previous_pos[1] - marioFloatPos[1]) < difference_threshold )
		{
			msg += "00";
		}
		else if(marioFloatPos[1] > previous_pos[1])
		{
			msg += "11";
		}
		else
		{
			msg += "10";
		}
		
		return msg;
	}
	
	public String encodeMarioGround()
	{
		if(isMarioOnGround)
			return("1");
		return("0");
	}
	
	public String encodeMarioJump()
	{
		if(isMarioAbleToJump)
			return("1");
		return("0");
	}
	
	public String encodeNearbyEnemies()
	{
		String msg = "";
		
		switch(marioMode)
		{
		case 0: 	// little baby boy
			
			// -1,1
			if(getEnemyFieldCellValue(-1,1) != 0)
			{
				msg += "1";
			}
			else
			{
				msg += "0";
			}
			
			// 0,1
			if(getEnemyFieldCellValue(0,1) != 0)
			{
				msg += "1";
			}
			else
			{
				msg += "0";
			}
			
			// 1,1
			if(getEnemyFieldCellValue(1,1) != 0)
			{
				msg += "1";
			}
			else
			{
				msg += "0";
			}
			
			// 1,0
			if(getEnemyFieldCellValue(1,0) != 0)
			{
				msg += "1";
			}
			else
			{
				msg += "0";
			}
			
			// 1,-1
			if(getEnemyFieldCellValue(1,-1) != 0)
			{
				msg += "1";
			}
			else
			{
				msg += "0";
			}
			
			// 0,-1
			if(getEnemyFieldCellValue(0,-1) != 0)
			{
				msg += "1";
			}
			else
			{
				msg += "0";
			}
			
			// -1,-1
			if(getEnemyFieldCellValue(-1,-1) != 0)
			{
				msg += "1";
			}
			else
			{
				msg += "0";
			}
			
			// -1,0
			if(getEnemyFieldCellValue(-1,0) != 0)
			{
				msg += "1";
			}
			else
			{
				msg += "0";
			}
			
			
			break;
		default: 	// "meat on them bones" Mario
			
			
			// -1,2
			if(getEnemyFieldCellValue(-1,2) != 0)
			{
				msg += "1";
			}
			else
			{
				msg += "0";
			}
			
			// 0,2
			if(getEnemyFieldCellValue(0,2) != 0)
			{
				msg += "1";
			}
			else
			{
				msg += "0";
			}
			
			// 1,2
			if(getEnemyFieldCellValue(1,2) != 0)
			{
				msg += "1";
			}
			else
			{
				msg += "0";
			}
			
			// 1,0
			//check two cells instead of one
			if(getEnemyFieldCellValue(1,1) != 0 || getEnemyFieldCellValue(1, 0)!=0)
			{
				msg += "1";
			}
			else
			{
				msg += "0";
			}
			
			// 1,-1
			if(getEnemyFieldCellValue(1,-1) != 0)
			{
				msg += "1";
			}
			else
			{
				msg += "0";
			}
			
			// 0,-1
			if(getEnemyFieldCellValue(0,-1) != 0)
			{
				msg += "1";
			}
			else
			{
				msg += "0";
			}
			
			// -1,-1
			if(getEnemyFieldCellValue(-1,-1) != 0)
			{
				msg += "1";
			}
			else
			{
				msg += "0";
			}
			
			// -1,0
			//check two cells instead of just one
			if(getEnemyFieldCellValue(-1,0) != 0 || getEnemyFieldCellValue(-1, 1)!=0)
			{
				msg += "1";
			}
			else
			{
				msg += "0";
			}
			
			
			
			break;
		}
		
		return msg;
	}
	
	
	
	@Override
	public void learn() {
		// TODO Auto-generated method stub
		
	}

	//reward mario for moving right and upwards
	//as well as killing enemies and completing the level (big reward)
	//pushis for going left and walking into enemies
	@Override
	public void giveReward(float reward) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void newEpisode() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setTask(ProgressTask task) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setNumberOfTrials(int num) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Agent getBestAgent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}
}