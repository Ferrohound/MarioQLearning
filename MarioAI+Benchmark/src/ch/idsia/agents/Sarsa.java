package ch.idsia.agents;

import java.util.Hashtable;

public class Sarsa extends MarioReinforcementLearning implements LearningAgent {

	String previous_state;
	boolean [] previousAction;
	
	public Sarsa(String name) {
		super(name);
	}
	
	public boolean[] getAction()
	{
		if(!running)
		{
			switch(progress)
			{
			case 1:
				setSmallMode();
			break;
			case 2:
				setBigMode();
			break;
			case 3:
				setFireMode();
			break;
			}
			running = true;
		}
		
		//System.out.println("why?");
		if(testing_state_1)
		{
			//update the Q-Values
			previous_state = current_state;
			current_state = getState();
			if(training)
			{
				/* Get random value, if it is less than epsilon, then pick a random action
				 * Otherwise, follow the path - "do what you're supposed to"
				 */
				if( rand.nextDouble() < epsilon )
				{
					return getExplorationAction();
				}
				else
				{
					return getBestAction();
				}
			}
			else
			{
				/* choose the most rewarding action from the current state according to Q table
				 * and return that action from this function */
				return getBestAction();
			}
		}
		return null;
	}
	
	//replace getBestAction()
	//reward previous state instead of the current one
	public boolean[] getBestAction()
	{
		//System.out.println("Calling getBestAction for SARSA");
		giveRCurrent();
		
		//get the list of actions
		String[] possible_states = getStatesFromState(current_state);
		
		//iterate over all of the actions and check their rewards, set HighestState to
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
		//System.out.println("Figured out best action, now we have to update the previous Q table");
		//get the HASH of actions from that state
		//updateQTable(currentState, nextState);
		
		// GET THE HASHTABLE FOR THE PREVIOUS STATE'S Q-TABLE
		Hashtable<String, Double> prevStateQ = Q.get(previous_state);
		
		// if the previous state has never been added, initialize it
		if (prevStateQ == null)
		{
			Q.put(previous_state, new Hashtable<String, Double>());
			prevStateQ = Q.get(previous_state);
		}
		// if it's the first time going to the current state from the previous state, initialize it
		if(!prevStateQ.containsKey(current_state))
		{
			prevStateQ.put(current_state, initial_q_value);
		}
		
		

		Hashtable<String, Double> currentStateQ = Q.get(current_state);
		
		
		if (currentStateQ == null)
		{
			Q.put(current_state, new Hashtable<String, Double>());
			currentStateQ = Q.get(current_state);
		}
		if (!currentStateQ.containsKey(nextState))
		{
			currentStateQ.put(nextState, initial_q_value);
		}
		
		double Q_previous_to_current = prevStateQ.get(current_state);
		double Q_current_to_next = currentStateQ.get(nextState);

		//System.out.printf("Q_previous: %f\n", Q_previous_to_current);
		//System.out.println("Just printed prevStateQ");		
		
		
		//calculate the new Q value
		double R_current = initial_q_value;
		Q_previous_to_current += updateFormula(R_current, Q_current_to_next, Q_previous_to_current, calculateAlpha(previous_state, action));
		
		//newQ += alpha * (HighestReward + (gamma * getMaxQ(nextState)) - newQ);
		prevStateQ.put(current_state, Q_previous_to_current);
				
		action = act;
		//act[Mario.KEY_SPEED] = !act[Mario.KEY_SPEED];
		return act;
	}
	
	//new update function
	public double updateFormula(double reward, double Q_curr, double Q_prev, double alpha)
	{
		return alpha * (reward + gamma * Q_curr - Q_prev);
	}

}
