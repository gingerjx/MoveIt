package searchclient;

import java.util.*;

/*
    Persist meta information about the level. Use init to compute the information.
    Use public fields or methods to retrieve needed information.
    !!!Remember that this preprocessing doesn't take into account the boxes and the agents,
    It treats them like they don't exist!!!
    E.g. such a map:
    ++++++
    +0A B++
    +1    +
    +++++++
    Will be processed as
    ++++++
    +    ++
    +     +
    +++++++
 */
public class LevelMetadata {

    public static int MAP_ROWS;
    public static int MAP_COLS;
    public static int MAP_AREA;
    public static int[][] MAPSPLIT;
    public static int MAX_NUM_OF_DISTINCT_AGENTS = StateUtils.MAX_AGENT_CHAR - StateUtils.MIN_AGENT_CHAR + 1;
    public static int MAX_NUM_OF_DISTINCT_BOXES = StateUtils.MAX_BOX_CHAR - StateUtils.MIN_BOX_CHAR + 1;
    /*
     * Number of all agent goals
     */
    public static int AGENT_GOALS_NUMBER = 0;
    /*
     * Number of all box goals
     */
    public static int BOX_GOALS_NUMBER = 0;
    /*
     * Number of goals of specific agent e.g. AGENT_GOALS_NUM[1] = 5 - means that there are 5 goal spots for agent 1
     */
    public final static int[] AGENT_GOALS_NUM = new int[MAX_NUM_OF_DISTINCT_AGENTS];
    /*
     * Number of goals of specific box e.g. BOX_GOALS_NUM[1] = 5 - means that there are 5 goal spots for box 'B'
     */
    public final static int[] BOX_GOALS_NUM = new int[MAX_NUM_OF_DISTINCT_BOXES];
    /*
    * E.g. AGENT_GOALS_COORDINATES[1] = [4,2,1,2]
    * means that agent 1 has goals at coords (4,2) and (1,2).
    */
    public static int[][] AGENT_GOALS_COORDINATES;
    /*
     * E.g. BOX_GOALS_COORDINATES[1] = [4,2,1,2]
     * means that box 'B' has goals at coords (4,2) and (1,2).
     */
    public static int[][] BOX_GOALS_COORDINATES;
    /*
     * Contains agent indexes for each color
     * E.g. AGENTS_PER_COLOR[Color.Blue.getValue()] = [1,3,5]
     */
    public static HashMap<MapStructurePreprocessor.Coordinates,Integer> goalRank = new HashMap<>();
    public static int[][] AGENTS_PER_COLOR;
    /*
     * Contains box indexes for each color
     * E.g. BOXES_PER_COLOR[Color.Blue.getValue()] = [A,C,F]
     */
    public static char[][] BOXES_PER_COLOR;
    /*
     * Contains real distances between two points e.g. DISTANCES[2][3][1][4] = 12
     * It means that real distance between (2,3) and (1,4) is 12
     */
    private static int[][][][] DISTANCES;
    /*
     * PATHS_ROW AND PATHS_COL together persist the coordinate of the next cell, which lead
     * from one cell to the other e.g. PATHS_ROW[1][1][3][1] = 1 and PATHS_COL[1][1][3][1] = 2
     * means that if you want to get from (1, 1) to (3, 1) the next cell of the shortest path is (1, 2).
     * You can retrieve it using getNextCell(...) method.
     *
     * It is separated into two 4D tables instead of 5D due to memory issues.
     *
     * Real path from (x, y) to (z, w) can be retrieved by recursively using getNextCell(...)
     * E.g. get shortest path between (1, 1) and (3, 1)
     * [1, 2] = getNextCell(1, 1, 3, 1)
     * [2, 2] = getNextCell(1, 2, 3, 1)
     * [3, 2] = getNextCell(2, 2, 3, 1)
     * [3, 1] = getNextCell(3, 2, 3, 1)
     * The shortest path (1, 1) -> (1, 2) -> (2, 2) -> (3, 2) -> (3, 1)
     */
    private static int[][][][] PATHS_ROW;
    private static int[][][][] PATHS_COL;
    /*
    Coordinates of the cells that have 3 or 4 non-wall cells around.
    Example:
    ++++++
    +    +
    + ++++
    +  A +
    + ++0+
    +  + +
    +  + +
    ++++++
    BRANCH_CELLS = [3, 1, 5, 1] -> cells (3, 1) and (5, 1) are branch cells
     */
    public static int[] BRANCH_CELLS;

    /*
     * CORRIDORS is a 3D integer array representing a collection of corridors.
     * Each corridor is a 2D integer array containing the coordinates of the cells in the corridor.
     * For example:
     * CORRIDORS = [
     *     [
     *         [1, 2],
     *         [1, 3],
     *         [1, 4]
     *     ],
     *     [
     *         [3, 1],
     *         [3, 2],
     *         [3, 3]
     *     ]
     * ]
     * This example shows two corridors, one with cells (1, 2), (1, 3), and (1, 4), and the other with cells (3, 1), (3, 2), and (3, 3).
     */
    public static int[][][] CORRIDORS;
    public static int[][][] DEADEND_CORRIDORS;

    /*
        Initialize meta information of the level
     */
    public static void init(State initialState) {
        MAP_ROWS = State.WALLS.length;
        MAP_COLS = State.WALLS[0].length;
        MAP_AREA = MAP_ROWS * MAP_COLS;

        initGoals();
        initObjectsPerColor(initialState);
        MapStructurePreprocessor mapStructurePreprocessor = new MapStructurePreprocessor();
        DISTANCES = mapStructurePreprocessor.getDistances();
        PATHS_ROW = mapStructurePreprocessor.getPathsRow();
        PATHS_COL = mapStructurePreprocessor.getPathsCol();
        BRANCH_CELLS = mapStructurePreprocessor.getBranchCells();
        // CORRIDORS = mapStructurePreprocessor.getCorridors(State.WALLS);
    }

    public static State initSplit(State initialState) {
        MAP_ROWS = State.WALLS.length;
        MAP_COLS = State.WALLS[0].length;
        MAP_AREA = MAP_ROWS * MAP_COLS;

        initGoals();
        initObjectsPerColor(initialState);
        MapStructurePreprocessor mapStructurePreprocessor = new MapStructurePreprocessor();
        DISTANCES = mapStructurePreprocessor.getDistances();
        PATHS_ROW = mapStructurePreprocessor.getPathsRow();
        PATHS_COL = mapStructurePreprocessor.getPathsCol();
        BRANCH_CELLS = mapStructurePreprocessor.getBranchCells();

        // create splits & replace boxes by walls - 1st time
        MAPSPLIT = mapStructurePreprocessor.getMapSplit(initialState);
        initialState = mapStructurePreprocessor.replaceBoxWall(initialState);

        //reprocess paths and distances with the new walls
        initObjectsPerColor(initialState);
        MapStructurePreprocessor mapStructurePreprocessor2 = new MapStructurePreprocessor();
        DISTANCES = mapStructurePreprocessor2.getDistances();
        PATHS_ROW = mapStructurePreprocessor2.getPathsRow();
        PATHS_COL = mapStructurePreprocessor2.getPathsCol();
        BRANCH_CELLS = mapStructurePreprocessor2.getBranchCells();

        // create splits & replace walls - 2nd time
        MAPSPLIT = mapStructurePreprocessor2.getMapSplit(initialState);
        initialState = mapStructurePreprocessor2.replaceBoxWall(initialState);

        //reprocess paths and distances with the new walls - again
        initObjectsPerColor(initialState);
        MapStructurePreprocessor mapStructurePreprocessor3 = new MapStructurePreprocessor();
        DISTANCES = mapStructurePreprocessor3.getDistances();
        PATHS_ROW = mapStructurePreprocessor3.getPathsRow();
        PATHS_COL = mapStructurePreprocessor3.getPathsCol();
        BRANCH_CELLS = mapStructurePreprocessor3.getBranchCells();
        // CORRIDORS = mapStructurePreprocessor.getCorridors(State.WALLS);

        CORRIDORS = mapStructurePreprocessor3.getCorridors(State.WALLS);
        // Debug.printCorridors(CORRIDORS);
        DEADEND_CORRIDORS = mapStructurePreprocessor3.getDeadendCorridors(CORRIDORS, State.WALLS);
        // Debug.printCorridors(DEADEND_CORRIDORS);
        mapStructurePreprocessor3.rankGoals();

        return initialState;
    }


    /*
        Returns the real distance between to points (row1, col1) and (row2, col2).
     */
    public static int getDistance(int startRow, int startCol, int endRow, int endCol) {
        return DISTANCES[startRow][startCol][endRow][endCol];
    }

    /*
        Returns the next cell of the shortest path between (row1, col1) and (row2, col2).
        E.g. getNextCell(1, 1, 3, 1) returns [1, 2]. It means to get from (1, 1) to (3, 1)
        you have to go to (1, 2) first.
     */
    public static int[] getNextCell(int startRow, int startCol, int endRow, int endCol) {
        return new int[]{PATHS_ROW[startRow][startCol][endRow][endCol], PATHS_COL[startRow][startCol][endRow][endCol]};
    }

    public static int[] getPath(int startRow, int startCol, int endRow, int endCol) {
        int[] path = new int[]{startRow, startCol};
        int nextRow = startRow;
        int nextCol = startCol;

        while(!(nextRow == endRow && nextCol == endCol)) {
            int[] nextCell = getNextCell(nextRow, nextCol, endRow, endCol);
            nextRow = nextCell[0];
            nextCol = nextCell[1];

            if (nextRow < 0 || nextCol < 0) {
                return null;
            }

            path = addToPath(path, nextRow, nextCol);
        }

        return path;
    }


    // Getter method for CORRIDORS
    public int[][][] getCorridors() {
        return CORRIDORS;
    }

    /*
        Returns true if there are any box goal spots, otherwise false.
     */
    public static boolean hasBoxGoals() {
        return BOX_GOALS_NUMBER > 0;
    }

    /*
        Returns true if there are any agent goal spots, otherwise false.
     */
    public static boolean hasAgentGoals() {
        return AGENT_GOALS_NUMBER > 0;
    }

    private static void initObjectsPerColor(State initialState) {
        AGENTS_PER_COLOR = new int[Color.values().length][LevelMetadata.MAP_AREA];
        BOXES_PER_COLOR = new char[Color.values().length][LevelMetadata.MAP_AREA];

        for (int i=0; i<Color.values().length; i++) {
            Arrays.fill(AGENTS_PER_COLOR[i], -1);
            Arrays.fill(BOXES_PER_COLOR[i], (char)-1);
        }

        int[] colorCounts = new int[Color.values().length];
        for (int agentIndex = 0; agentIndex < initialState.agentRows.length; agentIndex++) {
            int colorValue = State.AGENT_COLORS[agentIndex].getValue();
            AGENTS_PER_COLOR[colorValue][colorCounts[colorValue]++] = agentIndex;
        }
        removeNegativeIntegers(AGENTS_PER_COLOR);

        Arrays.fill(colorCounts, 0);
        for (int boxRow = 0; boxRow < initialState.boxes.length; boxRow++) {
            for (int boxCol = 0; boxCol < initialState.boxes[boxRow].length; boxCol++) {
                char box = initialState.boxes[boxRow][boxCol];
                if (box == 0) {
                    continue;
                }
                int colorValue = State.BOX_COLORS[StateUtils.boxCharToInt(box)].getValue();
                BOXES_PER_COLOR[colorValue][colorCounts[colorValue]++] = box;
            }
        }
        removeNonBoxChars(BOXES_PER_COLOR);
    }

    private static void initGoals() {
        AGENT_GOALS_COORDINATES = new int[MAX_NUM_OF_DISTINCT_AGENTS][2*MAP_AREA];
        BOX_GOALS_COORDINATES = new int[MAX_NUM_OF_DISTINCT_BOXES][2*MAP_AREA];
        for (int[] agentGoals : AGENT_GOALS_COORDINATES) {
            Arrays.fill(agentGoals, -1);
        }
        for (int[] boxGoals : BOX_GOALS_COORDINATES) {
            Arrays.fill(boxGoals, -1);
        }

        for (int row = 0; row < MAP_ROWS; row++) {
            for (int col = 0; col < MAP_COLS; col++) {
                char goal = State.GOALS[row][col];
                if (StateUtils.isBox(goal)) {
                    int goalInt = StateUtils.boxCharToInt(goal);
                    BOX_GOALS_COORDINATES[goalInt][2*BOX_GOALS_NUM[goalInt]] = row;
                    BOX_GOALS_COORDINATES[goalInt][2*BOX_GOALS_NUM[goalInt] + 1] = col;
                    BOX_GOALS_NUM[goalInt] += 1;
                    goalRank.put(new MapStructurePreprocessor.Coordinates(row,col), 0);
                    BOX_GOALS_NUMBER++;
                } else if (StateUtils.isAgent(goal)) {
                    int goalInt = StateUtils.agentCharToInt(goal);
                    AGENT_GOALS_COORDINATES[goalInt][2*AGENT_GOALS_NUM[goalInt]] = row;
                    AGENT_GOALS_COORDINATES[goalInt][2*AGENT_GOALS_NUM[goalInt] + 1] = col;
                    AGENT_GOALS_NUM[goalInt] += 1;
                    goalRank.put(new MapStructurePreprocessor.Coordinates(row,col), 0);
                    AGENT_GOALS_NUMBER++;
                }
            }
        }
        removeNegativeIntegers(AGENT_GOALS_COORDINATES);
        removeNegativeIntegers(BOX_GOALS_COORDINATES);
    }

    private static void removeNegativeIntegers(int[][] array) {
        for (int i=0; i<array.length; i++) {
            int lastIndex;
            int[] sub_array = array[i];
            for (lastIndex=0; lastIndex<sub_array.length; lastIndex++) {
                if (sub_array[lastIndex] < 0) {
                    break;
                }
            }
            array[i] = Arrays.copyOfRange(sub_array, 0, lastIndex);
        }
    }

    private static void removeNonBoxChars(char[][] array) {
        for (int i=0; i<array.length; i++) {
            int lastIndex;
            char[] sub_array = array[i];
            for (lastIndex=0; lastIndex<sub_array.length; lastIndex++) {
                if (!StateUtils.isBox(sub_array[lastIndex])) {
                    break;
                }
            }
            array[i] = Arrays.copyOfRange(sub_array, 0, lastIndex);
        }
    }


    /*
        add a new corridor to the existing CORRIDORS array
    */
    public void addCorridor(int[][] corridor) {
        int[][][] newCorridors = new int[CORRIDORS.length + 1][][];
        System.arraycopy(CORRIDORS, 0, newCorridors, 0, CORRIDORS.length);
        newCorridors[CORRIDORS.length] = corridor;
        CORRIDORS = newCorridors;
    }


    private static int[] addToPath(int[] path, int row, int col) {
        int len = path.length;
        path = Arrays.copyOf(path, len + 2);
        path[len] = row;
        path[len + 1] = col;
        return path;
    }
    
}
