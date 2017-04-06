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
	double alpha0 = 0.8;
	double gamma = 0.6;
	double initial_q_value = -.1;
	
	float difference_threshold = 0.1f;
	String current_state = "";
	float[] previous_pos;
	int prevMarioMode = -1;
	int prevEnemiesStomped = 0;
	int prevEnemiesFireballed =  0;
	

	final double right_reward = 0.0001;//1;
	final double up_reward = 0.0001;
	final double damage_reward = -400.0;
	final double fire_reward = 40.0;
	final double stomp_reward = 80.0;
	final double win_reward = 1000.0;
	final double death_reward = -500.0;
	
	boolean running = false;
	int progress = 0;
	
	int numEpisodes = 3000;
	
	Random rand = new Random();
	
	ProgressTask task;
	int training_episodes = 20;
	
	//hash table of Q values
	//input a string, get a hashtable of all the possible actions from that string
	//and their respective Q values
	Hashtable<String, Hashtable<String, Double>> Q;
	
	//reward table, reward mario for going right and upwards
	//punish for colliding with enemies and going left
	Hashtable<String, Double> R;
	
	//for the converging alpha
	//access this for the alpha values
	Hashtable<String, Hashtable<String, Integer>> AlphaHash;
	
	
	
	public MarioReinforcementLearning(String name)
	{
		super(name);

		previous_pos = marioFloatPos;
		Q = new Hashtable<String, Hashtable<String, Double>>();
		R = new Hashtable<String, Double>();
		AlphaHash = new Hashtable<String, Hashtable<String, Integer>>();
		
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
		state += encodeMidRangeEnemies() + "|";
		state += encodeObstaclesInFront() + "|";
		state += encodeRanIntoEnemy() + "|";
		state += encodeKills() + "|";
		state += encodeMarioStatus() + "|";
		//state += encodeMarioCanShoot() + "|";
		state += encodeIsMarioShooting() + "|";
		//state += encodeMarioshouldShoot() + "|";
		//System.out.println(current_state);
		return state;
	}
	public boolean probe(int x, int y)
	{
		return getEnemyFieldCellValue(-y,x) != 0;
	}
	public boolean[] getExplorationAction()
	{
		giveRCurrent();

		//get the list of possible actions
		String[] possible_states = getStatesFromState(current_state);
		
		String nextState = possible_states[rand.nextInt(possible_states.length)];
		
		updatePreviousVariables();
		
		return encodeActionToState(nextState);
	}
	public boolean[] getBestAction()
	{
		//System.out.println("MarioReinforcementLearning getBestAction()");
		giveRCurrent();
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
			//giveR(possible_states[i]);
			double reward = getR(possible_states[i]);
			if(reward > HighestReward)
			{
				HighestReward = reward;
				HighestState = possible_states[i];
			}
		}
		
		String nextState = HighestState;
		//System.out.println(nextState);
		
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
		
		double Q_val;
		
		if( currentStateQ.get(nextState) == null)
			Q_val = initial_q_value;
		else
			Q_val = currentStateQ.get(nextState);
		
		//calculate the new Q value
		Q_val += updateFormula(nextState, HighestReward, Q_val, calculateAlpha(nextState, act));
		//Q_val += updateFormula(nextState, HighestReward, Q_val, alpha0);
		
		currentStateQ.put(current_state, Q_val);
		
		//System.out.println(current_state);
		
		action = act;
		//act[Mario.KEY_SPEED] = !act[Mario.KEY_SPEED];
		return act;
	}
	
	public void updatePreviousVariables()
	{
		prevMarioMode = marioMode;
		prevEnemiesStomped = environment.levelScene.getKillsByStomp();
		prevEnemiesFireballed = environment.levelScene.getKillsByFire();
	}
	
	// This function does a decreasing alpha value for repeated actions from the same state
	public double calculateAlpha(String state, boolean[] act)
	{
		String act_str = getActionString(act);
		// if the state is uniquequequequequeque
		if(AlphaHash.get(state) != null)
		{
			Hashtable<String,Integer> alphas = AlphaHash.get(state);
			if(alphas.get(act_str) != null)
			{
				// get the number of times the action has been taken
				int repeats = alphas.get(act_str); // increase the counter for the times the action has been taken
				alphas.put(act_str, repeats+1);
				
				return alpha0 / repeats;
				
			}
			else
			{
				alphas.put(act_str,1);
				return alpha0;
			}
		}
		else
		{
			// loads up the new state into the AlphaHash, and puts the # of times the action is taken to 1
			Hashtable<String,Integer> tmp = new Hashtable<String,Integer>();
			tmp.put(act_str, 1);
			
			AlphaHash.put(state,tmp);
			return alpha0;
		}
	}
	
	public String getActionString(boolean[] act)
	{
		String msg = "";
		for(int i=0; i<act.length;i++)
		{
			if(act[i])
			{
				msg+="1";
			}
			else
			{
				msg+="0";
			}
		}
		return msg;
	}
	
	public double updateFormula(String next, double reward, double q, double a)
	{
		return 0.0;
	}
	
	//get all of the actions possible from the current state
	public String[] getStatesFromState(String state)
	{
		int x_movement_variations = 3;	// because determining the velocity is near impossible for our time frame
		int y_movement_variations = 3;	// because EVEN IF mario is jumping, if he holds it down he can jump higher
		int shooting_variations = 2;
		String[] actions = new String[ x_movement_variations * y_movement_variations * shooting_variations ];
		
		String[] x_movements = new String[3];
		x_movements[0] = "00";
		x_movements[1] = "10";
		x_movements[2] = "11";
		
		String[] y_movements = new String[3];
		y_movements[0] = "00";
		y_movements[1] = "10";
		y_movements[2] = "11";
		
		/*
		String[] shoots = new String[2];
		shoots[0] = "0";
		shoots[1] = "1";
		*/
		
		for(int i=0; i<x_movements.length;i++)
		{
			for(int j=0; j<y_movements.length;j++)
			{
				for(int k=0; k<shooting_variations;k++){
					int dex = (i*3 + j)*2 + k;
					actions[dex] = state.substring(0,3);
					actions[dex] += x_movements[i]+y_movements[j];
					actions[dex] += state.substring(7,state.length()-2/*-2*/);
					actions[dex] += Integer.toString(k)+"|";
				}
			}
		}
		
		/*
		System.out.println("RETURNING THE FOLLOWING STATES:");
		for(int i=0; i<actions.length; i++)
		{
			System.out.printf("[%d]: %s\n",i, actions[i]);
		}
		*/
		
		return actions;
	}
	
	public boolean[] getEmptyAction()
	{
		boolean[] act = new boolean[Environment.numberOfButtons];
		for(int i=0; i<Environment.numberOfButtons; i++)
			act[i] = false;
		return act;
	}
	
	public double getR(String state)
	{
		//giveR(state);
		//System.out.println(state);
		if(R.get(state) == null)
		{
			R.put(state, initial_q_value);
		}
		
		return R.get(state) + getIntermediateR(state);
	}
	public void giveRCurrent()
	{
		if(R.get(current_state) == null)
		{
			R.put(current_state, initial_q_value);
		}
		
		if(current_state.charAt(25+9) == '1')
		{
			//System.out.println("rewarding stomp state");
			R.put(current_state,  R.get(current_state)+stomp_reward);
		}
		if(current_state.charAt(26+9) == '1')
		{
			//System.out.println("rewarding fireball state");
			R.put(current_state,  R.get(current_state)+fire_reward);
		}
		
		if(current_state.charAt(28+9) == '1' && current_state.charAt(29+9) == '1')
		{
			//System.out.println("YOU DID IT!");
			R.put(current_state, R.get(current_state)+ win_reward);
		}
		else if(current_state.charAt(29+9) == '1')
		{
			//System.out.println("YOU CLOWN!");
			R.put(current_state, R.get(current_state)+ death_reward);
		}
		else if(current_state.charAt(23+9) == '1')
		{
			//System.out.println("rewarding damaged state");
			R.put(current_state, R.get(current_state) + damage_reward);
		}
	}
	
	//get the reward for a certain action
	public double getIntermediateR(String state)
	{
		double r = initial_q_value;
		if(state.charAt(4) == '1')
		{
			// if enemy nearby directly in front, or in front and up
			if(state.charAt(13) == '1' || state.charAt(14) == '1')
			{
				//System.out.println(state);
				//System.out.println("QUARTER of the RIGHT reward!");
				r -= right_reward/2f;
			}
			else if(state.charAt(24) == '1')
			{
				//System.out.println(state);
				//System.out.println("HALF of the RIGHT reward!");
				r -= right_reward/4f;
			}
			else
			{
				r += right_reward;				
			}
		}
		if(state.charAt(6) == '1')
		{
			//System.out.println("rewarding up state");
			if(state.charAt(12) == '1' || state.charAt(13) == '1')
			{
				//System.out.println(state);
				//System.out.println("QUARTER of the UP reward!");
				r -= up_reward/2f;
			}
			else if(state.charAt(21) == '1' || state.charAt(22) == '1')
			{
				//System.out.println(state);
				//System.out.println("HALF of the UP reward!");
				r -= up_reward/4f;
			}
			else
			{
				r += up_reward;				
			}
		}
		return r;
		
	}
	public void giveR(String state)
	{
		
		if( R.get(state) == null)
		{
			R.put(state, initial_q_value);
		}
		
		if(state.charAt(4) == '1')
		{
			//System.out.println("rewarding right state");
			R.put(state, R.get(state) + right_reward);
		}
		if(state.charAt(6) == '1')
		{
			//System.out.println("rewarding up state");
			R.put(state, R.get(state) + up_reward);
		}
		
	}
	
	/*
	public double getReward(String currentState, String nextState)
	{
		return 0;
	}
	*/
	
	/*
	//return the action with the highest reward
	public String getMaxR(String[] states)
	{
		int j=0;
		double maxR = -Double.MAX_VALUE;
		for(int i=0; i<states.length;i++)
		{
			System.out.println("Calling getR from getMaxR");
			if(getR(states[i]) > maxR)
			{
				j = i;
				maxR = getR(states[i]);
			}
		}
		return states[j];
	}
	*/
		
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
		switch(marioMode)
		{
			case 2: return "10";		// fire mario
			case 1: return "01";		// big mario
			default: return "00";		// small mario
		}
	}
	public String encodeIsMarioShooting()
	{
		if(isMarioAbleToShoot && action[Mario.KEY_SPEED])
		{
			return "1";
		}
		return "0";
	}
	public String encodeMarioStatus()
	{
		//Mario.STATUS_WIN
		if(marioStatus == Mario.STATUS_DEAD)
		{
			return "01";
		}
		if(marioStatus == Mario.STATUS_WIN)
		{
			return "11";
		}
		
		return "00";
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
		
		//System.out.println("direction encoded message: " + msg);
		
		return msg;
	}
	public String encodeMarioGround()
	{
		if(isMarioOnGround)
			return "1";
		return "0";
	}
	public String encodeMarioJump()
	{
		if(isMarioAbleToJump)
			return("1");
		return("0");
	}
	public String encodeMarioCanShoot()
	{
		if(isMarioAbleToShoot && marioMode == 2)
		{
			return "1";
		}
		return "0";
	}
	
	
	
	public String encodeNearbyEnemies()
	{
		String msg = "";
		
		switch(marioMode)
		{
		case 0: 	// little baby boy
			
			// -1,1
			if(probe(-1,1))
			{
				msg += "1";
			}
			else
			{
				msg += "0";
			}
			
			// 0,1
			if(probe(0,1))
			{
				//System.out.println("--- Enemy directly above ---");
				msg += "1";
			}
			else
			{
				msg += "0";
			}
			
			// 1,1
			if(probe(1,1))
			{
				msg += "1";
			}
			else
			{
				msg += "0";
			}
			
			// 1,0
			if(probe(1,0))
			{
				//System.out.println("--- Enemy directly in front ---");
				msg += "1";
			}
			else
			{
				msg += "0";
			}
			
			// 1,-1
			if(probe(1,-1))
			{
				msg += "1";
			}
			else
			{
				msg += "0";
			}
			
			// 0,-1
			if(probe(0,-1))
			{
				//System.out.println("--- Enemy directly below ---");
				msg += "1";
			}
			else
			{
				msg += "0";
			}
			
			// -1,-1
			if(probe(-1,-1))
			{
				msg += "1";
			}
			else
			{
				msg += "0";
			}
			
			// -1,0
			if(probe(-1,0))
			{
				msg += "1";
			}
			else
			{
				msg += "0";
			}
			return msg;
		default: 	// "meat on them bones" Mario
			
			
			// top left 
			if(probe(-1,1))
			{
				msg += "1";
			}
			else
			{
				msg += "0";
			}
			
			// top middle
			if(probe(0,1))
			{
				msg += "1";
			}
			else
			{
				msg += "0";
			}
			
			// top right
			if(probe(1,1))
			{
				msg += "1";
			}
			else
			{
				msg += "0";
			}
			
			// front
			if(probe(1,0) || probe(1,-1))
			{
				msg += "1";
			}
			else
			{
				msg += "0";
			}
			
			// bottom right
			if(probe(1,-2))
			{
				msg += "1";
			}
			else
			{
				msg += "0";
			}
			
			// bottom middle
			if(probe(0,-2))
			{
				msg += "1";
			}
			else
			{
				msg += "0";
			}
			
			// bottom left
			if(probe(-1,-2))
			{
				msg += "1";
			}
			else
			{
				msg += "0";
			}
			
			// back
			if(probe(-1,-1) || probe(-1, 0))
			{
				msg += "1";
			}
			else
			{
				msg += "0";
			}
			return msg;
		}
	}
	
	public String encodeMidRangeEnemies()
	{
		String msg = "";
		
		switch(marioMode)
		{
		case 0: 	// little baby boy
			// top left grouping
			if(probe(-2,1) || probe(-2,2) || probe(-1,2))
			{
				msg += "1";
			}
			else
			{
				msg += "0";
			}
			// top middle grouping
			if(probe(0,2))
			{
				//System.out.println("Midrange enemy directly above");
				msg += "1";
			}
			else
			{
				msg += "0";
			}
			// top right grouping
			if(probe(1,2) || probe(2,2) || probe(2,1))
			{
				//System.out.println("Midrange enemy up and right");
				msg += "1";
			}
			else
			{
				msg += "0";
			}
			// front grouping
			if(probe(2,0))
			{
				//System.out.println("Midrange enemy directly in front!");
				msg += "1";
			}
			else
			{
				msg += "0";
			}
			// bottom rightt grouping
			if(probe(2,-1) || probe(2,-2) || probe(1,-2))
			{
				//System.out.println("Midrange enemy bottom right!");
				msg += "1";
			}
			else
			{
				msg += "0";
			}
			// bottom middle grouping
			if(probe(0,-2))
			{
				//System.out.println("Midrange enemy IS BELOW ME");
				msg += "1";
			}
			else
			{
				msg += "0";
			}
			// bottom left grouping
			if(probe(-1,-2) || probe(-2,-2) || probe(-2,-1))
			{
				//System.out.println("Midrange enemy in bottom left");
				msg += "1";
			}
			else
			{
				msg += "0";
			}
			// back grouping
			if(probe(-2,0))
			{
				//System.out.println("Midrange enemy directly back");
				msg += "1";
			}
			else
			{
				msg += "0";
			}
			break;
		default: 	// "meat on them bones" Mario
			// top left grouping
			if(probe(-2,1) || probe(-2,2) || probe(-1,2))
			{
				msg += "1";
			}
			else
			{
				msg += "0";
			}
			// top middle grouping
			if(probe(0,2))
			{
				msg += "1";
			}
			else
			{
				msg += "0";
			}
			// top right grouping
			if(probe(1,2) || probe(2,2) || probe(2,1))
			{
				msg += "1";
			}
			else
			{
				msg += "0";
			}
			// front grouping
			if(probe(2,0) || probe(2,-1))
			{
				msg += "1";
			}
			else
			{
				msg += "0";
			}
			// bottom right grouping
			if(probe(2,-2) || probe(2,-3) || probe(1,-3))
			{
				msg += "1";
			}
			else
			{
				msg += "0";
			}
			// bottom middle grouping
			if(probe(0,-3))
			{
				msg += "1";
			}
			else
			{
				msg += "0";
			}			
			// bottom left grouping
			if(probe(-1,-3) || probe(-2,-3) || probe(-2,-2))
			{
				msg += "1";
			}
			else
			{
				msg += "0";
			}
			// back grouping
			if(probe(-2,-1) || probe(-2,0))
			{
				msg += "1";
			}
			else
			{
				msg += "0";
			}
			break;
		}
		//System.out.println("midrange enemy encoding: " + msg);
		return msg;
	}
	public String encodeObstaclesInFront()
	{
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
			return "1";
		}
		return "0";
	}
	public String encodeRanIntoEnemy()
	{
		//System.out.println("Mario Mode => " + marioMode+" prev Mode => " + prevMarioMode);
		if(marioMode < prevMarioMode)
		{
			prevMarioMode = marioMode;
			//System.out.println("Ran into an enemy..");
			return "1";
		}
		return "0";
	}
	public String encodeKills()
	{
		String msg = "";
		//prevEnemiesStomped = environment.levelScene.getKillsByStomp();
		//prevEnemiesFireballed = environment.levelScene.getKillsByFire();
		if(environment.levelScene.getKillsByStomp()>prevEnemiesStomped)
			msg+="1";
		else
			msg+="0";
		
		if(environment.levelScene.getKillsByFire()>prevEnemiesFireballed)
			msg+="1";
		else
			msg+="0";
		
		return msg;
	}
	
	
	//encode the action to get to state from current_state
	public boolean [] encodeActionToState(String goalState)
	{
		boolean[] act = getEmptyAction();
		
		// should Mario shoot
		//System.out.print(goalState.charAt(goalState.length()-3) + " ");
		if(goalState.charAt(goalState.length()-2) == '1')
		{	// if he is fire mario, always shoot
			// this is consistent encoding from our hand agent that we developed originally
			//System.out.println(goalState);
			act[Mario.KEY_SPEED] = true;
			//System.out.println("Pew");
		}
		else
		{
			//System.out.println("He actually turned it off");
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
		progress = 0;
		for(int i=1; i<numEpisodes;i++)
		{
			
			// turn on visualization
			if(i%60 == 61 && i!=1) {
				task.setVisualization(true);
			} else {
				task.setVisualization(false);
			}
			
			
			// change mario mode every 20
			if(i%training_episodes == 0) {
				switch(progress){
				case 0:
					progress = 1; break;
				case 1:
					progress = 2; break;
				case 2:
					progress = 0; break;
				}
			}
			int prog = progress;
			
			/*
			if(i%500 == 0)
			{
				epsilon/=2;
			}
			*/
			
			// actually run the simulation
			task.evaluate(this);
			
			
			//if(i%training_episodes != training_episodes-1)
			//{
				System.out.printf("%d (%d):\t%f\t--QTable size: %d\n", i, prog, task.getEnvironment().getFitness(), Q.size());
			//}
			/*else
			{
				System.out.printf("marioMode(%d) - [%d] final training - \t%f\n", prog, i, task.getEnvironment().getFitness());
				
			}*/
			running = false;
			//System.out.println(progress);
		}
		System.out.println("Training Complete!");
		
		progress = 2;
		task.setVisualization(true);
		task.evaluate(this);
		System.out.printf("%d (%d):\t%f\t--QTable size: %d\n", numEpisodes, progress, task.getEnvironment().getFitness(), Q.size());
		
		//System.out.printf("%f\t%d\n", task.getEnvironment().getFitness(), numEpisodes);
		task.setVisualization(false);
		//System.out.printf("Size of the QTable: %d", Q.size());
	}
	
	protected void setFireMode()
	{
		environment.levelScene.mario.setMode(true,true);
	}
	
	protected void setBigMode()
	{
		environment.levelScene.mario.setMode(true,false);
	}
	
	protected void setSmallMode()
	{
		environment.levelScene.mario.setMode(false,false);
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
		this.numEpisodes = num;
		
	}

	@Override
	public Agent getBestAgent() {
		// TODO Auto-generated method stub
		//don't need this
		return null;
	}
}