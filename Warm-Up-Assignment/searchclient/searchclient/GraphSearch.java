package searchclient;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/*
    Preprocess initial state using LevelMetadata and starts the search
 */
public class GraphSearch {

    public static Action[][] search(State initialState, Frontier frontier) {
        // Preprocessing of the map
        State state=LevelMetadata.initSplit(initialState); //Takes up to 100MB
        System.err.format("[Preprocessing] Time: %3.3f s Memory %s\n", (System.nanoTime() - startTime) / 1_000_000_000d, Memory.stringRep());
        Action[][] plan = new Action[0][initialState.numOfAgents];
        //State state = initialState;

        while (!state.isGoalState()) {

            // Get subPlans for each agent in state
            int[][] subPlansPath = SearchUtils.getSubPlans(state);
            Action[][] subPlanActions = PlanUtils.pathsToActions(subPlansPath);

            // Get prioritized agent base on number of obstacles
            int prioritizedAgent = PlanUtils.getPrioritizedAgentBasedOnNumberOfObstacles(subPlanActions, subPlansPath, state);

            // Empty non-prioritized subPlans
            PlanUtils.deleteNonPrioritizedSubPlans(prioritizedAgent, subPlanActions);

            Action[][] partialPlan = new Action[0][initialState.numOfAgents];
            int actionIndex = 0;
            while (true) {
                // Check if there is next conflict and get it in case
                Conflict conflict = Conflict.getNextConflict(state, subPlanActions[prioritizedAgent], actionIndex, prioritizedAgent);

                if (!conflict.isConflict) {
                    break;
                }

                // Add part of sub plan actions to partialPlan. The part corresponds to actions until obstacle is encountered
                Action[][] subPlanPart = copy2DColumnRangeOf(subPlanActions, actionIndex, conflict.numOfAction);
                subPlanPart = PlanUtils.alignPlans(subPlanPart, state.numOfAgents);
                partialPlan = PlanUtils.appendActionsToPlan(partialPlan, subPlanPart);
                actionIndex = conflict.numOfAction;

                // Finds the first free cells on the path for the agent and its box if necessary
                State postConflictState = conflict.getPostConflictState(subPlanActions[prioritizedAgent], actionIndex, prioritizedAgent);
                actionIndex += conflict.numberOfSkippedActions;

                // Find conflict resolution actions and add them to partialPlan
                Action[][] resolutionActionsPlan = SearchUtils.aStarSearch(conflict.conflictState, postConflictState, prioritizedAgent, conflict);
                partialPlan = PlanUtils.appendActionsToPlan(partialPlan, resolutionActionsPlan);

                // Update state after sub plan part + resolution actions
                state = StateActionUtils.getUpdatedState(conflict.conflictState, resolutionActionsPlan);
            }

            // Add remaining actions after resolving all the conflicts
            Action[][] subPlanPart = copy2DColumnRangeOf(subPlanActions, actionIndex, subPlanActions[prioritizedAgent].length);
            subPlanPart = PlanUtils.alignPlans(subPlanPart, state.numOfAgents);
            partialPlan = PlanUtils.appendActionsToPlan(partialPlan, subPlanPart);
            plan = PlanUtils.appendActionsToPlan(plan, partialPlan);

            // Update state after sub plan part
            state = StateActionUtils.getUpdatedState(state, subPlanPart);
        }

        return plan;
    }

    private final static long startTime = System.nanoTime();

    public static Action[][] copy2DColumnRangeOf(Action[][] src, int from, int to) {
        int newLen = to - from;
        Action[][] dest = new Action[src.length][0];

        for (int i=0; i < src.length; i++) {
            dest[i] = new Action[Math.min(src[i].length, newLen)];

            for (int j=from, k=0; j < to; j++, k++) {
                if (j >= src[i].length) {
                    break;
                }
                dest[i][k] = src[i][j];
            }
        }

        return dest;
    }

    /*
        printStream = System.err - cannot be read from the console
        printStream = System.out - can be read from the console
     */
    private static void printSearchStatus(HashSet<State> expanded, Frontier frontier, PrintStream printStream)
    {
        String statusTemplate = "#Expanded: %,8d, #Frontier: %,8d, #Generated: %,8d, Time: %3.3f s\n%s\n";
        double elapsedTime = (System.nanoTime() - startTime) / 1_000_000_000d;
        printStream.format(statusTemplate, expanded.size(), frontier.size(), expanded.size() + frontier.size(),
                          elapsedTime, Memory.stringRep());
    }
}