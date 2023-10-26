package searchclient;

/*
    Defines logic regarding available actions
 */
public class StateActionUtils {

    public static void move(State state, int agent, Action action) {
        state.agentRows[agent] += action.agentRowDelta;
        state.agentCols[agent] += action.agentColDelta;
    }

    public static void push(State state, int agent, Action action) {
        int newAgentRow = state.agentRows[agent] + action.agentRowDelta;
        int newAgentCol = state.agentCols[agent] + action.agentColDelta;
        state.boxes[newAgentRow + action.boxRowDelta][newAgentCol + action.boxColDelta] = state.boxes[newAgentRow][newAgentCol];
        state.boxes[newAgentRow][newAgentCol] = 0;
        state.agentRows[agent] = newAgentRow;
        state.agentCols[agent] = newAgentCol;
    }

    public static void pull(State state, int agent, Action action) {
        int agentRow = state.agentRows[agent];
        int agentCol = state.agentCols[agent];
        state.boxes[agentRow][agentCol] = state.boxes[agentRow - action.boxRowDelta][agentCol - action.boxColDelta];
        state.boxes[agentRow - action.boxRowDelta][agentCol - action.boxColDelta] = 0;
        state.agentRows[agent] += action.agentRowDelta;
        state.agentCols[agent] += action.agentColDelta;
    }

    public static boolean isMoveApplicable(State state, int agent, Action action) {
        int newAgentRow = state.agentRows[agent] + action.agentRowDelta;
        int newAgentCol = state.agentCols[agent] + action.agentColDelta;
        return StateUtils.cellIsFree(state, newAgentRow, newAgentCol);
    }

    public static boolean isPushApplicable(State state, int agent, Action action) {
        Color agentColor = State.AGENT_COLORS[agent];
        int newAgentRow = state.agentRows[agent] + action.agentRowDelta;
        int newAgentCol = state.agentCols[agent] + action.agentColDelta;
        char box = StateUtils.boxAt(state, newAgentRow, newAgentCol);
        if (!StateUtils.isBox(box)) {
            return false;
        }
        Color boxColor = State.BOX_COLORS[StateUtils.boxCharToInt(box)];
        int newBoxRow = newAgentRow + action.boxRowDelta;
        int newBoxCol = newAgentCol + action.boxColDelta;
        return agentColor.equals(boxColor) &&
                StateUtils.cellIsFree(state, newBoxRow, newBoxCol);
    }

    public static boolean isPullApplicable(State state, int agent, Action action) {
        Color agentColor = State.AGENT_COLORS[agent];
        int oldBoxRow = state.agentRows[agent] - action.boxRowDelta;
        int oldBoxCol = state.agentCols[agent] - action.boxColDelta;
        char box = StateUtils.boxAt(state, oldBoxRow, oldBoxCol);
        if (!StateUtils.isBox(box)) {
            return false;
        }
        Color boxColor = State.BOX_COLORS[StateUtils.boxCharToInt(box)];
        int newAgentRow = state.agentRows[agent] + action.agentRowDelta;
        int newAgentCol = state.agentCols[agent] + action.agentColDelta;
        return agentColor.equals(boxColor) &&
                StateUtils.cellIsFree(state, newAgentRow, newAgentCol);
    }

    public static State getUpdatedState(State state, Action[][] plans) {
        State updatedState = new State(state);

        for (Action[] plan : plans) {
            updatedState = new State(updatedState, plan);
        }

        return updatedState;
    }

    public static State getUpdatedState(State state, Action[] plan, int agent) {
        State updatedState = new State(state);

        for (Action action : plan) {
            switch (action.type) {
                case NoOp:
                    break;
                case Move:
                    StateActionUtils.move(updatedState, agent, action);
                    break;
                case Push:
                    StateActionUtils.push(updatedState, agent, action);
                    break;
                case Pull:
                    StateActionUtils.pull(updatedState, agent, action);
                    break;
            }
        }

        return updatedState;
    }

    public static Coordinates getBoxCellBeforeAction(int agentRow, int agentCol, Action action) {
        if (action.type == ActionType.Push) {
            int boxRow = agentRow + action.agentRowDelta;
            int boxCol = agentCol + action.agentColDelta;
            return new Coordinates(boxRow, boxCol);
        } else if (action.type == ActionType.Pull) {
            int boxRow = agentRow - action.agentRowDelta;
            int boxCol = agentCol - action.agentColDelta;
            return new Coordinates(boxRow, boxCol);
        } else {
            return null;
        }
    }

    public static Coordinates getBoxCellAfterAction(int agentRow, int agentCol, Action action) {
        if (action.type == ActionType.Push) {
            int boxRow = agentRow + action.boxRowDelta;
            int boxCol = agentCol + action.boxColDelta;
            return new Coordinates(boxRow, boxCol);
        } else if (action.type == ActionType.Pull) {
            return new Coordinates(agentRow - action.agentRowDelta, agentCol - action.agentColDelta);
        } else {
            return null;
        }
    }

    public static Coordinates getInitialBoxCell(int agentRow, int agentCol, Action action) {
        if (action.type == ActionType.Push) {
            int boxRow = agentRow + action.agentRowDelta;
            int boxCol = agentCol + action.agentColDelta;
            return new Coordinates(boxRow, boxCol);
        } else if (action.type == ActionType.Pull) {
            int boxRow = agentRow - action.boxRowDelta;
            int boxCol = agentCol - action.boxColDelta;
            return new Coordinates(boxRow, boxCol);
        } else {
            return null;
        }
    }

}
