package ch.idsia.agents;
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
	
		
	
	public MarioReinforcementLearning(String name)
	{
		super(name);

		previous_pos = marioFloatPos;
		
		
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
		state += encodeMarioGround() + "|";
		state += encodeMarioJump() + "|";
		state += encodeNearbyEnemies() + "|";
		return state;
	}
	
	public boolean[] getBestAction()
	{
		boolean[] act = getEmptyAction();
		// TO DO: Make it actually choose its best action from the Q table
		
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