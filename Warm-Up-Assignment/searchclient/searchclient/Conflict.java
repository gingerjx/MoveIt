package searchclient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Conflict {

    boolean isConflict = false;
    int numOfAction = -1;
    int numberOfSkippedActions = -1;

    List<Coordinates> obstacleArea = new ArrayList<>();
    Coordinates initialAgentCoords = null;
    Coordinates finalAgentCoords = null;
    Coordinates initialBoxCoords = null;
    Coordinates finalBoxCoords = null;
    State conflictState = null;

    public Conflict() {}

    public Conflict(boolean isConflict, int numOfAction, State conflictState) {
        this.isConflict = isConflict;
        this.numOfAction = numOfAction;
        this.conflictState = conflictState;
    }

    public static Conflict getNextConflict(State state, Action[] plan, int startAction, int agent) {
        int i=startAction;
        int row = state.agentRows[agent];
        int col= state.agentCols[agent];
        char agentChar = StateUtils.agentIntToChar(agent);

        for (; i < plan.length; i++) {
            Action action = plan[i];
            int nextRow = row + action.agentRowDelta;
            int nextCol = col + action.agentColDelta;

            if (action.type == ActionType.Move || action.type == ActionType.Pull) {
                if (!StateUtils.cellIsFree(state, nextRow, nextCol) && StateUtils.agentAt(state, nextRow, nextCol) != agentChar) {
                    State conflictState = StateActionUtils.getUpdatedState(state, Arrays.copyOfRange(plan, startAction, i), agent);
                    return new Conflict(true, i, conflictState);
                }
            } else if (action.type == ActionType.Push) {
                int boxRow = nextRow + action.boxRowDelta;
                int boxCol = nextCol + action.boxColDelta;
                if (!StateUtils.cellIsFree(state, boxRow, boxCol) && StateUtils.agentAt(state, nextRow, nextCol) != agentChar) {
                    State conflictState = StateActionUtils.getUpdatedState(state, Arrays.copyOfRange(plan, startAction, i), agent);
                    return new Conflict(true, i, conflictState);
                }
            }

            row = nextRow;
            col = nextCol;
        }

        return new Conflict();
    }

    public State getPostConflictState(Action[] plan, int startAction, int agent) {
        int agentRow = conflictState.agentRows[agent];
        int agentCol = conflictState.agentCols[agent];
        initialAgentCoords = new Coordinates(agentRow, agentCol);
        int i = startAction;

        for (; i < plan.length; i++) {
            Action action = plan[i];

            Coordinates boxCoords = StateActionUtils.getInitialBoxCell(agentRow, agentCol, action);
            if (initialBoxCoords == null && boxCoords != null) {
                initialBoxCoords = boxCoords;
                if (StateUtils.cellIsFree(conflictState, agentRow, agentCol)) {
                    break;
                }
            }

            if (StateUtils.cellIsFree(conflictState, agentRow, agentCol) && isBoxCellFree(boxCoords)) {
                break;
            }

            agentRow += action.agentRowDelta;
            agentCol += action.agentColDelta;
            obstacleArea.add(new Coordinates(agentRow, agentCol));
        }

        State postConflictState = new State(conflictState);
        Action lastAction = plan[i - 1];

        finalAgentCoords = new Coordinates(agentRow, agentCol);
        if (initialBoxCoords != null) {
            if (i >= plan.length) {
                finalBoxCoords = StateActionUtils.getBoxCellAfterAction(agentRow, agentCol, lastAction);
            } else {
                finalBoxCoords = StateActionUtils.getBoxCellBeforeAction(agentRow, agentCol, lastAction);
            }
        }
        int agentGoalDistance = LevelMetadata.getDistance(initialAgentCoords.row, initialAgentCoords.col, finalAgentCoords.row, finalAgentCoords.col);
        boolean lastTwoMoves = agentGoalDistance == 2;
        boolean lastOneMove = agentGoalDistance == 1;

        if (lastOneMove) {
            swapForOneMove(postConflictState, lastAction);
        }
        else if (lastTwoMoves) {
            swapForTwoMoves(postConflictState, initialAgentCoords, finalAgentCoords);
        }

        postConflictState.agentRows[agent] = agentRow;
        postConflictState.agentCols[agent] = agentCol;
        obstacleArea.remove(finalAgentCoords);

        if (initialBoxCoords != null) {
            if (!(lastOneMove && lastAction.type == ActionType.Pull)) {
                postConflictState.boxes[initialBoxCoords.row][initialBoxCoords.col] = 0;
            }

            if (lastTwoMoves) {
                swapForTwoMoves(postConflictState, initialBoxCoords, finalBoxCoords);
            }

            postConflictState.boxes[finalBoxCoords.row][finalBoxCoords.col] = conflictState.boxes[initialBoxCoords.row][initialBoxCoords.col];
            obstacleArea.remove(finalBoxCoords);
        }

        obstacleArea.remove(initialBoxCoords);
        numberOfSkippedActions = i - startAction;

        return postConflictState;
    }

    public boolean isBoxCellFree(Coordinates boxCell) {
        if (StateUtils.isOutOfBounds(boxCell)) {
            return true;
        } else if (StateUtils.cellIsFree(conflictState, boxCell)) {
            return true;
        } else {
            return false;
        }
    }

    private void swapForOneMove(State postConflictState, Action lastAction) {
        if (lastAction.type == ActionType.Move) {
            char boxOnCell = conflictState.boxes[finalAgentCoords.row][finalAgentCoords.col];
            char agentOnCellChar = StateUtils.agentAt(conflictState, finalAgentCoords.row, finalAgentCoords.col);

            if (StateUtils.isBox(boxOnCell)) {
                postConflictState.boxes[finalAgentCoords.row][finalAgentCoords.col] = 0;
                postConflictState.boxes[initialAgentCoords.row][initialAgentCoords.col] = boxOnCell;
            } else if (StateUtils.isAgent(agentOnCellChar)) {
                int agentOnCell1Int = StateUtils.agentCharToInt(agentOnCellChar);
                postConflictState.agentRows[agentOnCell1Int] = initialAgentCoords.row;
                postConflictState.agentCols[agentOnCell1Int] = initialAgentCoords.col;
            }
        } else if (lastAction.type == ActionType.Pull) {
            char boxOnCell = conflictState.boxes[finalAgentCoords.row][finalAgentCoords.col];
            char agentOnCellChar = StateUtils.agentAt(conflictState, finalAgentCoords.row, finalAgentCoords.col);

            if (StateUtils.isBox(boxOnCell)) {
                postConflictState.boxes[finalAgentCoords.row][finalAgentCoords.col] = 0;
                postConflictState.boxes[initialBoxCoords.row][initialBoxCoords.col] = boxOnCell;
            } else if (StateUtils.isAgent(agentOnCellChar)) {
                int agentOnCell1Int = StateUtils.agentCharToInt(agentOnCellChar);
                postConflictState.agentRows[agentOnCell1Int] = initialBoxCoords.row;
                postConflictState.agentCols[agentOnCell1Int] = initialBoxCoords.col;
            }
        } else if (lastAction.type == ActionType.Push) {
            char boxOnCell = conflictState.boxes[finalBoxCoords.row][finalBoxCoords.col];
            char agentOnCellChar = StateUtils.agentAt(conflictState, finalBoxCoords.row, finalBoxCoords.col);

            if (StateUtils.isBox(boxOnCell)) {
                postConflictState.boxes[finalBoxCoords.row][finalBoxCoords.col] = 0;
                postConflictState.boxes[initialAgentCoords.row][initialAgentCoords.col] = boxOnCell;
            } else if (StateUtils.isAgent(agentOnCellChar)) {
                int agentOnCell1Int = StateUtils.agentCharToInt(agentOnCellChar);
                postConflictState.agentRows[agentOnCell1Int] = initialAgentCoords.row;
                postConflictState.agentCols[agentOnCell1Int] = initialAgentCoords.col;
            }
        }
    }

    private void swapForTwoMoves(State postConflictState, Coordinates initialCoords, Coordinates finalCoords) {
        char boxOnCell = conflictState.boxes[finalCoords.row][finalCoords.col];
        char agentOnCellChar = StateUtils.agentAt(conflictState, finalCoords.row, finalCoords.col);
        if (StateUtils.isBox(boxOnCell)) {
            postConflictState.boxes[finalCoords.row][finalCoords.col] = 0;
            postConflictState.boxes[initialCoords.row][initialCoords.col] = boxOnCell;
        }
        if (StateUtils.isAgent(agentOnCellChar)) {
            int agentOnCell1Int = StateUtils.agentCharToInt(agentOnCellChar);
            postConflictState.agentRows[agentOnCell1Int] = initialCoords.row;
            postConflictState.agentCols[agentOnCell1Int] = initialCoords.col;
        }
    }
}


