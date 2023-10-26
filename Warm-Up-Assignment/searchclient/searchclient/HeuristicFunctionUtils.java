package searchclient;

import java.util.List;

/*
    Defines heuristic functions that can be used for AStar
 */
public class HeuristicFunctionUtils {


    public HeuristicFunctionUtils() {}

    public int getAgentsGoalsDistance(State state) {
        if (!LevelMetadata.hasAgentGoals()) {
            return 0;
        }
        int agentsDistance = 0;

        for (int agentIndex = 0; agentIndex < state.agentRows.length; agentIndex++) {
            int agentRow = state.agentRows[agentIndex];
            int agentCol = state.agentCols[agentIndex];
            agentsDistance += getMinimumDistance(agentIndex, agentRow, agentCol, LevelMetadata.AGENT_GOALS_COORDINATES);
        }

        return agentsDistance;
    }

    public int getAgentGoalDistance(State state, State goalState, int agent) {
        int agentRow = state.agentRows[agent];
        int agentCol = state.agentCols[agent];
        int goalRow = goalState.agentRows[agent];
        int goalCol = goalState.agentCols[agent];

        return LevelMetadata.getDistance(agentRow, agentCol, goalRow, goalCol);
    }

    public int getNumberOfUnsatisfiedGoals(State state) {
        int allGoalsNumber = LevelMetadata.AGENT_GOALS_NUMBER + LevelMetadata.BOX_GOALS_NUMBER;
        int satisfiedGoals = 0;

        for (int i=0; i<state.numOfAgents; i++) {
            int agentRow = state.agentRows[i];
            int agentCol = state.agentCols[i];
            int[] goalsCoordinates = LevelMetadata.AGENT_GOALS_COORDINATES[i];

            for (int j=0; j<goalsCoordinates.length; j += 2) {
                int goalRow = goalsCoordinates[j];
                int goalCol = goalsCoordinates[j+1];
                if (StateUtils.isSameCell(agentRow, agentCol, goalRow, goalCol)) {
                    satisfiedGoals++;
                }
            }
        }

        for (int i=0; i<LevelMetadata.BOX_GOALS_COORDINATES.length; i++) {
            char box = (char)(i + StateUtils.MIN_BOX_CHAR);
            int[] boxCoordinates = LevelMetadata.BOX_GOALS_COORDINATES[i];

            for (int j=0; j<boxCoordinates.length; j += 2) {
                int boxRow = boxCoordinates[j];
                int boxCol = boxCoordinates[j+1];
                if (state.boxes[boxRow][boxCol] == box) {
                    satisfiedGoals++;
                }
            }

        }

        return allGoalsNumber - satisfiedGoals;
    }

    public int getBoxesGoalsDistance(State state) {
        if (!LevelMetadata.hasBoxGoals()) {
            return 0;
        }

        int boxesDistance = 0;

        for (int boxRow = 0; boxRow < state.boxes.length; boxRow++) {
            for (int boxCol = 0; boxCol < state.boxes[boxRow].length; boxCol++) {
                char box = state.boxes[boxRow][boxCol];
                if (box == 0) {
                    continue;
                }
                boxesDistance += getMinimumDistance(StateUtils.boxCharToInt(box), boxRow, boxCol, LevelMetadata.BOX_GOALS_COORDINATES);
            }
        }

        return boxesDistance;
    }

    private int getMinimumDistance(int object, int row, int col, int[][] goals) {
        int minDistance = LevelMetadata.MAP_ROWS * LevelMetadata.MAP_COLS;
        for (int i=0; i < goals[object].length; i += 2) {
            int goalRow = goals[object][i];
            int goalCol = goals[object][i+1];
            if (goalRow < 0 || goalCol < 0) {
                break;
            }
            int distance = LevelMetadata.getDistance(row, col, goalRow, goalCol);
            minDistance = Math.min(minDistance, distance);
        }
        return minDistance;
    }

    private int getClosestSameColorAgent(State state, int obstacleRow, int obstacleCol, int prioritizedAgent) {

        int minDistance = -1;
        int chosenAgent = -1;
        
        //get box
        char box = StateUtils.boxAt(state, obstacleRow, obstacleCol);

        //get agents of same color
        boolean[] agents = StateUtils.agentsForBox(state, box);

        //get distances to agents
        for (int i = 0; i < agents.length; i++) {
            
            if (agents[i] && i!= prioritizedAgent){

                int agentRow = state.agentRows[i];
                int agentCol = state.agentCols[i];

                int distance=LevelMetadata.getDistance(agentRow, agentCol, obstacleRow, obstacleCol);

                if(chosenAgent == -1){ //first iteration
                    minDistance = distance;
                    chosenAgent = i;
                }

                if(distance < minDistance){
                    minDistance = distance;
                    chosenAgent = i;
                }
            }                
        }

        return chosenAgent;        
    }

    public int getMinimumDistanceForConflictAndAgent(State state, List<Coordinates> obstacles, int prioritizedAgent) {
        int sumOfDistances = 0;
        for (int i = 0; i < obstacles.size(); i ++) {
            int obstacleRow = obstacles.get(i).row;
            int obstacleCol = obstacles.get(i).col;
            if(StateUtils.cellIsFree(state, obstacleRow, obstacleCol)){
                continue;
            }
            char box = StateUtils.boxAt(state, obstacleRow, obstacleCol);
            if(StateUtils.isBox(box)){
                int closestAgent = getClosestSameColorAgent(state,obstacleRow,obstacleCol, prioritizedAgent);        
                int agentRow = state.agentRows[closestAgent];
                int agentCol = state.agentCols[closestAgent];
                int distance = LevelMetadata.getDistance(obstacleRow, obstacleCol, agentRow, agentCol);
                sumOfDistances += distance;
            }
        }
        
        return sumOfDistances;
    }

    public int getNumberOfObstaclesOnPath(State state, List<Coordinates> obstacles,int prioritizedAgent, Conflict conflict){
        int numOfObstacles = 0;
         boolean isAgentCarryingBox = false;
         char agentBox ='A';
         Color agentBoxColor = null;
        //  if(conflict.initialBoxCoords != null && conflict.finalBoxCoords !=null){
        //      isAgentCarryingBox = true;
        //      agentBox = StateUtils.boxAt(conflict.conflictState, conflict.initialBoxCoords.row, conflict.initialBoxCoords.col);
        //      // System.err.format("agentBox " + agentBox + "end ");

        //      agentBoxColor = State.BOX_COLORS[StateUtils.boxCharToInt(agentBox)];
        //  }
        
        for (int i = 0; i < obstacles.size(); i ++) {
            int obstacleRow = obstacles.get(i).row;
            int obstacleCol = obstacles.get(i).col;
            
            if(StateUtils.cellIsFree(state, obstacleRow, obstacleCol)){
                continue;
            }

            char box = StateUtils.boxAt(state, obstacleRow, obstacleCol);
            if(StateUtils.isBox(box)){
                numOfObstacles +=1;
                continue;
            }

            int agentRow = state.agentRows[prioritizedAgent];
            int agentCol = state.agentCols[prioritizedAgent];

            if(agentRow == obstacleRow && agentCol == obstacleCol){
                continue;
            }else{ // not a free cell & not a box & not the priortized agent -> different agent 
                numOfObstacles +=1;
            }
        }

        //  if(isAgentCarryingBox){
        //      char potentialAgentBox = StateUtils.boxAt(state, conflict.initialBoxCoords.row, conflict.initialBoxCoords.col);
        //      if(!StateUtils.isBox(potentialAgentBox)){ // box is not at the initial position
        //          char potentialFinalBox = StateUtils.boxAt(state, conflict.finalBoxCoords.row, conflict.finalBoxCoords.col);

        //          if(StateUtils.isBox(potentialFinalBox)){ // some similar box is at the final, at this stage the agent's box might be on the path an
        //                                                  // should not be counted as an obstacle. If the agent's
        //                                                  // box is already on the final coordinates, the conflict resolution stops and
        //                                                  // wont affect the heuristic as the iteration will be over by then.
        //                  Color potentialFinalBoxColor = State.BOX_COLORS[StateUtils.boxCharToInt(potentialFinalBox)];
        //                  if(potentialFinalBox == agentBox && potentialFinalBoxColor == agentBoxColor){
        //                  numOfObstacles -=1;
        //              }
        //          }
        //      }
        //  }

        return numOfObstacles;
    }

}
