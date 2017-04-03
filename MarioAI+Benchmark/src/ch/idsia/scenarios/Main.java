package ch.idsia.scenarios;

import ch.idsia.benchmark.tasks.BasicTask;
import ch.idsia.benchmark.tasks.ProgressTask;
import ch.idsia.tools.CmdLineOptions;

/**
 * Created by IntelliJ IDEA. User: Sergey Karakovskiy, sergey at idsia dot ch Date: Mar 17, 2010 Time: 8:28:00 AM
 * Package: ch.idsia.scenarios
 */
public final class Main
{
public static void main(String[] args)
{
//        final String argsString = "-vis on";
    final CmdLineOptions cmdLineOptions = new CmdLineOptions(args);
//        final Environment environment = new MarioEnvironment();
//        final Agent agent = new ForwardAgent();
//        final Agent agent = cmdLineOptions.getAgent();
//        final Agent a = AgentsPool.load("ch.idsia.controllers.agents.controllers.ForwardJumpingAgent");
    final ProgressTask task = new ProgressTask(cmdLineOptions);
//  for (int i = 0; i < 10; ++i)
//  {
//      int seed = 0;
//      do
//      {
//          cmdLineOptions.setLevelDifficulty(i);
//          cmdLineOptions.setLevelRandSeed(seed++);
/* basicTask.reset(cmdLineOptions);
basicTask.runOneEpisode();
System.out.println(basicTask.getEnvironment().getEvaluationInfoAsString());
//      } while (basicTask.getEnvironment().getEvaluationInfo().marioStatus != Environment.MARIO_STATUS_WIN);
//  }
//
* */
//task.reset(cmdLineOptions);
//task.runOneEpisode();
//task.reset(cmdLineOptions);
    System.out.println(task.getEnvironment().getEvaluationInfoAsString());
    System.exit(0);
}

}
