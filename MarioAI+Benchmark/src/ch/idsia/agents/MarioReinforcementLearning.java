package ch.idsia.agents;
import java.util.Random;
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
	boolean testing_state_1 = true;	
	boolean evaluating = false;
	boolean exploring = false;
	boolean training = true;
	double epsilon = 0.3;
	double alpha = 0.8;
	double gamma = 0.6;
	double initial_q_value = -.1;
	
	float difference_threshold = 0.1f;
	String current_state = "";
	float[] previous_pos;
	
	Random rand = new Random();
	
	
	ProgressTask task;
	int num_episodes = 20;
	
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
		
		//init();
		
	}
	
	@Override
	public void init() {
		// TODO Auto-generated method stub
		//initialize the simulation
		
		// start the simulation
		learn();
	}
	
	//============================================================================"GET" FUNCTIONS===========================================
	public boolean[] getAction()
	{
		return null;	// temporary so Eclipse will shut up
	}

	
	public String getState()
	{
		//System.out.println("Beginning to encode state");
		String state = "";
		state += encodeMarioMode() + "|";
		//System.out.println("Calling encodeMarioDirection()...");
		state += encodeMarioDirection() + "|";
		//System.out.println("Complete.");
		state += encodeMarioJump() + "|";
		state += encodeMarioGround() + "|";
		state += encodeNearbyEnemies() + "|";
		state += encodeObstaclesInFront() + "|";
		return state;
	}
	public boolean[] getExplorationAction()
	{
		System.out.println(" E X P L O R I N G");
		//get the list of possible actions
		String[] possible_states = getStatesFromState(current_state);
		String nextState = possible_states[rand.nextInt(possible_states.length)];
		return encodeActionToState(nextState);
	}
	public boolean[] getBestAction()
	{
		System.out.println(" F O L L O W I N G    Q T A B L E");
		// TO DO: Make it actually choose its best action from the Q table
	
		//get the list of actions
		String[] possible_states = getStatesFromState(current_state);
		//System.out.println(possible_states.length);
		//iterate over all of the actions and check their rewards, set nextState to
		//the highest of all of them
		String HighestState = possible_states[0];
		double HighestReward = -Double.MAX_VALUE;
		for(int i=0; i<possible_states.length;i++)
		{
			//find the state with the highest reward
			double reward = getR(possible_states[i]);
			if(reward > HighestReward)
			{
				HighestReward = reward;
				HighestState = possible_states[i];
			}
		}
		
		String nextState = HighestState;
		System.out.println(nextState);
		
		//encode the best action from possible_states into an action
		boolean[] act = encodeActionToState(nextState);
		
		//========================= UPDATE Q-TABLE =========================

		//get the HASH of actions from that state
		//updateQTable(currentState, nextState);
		Hashtable<String, Double> currentStateQ = Q.get(current_state);
		//make sure the thing isn't null
		if (currentStateQ == null)
		{
			Q.put(current_state, new Hashtable<String, Double>());
			currentStateQ = Q.get(current_state);
		}
		
		double newQ;
		
		if(!currentStateQ.containsKey(nextState))
			newQ = initial_q_value;
		else
			newQ = currentStateQ.get(nextState);
		
		//calculate the new Q value
		newQ += updateFormula(current_state, nextState, HighestReward, newQ);
		
		//newQ += alpha * (HighestReward + (gamma * getMaxQ(nextState)) - newQ);
		currentStateQ.put(current_state, newQ);
		
		action = act;
		return act;
	}
	
	private void setFireMode()
	{
		environment.levelScene.mario.setMode(true,true);
	}
	
	private void setBigMode()
	{
		environment.levelScene.mario.setMode(true,false);
	}
	
	private void setSmallMode()
	{
		environment.levelScene.mario.setMode(false,false);
	}
	
	
	
	
	public double updateFormula(String current, String next, double reward, double q)
	{
		return 0.0;
	}
	
	//get all of the actions possible from the current state
	public String[] getStatesFromState(String state)
	{
		
		int x_movement_variations = 3;	// because determining the velocity is near impossible for our time frame
		int y_movement_variations = 3;	// because EVEN IF mario is jumping, if he holds it down he can jump higher
		String[] actions = new String[ x_movement_variations * y_movement_variations ];
		
		String[] x_movements = new String[3];
		x_movements[0] = "00";
		x_movements[1] = "10";
		x_movements[2] = "11";
		
		String[] y_movements = new String[3];
		y_movements[0] = "00";
		y_movements[1] = "10";
		y_movements[2] = "11";
		
		for(int i=0; i<x_movements.length;i++)
		{
			for(int j=0; j<y_movements.length;j++)
			{
				String combo = "";
				combo+=x_movements[i]+y_movements[j];
				
				int dex = i*3 + j;
				actions[dex] = state.substring(0,3);
				actions[dex] += combo;
				actions[dex] += state.substring(7,state.length());
			}
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
	public double getR(String state)
	{
		double right_reward = 2;
		double up_reward = 2;
		
		if(!R.contains(state))
		{
			R.put(state, 0.0);
		}
		else
		{
			System.out.println("Already contained that state!");
		}
		//reward for going right
		System.out.println(state);
		
		if(state.charAt(4) == '1')
		{
			System.out.println("rewarding right state");
			R.put(state, R.get(state) + right_reward);
		}
		if(state.charAt(6) == '1')
		{
			System.out.println("rewarding up state");
			R.put(state, R.get(state) + up_reward);
		}
		
		System.out.printf("state: %s  reward: %f\n", state, R.get(state));
		return R.get(state);
	}
	
	public double getReward(String currentState, String nextState)
	{
		return 0;
	}
	
	//return the action with the highest reward
	public String getMaxR(String[] states)
	{
		int j=0;
		double maxR = -Double.MAX_VALUE;
		for(int i=0; i<states.length;i++)
		{
			if(getR(states[i]) > maxR)
			{
				j = i;
				maxR = getR(states[i]);
			}
		}
		return states[j];
	}
	
	
	public double getMaxQ(String state)
	{
		Hashtable<String, Double> options = Q.get(state);
		
		if(options == null)
		{
			Q.put(state, new Hashtable<String, Double>());
			return initial_q_value;
		}
		
		double greatestQ = -Double.MAX_VALUE;
		
		//iterate over each Q value and return the max
		//keep the entry just in case
		Iterator<Map.Entry<String, Double>> it = options.entrySet().iterator();
		while(it.hasNext())
		{
			Map.Entry<String, Double> entry = it.next();
			if( entry.getValue() > greatestQ )
			{
				greatestQ = entry.getValue();
			}
		}
		
		return greatestQ;
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
		//System.out.println("Starting of encodeMarioDirection()...");
		String msg = "";
		//System.out.printf("KEY_RIGHT: %b\tKEY_LEFT: %b\n", action[Mario.KEY_RIGHT], action[Mario.KEY_LEFT]);
		// make x direction encoding
		if(action[Mario.KEY_RIGHT] && !action[Mario.KEY_LEFT])	// mario is going right
		{
			msg += "11";
		}
		else if(!action[Mario.KEY_RIGHT] && action[Mario.KEY_LEFT] )													// mario is going left
		{
			msg += "10";
		}
		else
		{
			msg += "00";
		}
		/*
		if(previous_pos == null)
		{
			msg+="00";
			return msg;
		}
		*/
		// make y direction encoding
		if(action[Mario.KEY_JUMP] && !action[Mario.KEY_DOWN])	// mario is going right
		{
			msg += "11";
		}
		else if(!action[Mario.KEY_JUMP] && action[Mario.KEY_DOWN] )													// mario is going left
		{
			msg += "10";
		}
		else
		{
			msg += "00";
		}
		
		System.out.println("direction encoded message: " + msg);
		
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
		//System.out.println("enemy encoding: " + msg);
		return msg;
	}
	public String encodeObstaclesInFront()
	{
		String msg = "0";
		
		int x = 9;
		int y = 10;
		if(marioMode == 0)
		{
			y = 9;
		}
		
		int coord10 = getReceptiveFieldCellValue(x,y+0);
		int coord11 = getReceptiveFieldCellValue(x,y-1);
		int coord12 = getReceptiveFieldCellValue(x,y-2);
		
		if(coord10 != 0 || coord11 != 0 || coord12 != 0)
		{
			msg = "1";
		}
		//System.out.println("returning "+msg);
		return msg;
	}
	
	//encode the action to get to state from prevState
	public boolean [] encodeActionToState(String goalState)
	{
		boolean[] act = getEmptyAction();
		
		// should Mario shoot or nah
		//if he is to shoot
		if(goalState.charAt(0) == '1' && goalState.charAt(1) == '1')
		{	// if he is fire mario, always shoot
			// this is consistent encoding from our hand agent that we developed originally
			act[Mario.KEY_SPEED] = true;
		}
		else
		{
			act[Mario.KEY_SPEED] = false;
		}
		
		
		// --- this is not truly the direction of mario's velocity ---
		if(goalState.charAt(3)== '1')
		{
			//go left
			if(goalState.charAt(4)=='1')
			{
				act[Mario.KEY_RIGHT] = true;
				act[Mario.KEY_LEFT] = false;
			}
			else
			{
				act[Mario.KEY_RIGHT] = false;
				act[Mario.KEY_LEFT] = true;
			}
		}
		else
		{
			act[Mario.KEY_RIGHT] = false;
			act[Mario.KEY_LEFT] = false;
		}
		
		//if mario is to jump
		if(goalState.charAt(5) == '1')
		{
			if(goalState.charAt(6)=='1')
			{
				act[Mario.KEY_JUMP] = true;
				act[Mario.KEY_DOWN] = false;
			}
			else
			{
				act[Mario.KEY_JUMP] = false;
				act[Mario.KEY_DOWN] = true;
			}
		}
		else
		{
			act[Mario.KEY_JUMP] = false;
			act[Mario.KEY_DOWN] = false;
		}
		
		
		
		return act;
	}	
	
	//================================================================================LEARNING AND REWARDS======================================
	@Override
	public void learn() {
		// TODO Auto-generated method stub
		task.evaluate(this);
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
		this.num_episodes = num;
		
	}

	@Override
	public Agent getBestAgent() {
		// TODO Auto-generated method stub
		//don't need this
		return null;
	}
}