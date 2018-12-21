package cmsc433_p4;


import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This file needs to hold your solver to be tested. 
 * You can alter the class to extend any class that extends MazeSolver.
 * It must have a constructor that takes in a Maze.
 * It must have a solve() method that returns the datatype List<Direction>
 *   which will either be a reference to a list of steps to take or will
 *   be null if the maze cannot be solved.
 */

public class StudentMTMazeSolver extends SkippingMazeSolver
{
    public StudentMTMazeSolver(Maze maze)
    {
        super(maze);
    }

    public List<Direction> solve()
    {
        // TODO: Implement your code here
        //This is to check how many processors available in JVM
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        // Initial the thread pool
        ExecutorService threadPool = Executors.newFixedThreadPool(availableProcessors);
        //Using Callable to track the return result(Runnable not return)
        List<Callable<List<Direction>>> tasks = new LinkedList<>();
        try {
            Choice startPositon = firstChoice(maze.getStart());
            while (!startPositon.choices.isEmpty()) {
                tasks.add(new MyDFS(follow(startPositon.at, startPositon.choices.peek()),startPositon.choices.pop()));
            }
        } catch (SkippingMazeSolver.SolutionFound e) {
            System.out.println("No possible solution exists.");
        }

        //List solutions to track the possible results
        List<Direction> solutions = null;
        try {
            for (int i = 0; i < tasks.size(); i++) {
                solutions = threadPool.submit(tasks.get(i)).get();
                if (solutions != null) {
                	System.out.println("The length of this solution is: " + solutions.size() );
					break;
				}
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        //Shutdown this thread pool
        threadPool.shutdown();
        return solutions;
        //throw new RuntimeException("Not yet implemented!");
    }

    //Customize My DFS, implemets Callable
    private class MyDFS implements Callable<List<Direction>> {

		public Direction firstDirection;
		public Choice startPosition;

		public MyDFS(Choice startPosition, Direction firstDirection) {
			this.startPosition = startPosition;
                        this.firstDirection = firstDirection;
		}

		@Override
		public List<Direction> call() {
			LinkedList<Choice> stackForChoices = new LinkedList<>();
			Choice choice;
			int numberOfChoices = 0;

			try {
				stackForChoices.push(this.startPosition);
				while (!stackForChoices.isEmpty()) {
					choice = stackForChoices.peek();
					if (choice.isDeadend()) {
						//add number of choices
						numberOfChoices++;
						stackForChoices.pop();
						if (!stackForChoices.isEmpty())
							stackForChoices.peek().choices.pop();
						continue;
					}
					stackForChoices.push(follow(choice.at, choice.choices.peek()));
				}
				//This occurs when there is no solution
				return null;
			} catch (SolutionFound e) {
				Iterator<Choice> iterator = stackForChoices.iterator();
				LinkedList<Direction> path = new LinkedList<>();
				//Iterate the whole stack
				while (iterator.hasNext()) {
					choice = iterator.next();
					path.push(choice.choices.peek());
				}
				path.push(this.firstDirection);

				if (maze.display != null)
					maze.display.updateDisplay();
				System.out.println("The number of choices that the search performs for this solution is: " + numberOfChoices);
				
				return pathToFullPath(path);
			}
		}
	}
}
