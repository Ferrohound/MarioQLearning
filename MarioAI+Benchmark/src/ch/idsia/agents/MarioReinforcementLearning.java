package ch.idsia.agents;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import ch.idsia.agents.Agent;
import ch.idsia.agents.BasicMarioAIAgent;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.environments.Environment;
import ch.idsia.benchmark.tasks.ProgressTask;

/*
 * Neil and Fuller
 * 
 */

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
	
	ProgressTask task;
	
	//hash table of Q values
	//input a string, get a hashtable of all the possible actions from that string
	//and their respective Q values
	Hashtable<String, Hashtable<String, Double>> Q;
	
	//reward table, reward mario for going right and upwards
	//punish for colliding with enemies and going left
	Hashtable<String, Double> R;
	
	
	
	public MarioReinforcementLearning(String name)
	{
		super(name);

		previous_pos = marioFloatPos;
		Q = new Hashtable<String, Hashtable<String, Double>>();
		R = new Hashtable<String, Double>();
		
		init();
		
	}
	
	@Override
	public void init() {
		// TODO Auto-generated method stub
		//initialize the simulation
		
	}
	
	//============================================================================"GET" FUNCTIONS===========================================
	public boolean[] getAction()
	{
		if(testing_state_1)
		{
			//update the Q-Values
			learn();
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
				
				evaluating = false;
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
		Hashtable<String, Double> options = Q.get(currentState);
		
		//get the list of actions
		String[] actions = getActionsFromState(currentState);
		
		//iterate over all of the actions and check their rewards, set nextState to
		//the highest of all of them
		int j = 0;
		double reward = 0;
		for(int i=0; i<actions.length;i++)
		{
			//find the state with the highest reward
			if(R.get(actions[i])>reward)
			{
				j = i;
				reward = R.get(actions[i]);
			}
		}
		
		String nextState = actions[j];
		
		//encode the best action from options and store it in act
		act = encodeAction(nextState);
		
		//============================================================================UPDATE QTable============================
		options = Q.get(currentState);
		double newQ = options.get(actions[j]);
		
		double rwd = getReward(currentState, nextState);
		
		newQ += alpha * (rwd + (gamma * getMaxQ(currentState)) - newQ);
		options.put(currentState, newQ);
		
		return act;
	}
	
	public double getReward(String currentState, String nextState)
	{
		
		return 0;
	}
	
	//get all of the actions possible from the current state
	public String[] getActionsFromState(String state)
	{
		String[] actions = new String[20];
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
		 * |0010|0|go up
		 * |0000|0|go(ing?) down(?)
		 */
		String[] additions = new String[3];
		additions[0] = "1100";
		additions[1] = "1000";
		additions[2] = "0010";
		
		for(int i=0; i<3; i++)
		{
			String tmp = state.substring(0,4);
			
			tmp+=additions[i];
			tmp+=state.substring(7, 21);
			additions[i] = tmp;
			
			actions[i] = additions[i];
		}	
		
		return actions;
	}
	
	public boolean[] getEmptyAction()
	{
		boolean[] act = new boolean[Environment.numberOfButtons];
		for(int i=0; i<Environment.numberOfButtons; i++)
			act[i] = false;
		return act;
	}
	
	//get the reward for a certain action
	public double getR(String act)
	{
		return R.get(act);
	}
	
	//return the action with the highest reward
	public String getMaxR(String[] acts)
	{
		int j=0;
		double maxR = 0;
		for(int i=0; i<acts.length;i++)
		{
			if(getR(acts[i])>maxR)
			{
				j = i;
				maxR = getR(acts[i]);
			}
		}
		return acts[j];
	}
	
	
	public double getMaxQ(String state)
	{
		Hashtable<String, Double> options = Q.get(state);
		
		double max = 0;
		
		//iterate over each Q value and return the max
		//keep the entry just in case
		Map.Entry<String, Double> tmp;
		Iterator<Map.Entry<String, Double>> it = options.entrySet().iterator();
		while(it.hasNext())
		{
			Map.Entry<String, Double> entry = it.next();
			if(entry.getValue()>max)
			{
				max = entry.getValue();
				tmp = entry;
			}
		}
		
		return max;
		//return tmp.getKey(
	}
	//===============================================================MARIO ENCODING===========================================================
	
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
	
	
	//encode the action to get to state from prevState
	public boolean [] encodeAction(String state)
	{
		boolean[] act = getEmptyAction();
		String next = state.substring(3, 7);
		
		// index as such => act[Mario.KEY_JUMP] = false;
		
		
		
		return act;
	}	
	
	//================================================================================LEARNING AND REWARDS======================================
	@Override
	public void learn() {
		// TODO Auto-generated method stub
		//update the Q table, then set evaluate to true and make
		//the best choice based on that
		
		for(int i=0; i<5000; i++)
		{
			
		}
		
		evaluating = true;
	}

	//reward mario for moving right and upwards
	//as well as killing enemies and completing the level (big reward)
	//pushis for going left and walking into enemies
	@Override
	public void giveReward(float reward) {
		// TODO Auto-generated method stub
		//I assume this is just supplementing the Q value for the current state?
		
	}

	@Override
	public void newEpisode() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setTask(ProgressTask task) {
		// TODO Auto-generated method stub
		this.task = task;
	}

	@Override
	public void setNumberOfTrials(int num) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Agent getBestAgent() {
		// TODO Auto-generated method stub
		//don't need this
		return null;
	}
}