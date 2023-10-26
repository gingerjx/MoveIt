package searchclient;

import java.util.ArrayList;

/*
    Methods used by State.getExpandedStates(...) method.
 */
public class StateExpandUtils {

    public static  Action[][] getApplicableActions(State state) {
        Action[][] applicableActions = new Action[state.numOfAgents][];
        for (int agent = 0; agent < state.numOfAgents; ++agent) {
            ArrayList<Action> agentActions = new ArrayList<>(Action.values().length);
            for (Action action : Action.values()) {
                if (isApplicable(state, agent, action)) {
                    agentActions.add(action);
                }
            }
            applicableActions[agent] = agentActions.toArray(new Action[0]);
        }
        return applicableActions;
    }

    public static boolean isConflicting(State state, Action[] jointAction) {
        int[] agentsRows = new int[state.numOfAgents];
        int[] agentsCols = new int[state.numOfAgents];
        int[] boxesRows = new int[state.numOfAgents];
        int[] boxesCols = new int[state.numOfAgents];

        for (int agent = 0; agent < state.numOfAgents; ++agent) {
            Action action = jointAction[agent];
            int agentRow = state.agentRows[agent];
            int agentCol = state.agentCols[agent];

            switch (action.type) {
                case NoOp:
                    break;
                case Move:
                    agentsRows[agent] = agentRow + action.agentRowDelta;
                    agentsCols[agent] = agentCol + action.agentColDelta;
                    break;
                case Push:
                    agentsRows[agent] = agentRow + action.agentRowDelta;
                    agentsCols[agent] = agentCol + action.agentColDelta;
                    boxesRows[agent] = agentsRows[agent] + action.boxRowDelta;
                    boxesCols[agent] = agentsCols[agent] + action.boxColDelta;
                    break;
                case Pull:
                    agentsRows[agent] = agentRow + action.agentRowDelta;
                    agentsCols[agent] = agentCol + action.agentColDelta;
                    boxesRows[agent] = agentRow;
                    boxesCols[agent] = agentCol;
                    break;
            }
        }

        for (int a1 = 0; a1 < state.numOfAgents; ++a1) {
            if (jointAction[a1] == Action.NoOp) {
                continue;
            }
            for (int a2 = a1 + 1; a2 < state.numOfAgents; ++a2) {
                if (jointAction[a2] == Action.NoOp) {
                    continue;
                }

                if(isSameCoord(agentsRows, agentsCols, a1, a2) ||
                    isSameCoord(boxesRows, boxesCols, a1, a2) ||
                    isSameCoord(boxesRows, boxesCols, agentsRows, agentsCols, a1, a2) ||
                    isSameCoord(agentsRows, agentsCols, boxesRows, boxesCols, a1, a2)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean performPermutation(State state, int[] actionsPermutation, Action[][] applicableActions) {
        for (int agent = 0; agent < state.numOfAgents; ++agent) {
            if (actionsPermutation[agent] < applicableActions[agent].length - 1) {
                ++actionsPermutation[agent];
                return false;
            } else {
                actionsPermutation[agent] = 0;
                if (agent == state.numOfAgents - 1) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isSameCoord(int[] rows, int[] cols, int agent1, int agent2) {
        return rows[agent1] == rows[agent2] && cols[agent1] == cols[agent2];
    }

    private static boolean isSameCoord(int[] rows1, int[] cols1, int[] rows2, int[] cols2, int agent1, int agent2) {
        return rows1[agent1] == rows2[agent2] && cols1[agent1] == cols2[agent2];
    }

    private static boolean isApplicable(State state, int agent, Action action) {
        return switch (action.type) {
            case NoOp -> true;
            case Move -> StateActionUtils.isMoveApplicable(state, agent, action);
            case Push -> StateActionUtils.isPushApplicable(state, agent, action);
            case Pull -> StateActionUtils.isPullApplicable(state, agent, action);
        };
    }
}