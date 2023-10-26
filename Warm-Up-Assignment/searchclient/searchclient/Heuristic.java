package searchclient;

import java.util.Comparator;

public abstract class Heuristic
        implements Comparator<State>
{
    private HeuristicFunctionUtils heuristicFunction = new HeuristicFunctionUtils();

    public Heuristic(State initialState)
    {
    }

    public int h(State s)
    {
        // With such a 'heuristic' it behaves like BFS, we should think about relevant heuristics
        return 0;
    }

    public abstract int f(State s);

    @Override
    public int compare(State s1, State s2)
    {
        return this.f(s1) - this.f(s2);
    }

}

class HeuristicAStar
        extends Heuristic
{
    private static State goalState;
    private static int prioritizedAgent;
    private static Conflict conflictObject;

    private HeuristicFunctionUtils heuristicFunction = new HeuristicFunctionUtils();
    public HeuristicAStar(State initialState)
    {
        super(initialState);
    }
    public static void setGoalStateAndAgentAndConflict(State goal, int agent, Conflict conflict) {
        goalState = goal;
        prioritizedAgent = agent;
        conflictObject = conflict;

    }
    @Override
    public int h(State initialState) {
        // Returns the distance between agent that has a conflict and the cell that he has to move to.
        int agentGoalDistance = heuristicFunction.getAgentGoalDistance(initialState, goalState, prioritizedAgent);

        int numberOfUnsatisfiedGoals = heuristicFunction.getNumberOfUnsatisfiedGoals(initialState);

        // Heuristic 2 - get closest helper agents' distances
        // int minimumClosestAgentsDistances = heuristicFunction.getMinimumDistanceForConflictAndAgent(initialState, conflictObject.obstacleArea, prioritizedAgent);
        // System.err.format("minimumClosestAgentsDistances " + minimumClosestAgentsDistances);
        
        //Heuristic 3 - get the number of obstacles in the obstacle area
        int numberOfObstacles = heuristicFunction.getNumberOfObstaclesOnPath(initialState, conflictObject.obstacleArea, prioritizedAgent, conflictObject);
        // System.err.format("numberOfObstacles " + numberOfObstacles);
        
        return agentGoalDistance + numberOfUnsatisfiedGoals + numberOfObstacles;
    }

    @Override
    public int f(State s)
    {
        return s.g() + 2 * this.h(s);
    }

    @Override
    public String toString()
    {
        return "A* evaluation";
    }
}

class HeuristicWeightedAStar
        extends Heuristic
{
    private int w;

    public HeuristicWeightedAStar(State initialState, int w)
    {
        super(initialState);
        this.w = w;
    }

    @Override
    public int f(State s)
    {
        return s.g() + this.w * this.h(s);
    }

    @Override
    public String toString()
    {
        return String.format("WA*(%d) evaluation", this.w);
    }
}

class HeuristicGreedy
        extends Heuristic
{
    public HeuristicGreedy(State initialState)
    {
        super(initialState);
    }

    @Override
    public int f(State s)
    {
        return this.h(s);
    }

    @Override
    public String toString()
    {
        return "greedy evaluation";
    }
}
