package searchclient;

import java.util.*;

public class Obstacles extends ArrayList<List<Integer>> {

    /**
     * Find the obstacles (other agents and boxes) in the path of the prioritized
     * agent.
     *
     * Example:
     * Given the following sub-plan path [1, 1, 1, 2, -1, -1, 1, 3, 1, 4, 1, 5] and
     * the prioritized agent index,
     * this method checks if there are any agents or boxes on the sub-path that
     * could cause a conflict.
     *
     * It skips coordinates [-1,-1], box coordinates and any duplicates.
     *

     * @return A list of obstacles, each represented by a list containing the row
     *         and column coordinates.
     *         Example: [[1, 2], [1, 3]]
     */

    public static Obstacles findObstacles(int prioritizedAgent, int[][] subPlansPath, State state) {
        Obstacles obstacles = new Obstacles();


        // "Next iteration" is one after [-1, -1] coordinates (the box of prioritized
        // agent)
        boolean skipNextIteration = false;

        int rowToBeSkipped = 0;
        int colToBeSkipped = 0;

        for (int i = 0; i < subPlansPath[prioritizedAgent].length; i += 2) {
            int row = subPlansPath[prioritizedAgent][i];
            int col = subPlansPath[prioritizedAgent][i + 1];

            if (skipNextIteration) {
                skipNextIteration = false;
                continue;
            }

            // Lets skip the box coordinates
            if (row == -1 && col == -1) {
                skipNextIteration = true;
                rowToBeSkipped = subPlansPath[prioritizedAgent][i + 2];
                colToBeSkipped = subPlansPath[prioritizedAgent][i + 3];
                continue;
            }

            // Skip the coordinates that match rowToBeSkipped and colToBeSkipped
            if (row == rowToBeSkipped && col == colToBeSkipped) {

                continue;
            }

            // Check if there is any agent on the way of the prioritizedAgent
            for (int agent = 0; agent < state.numOfAgents; agent++) {
                if (agent == prioritizedAgent) {
                    continue;
                }
                if (row == state.agentRows[agent] && col == state.agentCols[agent]) {
                    List<Integer> newObstacle = Arrays.asList(row, col);
                    if (!obstacles.contains(newObstacle)) {
                        obstacles.add(newObstacle);
                    }

                }
            }

            // Check if there is any box on the way of the prioritizedAgent
            if (state.boxes[row][col] != 0) {
                List<Integer> newObstacle = Arrays.asList(row, col);
                if (!obstacles.contains(newObstacle)) {
                    obstacles.add(newObstacle);
                }
            }
        }

        return obstacles;
    }
    public static List<Integer> findAgentsByObstacles(Action[][] plans, int[][] subPlansPath, State state) {
        List<Integer> agents = new ArrayList<>();
        int minObstacles = Integer.MAX_VALUE;

        for (int agent = 0; agent < plans.length; agent++) {
            if (plans[agent].length > 0) {
                Obstacles obstacles = findObstacles(agent, subPlansPath, state);
                int numObstacles = obstacles.size();

                if (numObstacles < minObstacles) {
                    minObstacles = numObstacles;
                    agents.clear();
                    agents.add(agent);
                } else if (numObstacles == minObstacles) {
                    agents.add(agent);
                }
            }
        }

        return agents;
}

    public boolean isObstaclesEmpty() {
        return this.isEmpty();
    }

    public static int[] getClosestCellForObstacles(int prioritizedAgent, int[][] subPlansPath, State state,
            Obstacles agentObstacles) {
        /**
         * Get the closest free cell for the obstacles (other agents and boxes) in the
         * path of the prioritized agent.
         * 
         * Example:
         * Given the following coordinates [[1, 2], [1, 3]] as obstacles and prioritized
         * agent N and that agent's
         * subpath, the method will look for free cells by iteratively checking the 4
         * immediate neighbours of each subPath Cell
         * with increasing distance to them. This checking starts from the start of the
         * path of the agent to its goal
         * or however the subPath is supplied to this method.
         * 
         * The increasing distance means that the 4 immediate neighbours are initially
         * at a distance of 0,
         * i.e directly adjacent to the path cell.
         * If a free cell is not found for each of the obstacle then the increasing
         * distance is incremented by 1.
         * 
         * @return A list of obstacles and a free cell for each of them.
         *         Example: [1, 2, 1, 3] - the obstacle at (1,2) is to be moved to
         *         (1,3),(1,3) is a free cell allotted to this obstacle.
         */
        List<List<Integer>> pathCells = new ArrayList<>(); // [[1,1], [2,3]] path cells formatted as [row,col]
        List<List<Integer>> obstaclesToFreeCellsMapping = new ArrayList<>(); // [[2,3,5,6], [7,8,9,10]] - obstacle at
                                                                             // (2,3) should be at (5,6)
        List<List<Integer>> freeCells = new ArrayList<>(); // [[5,6], [9,10]] - free cells found along the subpath
        int obstacleRow, obstacleCol;
        for (int i = 0; i < subPlansPath[prioritizedAgent].length; i += 2) {
            int row = subPlansPath[prioritizedAgent][i];
            int col = subPlansPath[prioritizedAgent][i + 1];
            pathCells.add(Arrays.asList(row, col));
        }

        for (int levelCheckCounter = 0; (levelCheckCounter < LevelMetadata.MAP_ROWS)
                || (levelCheckCounter < LevelMetadata.MAP_COLS); // not to go out of bounds
                levelCheckCounter++) {

            for (int i = 0; i < subPlansPath[prioritizedAgent].length; i += 2) {
                int row = subPlansPath[prioritizedAgent][i];
                int col = subPlansPath[prioritizedAgent][i + 1];
                freeCells = addFreeCellIfValid(state, freeCells, pathCells, row - levelCheckCounter, col);
                freeCells = addFreeCellIfValid(state, freeCells, pathCells, row + levelCheckCounter, col);
                freeCells = addFreeCellIfValid(state, freeCells, pathCells, row, col - levelCheckCounter);
                freeCells = addFreeCellIfValid(state, freeCells, pathCells, row, col + levelCheckCounter);
            }
            if (freeCells.size() == agentObstacles.size()) {
                break;
            }
        }

        List<Integer> freeCellsAllotted = new ArrayList<>();
        for (List<Integer> obstacle : agentObstacles) {
            int leastDistance = -1;
            int[] leastDistanceCoordinates = new int[2];
            obstacleRow = obstacle.get(0);
            obstacleCol = obstacle.get(1);
            int currentChosenFreeCell = 0;
            for (int i = 0; i < freeCells.size(); i++) {
                if (!freeCellsAllotted.contains(i)) {
                    int freeCellRow = freeCells.get(i).get(0);
                    int freeCellCol = freeCells.get(i).get(1);
                    int currentDistance = LevelMetadata.getDistance(obstacleRow, obstacleCol, freeCellRow, freeCellCol);
                    if (leastDistance == -1) {
                        leastDistance = currentDistance;
                        leastDistanceCoordinates[0] = freeCellRow;
                        leastDistanceCoordinates[1] = freeCellCol;
                        continue;
                    }

                    if (currentDistance < leastDistance) {
                        leastDistance = currentDistance;
                        leastDistanceCoordinates[0] = freeCellRow;
                        leastDistanceCoordinates[1] = freeCellCol;
                        currentChosenFreeCell = i;
                    }
                }
            }
            freeCellsAllotted.add(currentChosenFreeCell);
            leastDistance = -1;
            obstaclesToFreeCellsMapping.add(
                    Arrays.asList(obstacleRow, obstacleCol, leastDistanceCoordinates[0], leastDistanceCoordinates[1]));
        }
        return flattenList(obstaclesToFreeCellsMapping);

    }

    public static List<List<Integer>> addFreeCellIfValid(State state, List<List<Integer>> freeCells,
            List<List<Integer>> pathCells, int row, int col) {
        if (!(StateUtils.isOutOfBounds(row, col)) // not out of map
                && (StateUtils.cellIsFree(state, row, col))// is free
                && (!pathCells.contains(Arrays.asList(row, col))) // not on path
                && (!freeCells.contains(Arrays.asList(row, col)))) {

            freeCells.add(Arrays.asList(row, col));
        }
        return freeCells;
    }

    public static int[] flattenList(List<List<Integer>> outerList) {
        return outerList.stream()
                .flatMapToInt(list -> list.stream().mapToInt(Integer::intValue))
                .toArray();
    }

    public static State createStateForBFS(State state, int[] obstaclesToFreeCellsMapping) {

        /**
         * Creates the state by 'moving' the obstacles to the free cells. Agents and boxes
         * are moved accordingly.
         * path of the prioritized agent.
         * 
         * Example:
         * Given the following coordinates [1, 2, 1, 3] the obstacle at (1,2) is moved at (1,3)
         * 
         * @return State 
         *     The altered State where the obstacles have been moved to the free cells.
         * 
         */

        State newState = new State(state);

        for (int i = 0; i < obstaclesToFreeCellsMapping.length; i += 4) {
            int obstacleRow = obstaclesToFreeCellsMapping[i];
            int obstacleCol = obstaclesToFreeCellsMapping[i + 1];
            int freeCellRow = obstaclesToFreeCellsMapping[i + 2];
            int freeCellCol = obstaclesToFreeCellsMapping[i + 3];

            if (StateUtils.boxAt(newState, obstacleRow, obstacleCol) == 0) { // obstacle is not a box therefore its an
                                                                          // agent
                char agentObstacleChar = StateUtils.agentAt(newState, obstacleRow, obstacleCol);
                int agentObstacle = StateUtils.agentCharToInt(agentObstacleChar);
                newState.agentRows[agentObstacle] = freeCellRow;
                newState.agentCols[agentObstacle] = freeCellCol;
            } else {
                char boxObstacle = StateUtils.boxAt(newState, obstacleRow, obstacleCol);
                newState.boxes[obstacleRow][obstacleCol] = 0;
                newState.boxes[freeCellRow][freeCellCol] = boxObstacle;
            }
        }
        // System.err.format("Constructed State:" + state.toString());
        return newState;
    }
}
