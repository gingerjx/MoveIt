package searchclient;

import java.util.Arrays;

import java.util.ArrayList;
import java.util.HashSet;


public class SearchUtils {

    /*
     * Return the shortest path between agent - box - goal, where the distance all together is the
     * shortest agent - box and box - goals paths are separated by [-1, -1].
     * Example:
     * [0][1, 1, 1, 2, -1, -1, 1, 3, 1, 4, 1, 5] means that agent 0 has to go through (1,1), and (1,2) to reach
     * the box that is at (1, 3) and push it through (1, 4) to (1, 5)
     *
     * Else if there is no any agent - box - goal possibility, then the shortest agent - goal path is returned.
     * Path is returned in the form of array [startRow, startCol, row2, col2, row3, col3, ..., endRow, endCol]
     *
     * Else if there is no any agent - goal possibility, then [] is returned.
     *
     * Note that the subplans DON'T take into account obstacles (agents and boxes) on their way.
     * Also the subplans DON't exclude possible conflicts, these are completely individual subplans.
     */
    public static int[][] getSubPlans(State state) {
        int[][] subPlans = new int[LevelMetadata.MAX_NUM_OF_DISTINCT_AGENTS][0];
        int[][] chosenGoals = chooseSubGoals(state);
        for (int agent=0; agent<state.agentRows.length; agent++) {
            int agentRow = state.agentRows[agent];
            int agentCol = state.agentCols[agent];
            int[] agentGoals = chosenGoals[agent];

            if (agentGoals.length == 4) { // box i goal
                subPlans[agent] = getAgentBoxGoalPath(agentGoals, agentRow, agentCol);
            } else if (agentGoals.length == 2) { // goal sopt
                subPlans[agent] = getAgentGoalPath(agentGoals, agentRow, agentCol);
            }
        }

        return subPlans;
    }

    public static Action[][] aStarSearch(State initialState, State goalState, int prioritizedAgent,Conflict conflict) {
        HeuristicAStar.setGoalStateAndAgentAndConflict(goalState,prioritizedAgent,conflict);
        Frontier frontier = new FrontierBestFirst(new HeuristicAStar(initialState));
        frontier.add(initialState);
        HashSet<State> expanded = new HashSet<>();
        expanded.add(initialState);

        while (true) {

            if (frontier.isEmpty()) {
                return null;
            }

            State state = frontier.pop();

            if (state.equals(goalState)) {
                return state.extractPlan();
            }

            ArrayList<State> children = state.getExpandedStates();
            for (State child : children) {
                if (!expanded.contains(child)) {
                    frontier.add(child);
                    expanded.add(child);
                }
            }
        }
    }

    /*
     * If there is box goal:
     *  Return [agent][boxRow, boxCol, boxGoalRow, boxGoalCol], which are the coordinates of
     *  the chosen box and its closest, available goal spot coordinates for a specific agent.
     *  Example:
     *  [1][0, 0, 2, 2] - Agent 1 has chosen box at (0, 0), which has the closest goal at (2, 2).
     * Else if there is agent goal:
     *  Return [agent][agentGoalRow, agentGoalCol], which are the coordinates of the
     *  closest, available goal spot coordinates for a specific agent.
     *  Example:
     *  [1][0, 0] - Agent 1 has chosen going to the goal at (0, 0).
     * Else:
     *  Return [agent][], means agent has no any goal.
     * */
    private static int[][] chooseSubGoals(State state) {
        int[][] choices = new int[LevelMetadata.MAX_NUM_OF_DISTINCT_AGENTS][0];
        int[][] boxesGoals = findBoxesGoals(state);
        for (int agent=0; agent<state.agentRows.length; agent++) {
            choices[agent] = chooseBox(state, agent, boxesGoals);
            if (choices[agent].length <= 0) {
                choices[agent] = getAgentMinGoal(state, agent);
            }
        }
        return choices;
    }

    /*
     * Returns [box][boxRow, boxCol, boxGoalRow, boxGoalCol, ...], which are the coordinates of
     * the box and its closest goal spot coordinates.
     * Example:
     * [1][0, 0, 2, 2, 1, 3, 3, 1] - There are two boxes 'B' - (0, 0) with the closest goal at (2, 2) and
     * (1, 3) with the closest goal at (3, 1)
     * */
    private static int[][] findBoxesGoals(State state) {
        int[][] boxesGoals = new int[LevelMetadata.MAX_NUM_OF_DISTINCT_BOXES][0];
        for (int boxRow = 0; boxRow < state.boxes.length; boxRow++) {
            for (int boxCol = 0; boxCol < state.boxes[boxRow].length; boxCol++) {
                char box = state.boxes[boxRow][boxCol];
                if (box == 0) {
                    continue;
                }
                int boxInt = StateUtils.boxCharToInt(box);
                int[] boxGoalCoordinates = getBoxMinGoal(state, box, boxRow, boxCol);
                if (boxGoalCoordinates != null && boxGoalCoordinates.length == 2) {
                    boxesGoals[boxInt] = addBoxGoal(boxesGoals[boxInt], boxRow, boxCol, boxGoalCoordinates);
                }
            }
        }
        return boxesGoals;
    }

    /*
     * Returns [boxRow, boxCol, boxGoalRow, boxGoalCol], which are the coordinates of
     * the chosen box and its closest goal spot coordinates. Box is chosen based on the smallest
     * distance agent - box - goal.
     * Example:
     * [0, 0, 2, 2] - chosen box at (0, 0), which has the closest goal at (2, 2)
     * */
    private static int[] chooseBox(State state, int agent, int[][] boxesGoals) {
        int minDistance = LevelMetadata.MAP_AREA;
        Color agentColor = State.AGENT_COLORS[agent];
        char[] allowedBoxes = LevelMetadata.BOXES_PER_COLOR[agentColor.getValue()];
        int agentRow = state.agentRows[agent];
        int agentCol = state.agentCols[agent];
        int[] chosenBox = new int[0];

        for (char box : allowedBoxes) {
            int[] coordinates = boxesGoals[StateUtils.boxCharToInt(box)];
            for (int j = 0; j < coordinates.length; j += 4) {
                int boxRow = coordinates[j];
                int boxCol = coordinates[j+1];
                if (LevelMetadata.getPath(agentRow, agentCol, boxRow, boxCol) == null) {
                    continue;
                }

                int distance = (int)(0.4 * LevelMetadata.getDistance(agentRow, agentCol, boxRow, boxCol) +
                        0.6 * getDistance(Arrays.copyOfRange(coordinates, j, j+4)));
                if (distance < minDistance) {
                    minDistance = distance;
                    chosenBox = Arrays.copyOfRange(coordinates, j, j+4);
                }
            }
        }

        return chosenBox;
    }

    /*
     * Return the coordinates of the closest, free and relevant goal of the box. The choice is based
     * on LevelMetadata.getDistance(...)
     */
    private static int[] getBoxMinGoal(State state, char box, int boxRow, int boxCol) {
        int[] boxGoals = LevelMetadata.BOX_GOALS_COORDINATES[StateUtils.boxCharToInt(box)];
        int minDistance = LevelMetadata.MAP_AREA;
        int minGoalRow = -1;
        int minGoalCol = -1;

        for (int i=0; i<boxGoals.length; i += 2) {
            int goalRow = boxGoals[i];
            int goalCol = boxGoals[i+1];
            if (LevelMetadata.getPath(boxRow, boxCol, goalRow, goalCol) == null) {
                continue;
            }

            if (goalRow == boxRow && goalCol == boxCol) {
                return new int[0];
            }
            if (StateUtils.boxAt(state, goalRow, goalCol) == box){
                continue;
            }

            int distance = LevelMetadata.getDistance(boxRow, boxCol, goalRow, goalCol);
            if (distance < minDistance) {
                minDistance = distance;
                minGoalRow = goalRow;
                minGoalCol = goalCol;
            }
        }

        return getCoordinatesOrEmpty(minGoalRow, minGoalCol);
    }

    /*
     * Return the coordinates of the closest, free and relevant goal of the agent. The choice is based
     * on LevelMetadata.getDistance(...)
     */
    private static int[] getAgentMinGoal(State state, int agent) {
        int[] agentGoals = LevelMetadata.AGENT_GOALS_COORDINATES[agent];
        int minDistance = LevelMetadata.MAP_AREA;
        int minGoalRow = -1;
        int minGoalCol = -1;

        for (int i=0; i<agentGoals.length; i += 2) {
            int goalRow = agentGoals[i];
            int goalCol = agentGoals[i+1];
            int agentRow = state.agentRows[agent];
            int agentCol = state.agentCols[agent];
            if (LevelMetadata.getPath(agentRow, agentCol, goalRow, goalCol) == null) {
                continue;
            }

            if (goalRow == agentRow && goalCol == agentCol) {
                return new int[0];
            }
            if (StateUtils.agentAt(state, goalRow, goalCol) == StateUtils.agentIntToChar(agent)){
                continue;
            }

            int distance = LevelMetadata.getDistance(agentRow, agentCol, goalRow, goalCol);
            if (distance < minDistance) {
                minDistance = distance;
                minGoalRow = goalRow;
                minGoalCol = goalCol;
            }
        }

        return getCoordinatesOrEmpty(minGoalRow, minGoalCol);
    }

    /*
     * Return path from agent to box + from box to goal. Agent - box and box - goals paths are separated by [-1, -1].
     * Example:
     * [1, 1, 1, 2, -1, -1, 1, 3, 1, 4, 1, 5] means that agent has to go through (1,1), and (1,2) to reach
     * the box that is at (1, 3) and push it through (1, 4) to (1, 5)
     */
    private static int[] getAgentBoxGoalPath(int[] goals, int agentRow, int agentCol) {
        int boxRow = goals[0];
        int boxCol = goals[1];
        int goalRow = goals[2];
        int goalCol = goals[3];

        int[] agentBoxPath = LevelMetadata.getPath(agentRow, agentCol, boxRow, boxCol);
        int[] boxGoalPath = LevelMetadata.getPath(boxRow, boxCol, goalRow, goalCol);
        int agentBoxLen = agentBoxPath.length;
        int boxGoalLen = boxGoalPath.length;
        int[] path = new int[agentBoxLen + boxGoalLen];
        System.arraycopy(agentBoxPath, 0, path, 0, agentBoxLen - 2);
        System.arraycopy(new int[]{-1, -1}, 0, path, agentBoxLen - 2, 2);
        System.arraycopy(boxGoalPath, 0, path, agentBoxLen, boxGoalLen);
        return path;
    }

    /*
     * Return path from agent to goal
     */
    private static int[] getAgentGoalPath(int[] goals, int agentRow, int agentCol) {
        int goalRow = goals[0];
        int goalCol = goals[1];
        return LevelMetadata.getPath(agentRow, agentCol, goalRow, goalCol);
    }

    /*
     * Util method
     */
    private static int[] addBoxGoal(int[] currentGoals, int boxRow, int boxCol, int[] goalCoordinates) {
        int oldLength = currentGoals.length;
        int[] newBoxesGoals = Arrays.copyOf(currentGoals, oldLength + 4);
        newBoxesGoals[oldLength] = boxRow;
        newBoxesGoals[oldLength+1] = boxCol;
        newBoxesGoals[oldLength+2] = goalCoordinates[0];
        newBoxesGoals[oldLength+3] = goalCoordinates[1];
        return newBoxesGoals;
    }

    /*
     * Util method
     */
    private static int getDistance(int[] boxGoalCoordinates) {
        return LevelMetadata.getDistance(boxGoalCoordinates[0], boxGoalCoordinates[1], boxGoalCoordinates[2], boxGoalCoordinates[3]);
    }

    /*
     * Util method
     */
    private static int[] getCoordinatesOrEmpty(int row, int col) {
        if (row >= 0 && col >= 0) {
            return new int[]{row, col};
        }
        else {
            return new int[0];
        }
    }
}