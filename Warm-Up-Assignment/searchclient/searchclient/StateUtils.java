package searchclient;

/*
    Small utils functions, which wrap up simple domain conditions are transformations
    into readable methods.
 */
public class StateUtils {

    public static final char MIN_AGENT_CHAR = '0';
    public static final char MAX_AGENT_CHAR = '9';
    public static final char MIN_AGENT_INT = 0;
    public static final char MAX_AGENT_INT = 9;
    public static final char MIN_BOX_CHAR = 'A';
    public static final char MAX_BOX_CHAR = 'Z';

    public static int getNumOfWallsAround(int row, int col) {
        int wallsAroundCell = 0;
        wallsAroundCell += !isOutOfBounds(row + 1, col) && cellIsWall(row + 1, col) ? 1 : 0;
        wallsAroundCell += !isOutOfBounds(row - 1, col) && cellIsWall(row - 1, col) ? 1 : 0;
        wallsAroundCell += !isOutOfBounds(row, col + 1) && cellIsWall(row, col + 1) ? 1 : 0;
        wallsAroundCell += !isOutOfBounds(row, col - 1) && cellIsWall(row, col - 1) ? 1 : 0;
        return wallsAroundCell;
    }

    public static boolean isAgent(char agent) {
        return agent >= MIN_AGENT_CHAR && agent <= MAX_AGENT_CHAR;
    }

    public static boolean isAgent(int agent) {
        return agent >= MIN_AGENT_INT && agent <= MAX_AGENT_INT;
    }

    public static boolean isBox(char box) {
        return box >= MIN_BOX_CHAR && box <= MAX_BOX_CHAR;
    }

    public static boolean isBoxInGoalState(State state, int row, int col) {
        return State.GOALS[row][col] == state.boxes[row][col];
    }

    public static boolean isAgentInGoalState(State state, char agent) {
        int agentIndex = StateUtils.agentCharToInt(agent);
        int row = state.agentRows[agentIndex];
        int col = state.agentCols[agentIndex];
        return State.GOALS[row][col] == agent;
    }
    public static boolean isGoal(int x, int y){
        char goalCell = State.GOALS[x][y];
        if (StateUtils.isBox(goalCell) || StateUtils.isAgent(goalCell)){return true;}
        else{return false;}
    }

    public static char agentIntToChar(int agent) {
        return (char) (agent + MIN_AGENT_CHAR);
    }

    public static int agentCharToInt(char agent) {
        return agent - MIN_AGENT_CHAR;
    }

    public static int boxCharToInt(char box) {
        return box - MIN_BOX_CHAR;
    }

    public static boolean cellIsFree(State state, int row, int col) {
        return !cellIsWall(row, col) && boxAt(state, row, col) == 0 && agentAt(state, row, col) == 0;
    }

    public static boolean cellIsFree(State state, Coordinates coords) {
        return coords != null && !cellIsWall(coords.row, coords.col) && boxAt(state, coords.row, coords.col) == 0 && agentAt(state, coords.row, coords.col) == 0;
    }

    public static boolean cellIsWall(int row, int col) {
        return State.WALLS[row][col];
    }

    public static boolean isSameCell(int row1, int col1, int row2, int col2) {
        return row1 == row2 && col1 == col2;
    }

    public static char agentAt(State state, int row, int col) {
        for (int i = 0; i < state.agentRows.length; i++) {
            if (state.agentRows[i] == row && state.agentCols[i] == col) {
                return (char) (MIN_AGENT_CHAR + i);
            }
        }
        return 0;
    }

    public static char boxAt(State state, int row, int col) {
        return state.boxes[row][col];
    }

    public static boolean isOutOfBounds(int row, int col) {
        return row < 0 || row >= LevelMetadata.MAP_ROWS || col < 0 || col >= LevelMetadata.MAP_COLS;
    }

    public static boolean isOutOfBounds(Coordinates coords) {
        return coords == null || coords.row < 0 || coords.row >= LevelMetadata.MAP_ROWS || coords.col < 0 || coords.col >= LevelMetadata.MAP_COLS;
    }

    public static boolean[] agentsForBox(State state, char box){
        Color[] agentColors = State.AGENT_COLORS;
        Color[] boxColors = State.BOX_COLORS;
        Color colorOfBox = boxColors[boxCharToInt(box)];
        boolean[] agentSameColor = new boolean[state.agentCols.length];
        for (int a=0; a<state.agentRows.length;a++){
            if (agentColors[a] == colorOfBox){
                agentSameColor[a] = true;
            } else {
                agentSameColor[a] = false;
            }
        }
        return agentSameColor;
    }

}