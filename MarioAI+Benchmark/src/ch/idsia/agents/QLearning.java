package ch.idsia.agents;

public class QLearning extends MarioReinforcementLearning implements LearningAgent{

	public QLearning(String name)
	{
		super(name);
	}
	
	//@override
	public double updateFormula(String next, double reward, double q, double alpha)
	{
		return alpha * (reward + (gamma * getMaxQ(next)) - q);
	}
	
	public boolean[] getAction()
	{
		if(!running)
		{
			switch(progress)
			{
			case 0:
				setSmallMode();
			break;
			case 1:
				setBigMode();
			break;
			case 2:
				setFireMode();
			break;
			}
			running = true;
		}
		
		//System.out.println("why?");
		if(testing_state_1)
		{
			//update the Q-Values
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
}
