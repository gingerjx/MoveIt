package searchclient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

public class State {
    private static final Random RNG = new Random(1);

    public static char[][] GOALS;
    public static boolean[][] WALLS;
    public static Color[] AGENT_COLORS;
    public static Color[] BOX_COLORS;

    public int[] agentRows;
    public int[] agentCols;
    public char[][] boxes;
    public final int numOfAgents;
    public final State parent;
    public final Action[] jointAction;

    private final int g;
    private int hash = 0;

    public State(int[] agentRows, int[] agentCols, Color[] agentColors, boolean[][] walls,
                 char[][] boxes, Color[] boxColors, char[][] goals
    ) {
        GOALS = goals;
        WALLS = walls;
        AGENT_COLORS = agentColors;
        BOX_COLORS = boxColors;

        this.agentRows = agentRows;
        this.agentCols = agentCols;
        this.boxes = boxes;
        this.numOfAgents = agentRows.length;
        this.parent = null;
        this.jointAction = null;

        this.g = 0;
    }

    public State(State state) {
        this.agentRows = Arrays.copyOf(state.agentRows, state.agentRows.length);
        this.agentCols = Arrays.copyOf(state.agentCols, state.agentCols.length);
        this.boxes = new char[state.boxes.length][];
        for (int i = 0; i < state.boxes.length; i++) {
            this.boxes[i] = Arrays.copyOf(state.boxes[i], state.boxes[i].length);
        }
        this.numOfAgents = state.numOfAgents;
        this.parent = null;
//        if (state.jointAction != null) {
//            this.jointAction = Arrays.copyOf(state.jointAction, state.jointAction.length);
//        } else {
//            this.jointAction = null;
//        }
        this.jointAction = null;
        this.g = 0;
    }

    public State(State parent, Action[] jointAction) {
        this.agentRows = Arrays.copyOf(parent.agentRows, parent.agentRows.length);
        this.agentCols = Arrays.copyOf(parent.agentCols, parent.agentCols.length);
        this.boxes = new char[parent.boxes.length][];
        for (int i = 0; i < parent.boxes.length; i++) {
            this.boxes[i] = Arrays.copyOf(parent.boxes[i], parent.boxes[i].length);
        }

        this.numOfAgents = parent.numOfAgents;
        this.parent = parent;
        this.jointAction = Arrays.copyOf(jointAction, jointAction.length);
        this.g = parent.g + 1;

        for (int agent = 0; agent < numOfAgents; ++agent) {
            Action action = jointAction[agent];
            switch (action.type) {
                case NoOp:
                    break;
                case Move:
                    StateActionUtils.move(this, agent, action);
                    break;
                case Push:
                    StateActionUtils.push(this, agent, action);
                    break;
                case Pull:
                    StateActionUtils.pull(this, agent, action);
                    break;
            }
        }
    }

    public int g() {
        return this.g;
    }

    public boolean isGoalState() {
        for (int row = 1; row < GOALS.length - 1; row++) {
            for (int col = 1; col < GOALS[row].length - 1; col++) {
                char goal = GOALS[row][col];
                if (StateUtils.isBox(goal) && !StateUtils.isBoxInGoalState(this, row, col)) {
                    return false;
                } else if (StateUtils.isAgent(goal) && !StateUtils.isAgentInGoalState(this, goal)) {
                    return false;
                }
            }
        }
        return true;
    }

    public ArrayList<State> getExpandedStates() {
        Action[][] applicableActions = StateExpandUtils.getApplicableActions(this);
        Action[] jointAction = new Action[numOfAgents];
        int[] actionsPermutation = new int[numOfAgents];
        ArrayList<State> expandedStates = new ArrayList<>(16);

        do {
            for (int agent = 0; agent < numOfAgents; ++agent) {
                jointAction[agent] = applicableActions[agent][actionsPermutation[agent]];
            }
            if (!StateExpandUtils.isConflicting(this, jointAction)) {
                expandedStates.add(new State(this, jointAction));
            }
        } while(!StateExpandUtils.performPermutation(this, actionsPermutation, applicableActions));

        Collections.shuffle(expandedStates, State.RNG);
        return expandedStates;
    }

    public Action[][] extractPlan() {
        Action[][] plan = new Action[this.g][];
        State state = this;
        while (state.jointAction != null) {
            plan[state.g - 1] = state.jointAction;
            state = state.parent;
        }
        return plan;
    }

    @Override
    public int hashCode() {
        if (this.hash == 0) {
            final int prime = 31;
            int result = 1;
            result = prime * result + Arrays.hashCode(this.AGENT_COLORS);
            result = prime * result + Arrays.hashCode(this.BOX_COLORS);
            result = prime * result + Arrays.deepHashCode(this.WALLS);
            result = prime * result + Arrays.deepHashCode(this.GOALS);
            result = prime * result + Arrays.hashCode(this.agentRows);
            result = prime * result + Arrays.hashCode(this.agentCols);
            for (int row = 0; row < this.boxes.length; ++row) {
                for (int col = 0; col < this.boxes[row].length; ++col) {
                    char c = this.boxes[row][col];
                    if (c != 0) {
                        result = prime * result + (row * this.boxes[row].length + col) * c;
                    }
                }
            }
            this.hash = result;
        }
        return this.hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        State other = (State) obj;
        return Arrays.equals(this.agentRows, other.agentRows) &&
                Arrays.equals(this.agentCols, other.agentCols) &&
                Arrays.equals(this.AGENT_COLORS, other.AGENT_COLORS) &&
                Arrays.deepEquals(this.WALLS, other.WALLS) &&
                Arrays.deepEquals(this.boxes, other.boxes) &&
                Arrays.equals(this.BOX_COLORS, other.BOX_COLORS) &&
                Arrays.deepEquals(this.GOALS, other.GOALS);
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (int row = 0; row < WALLS.length; row++) {
            for (int col = 0; col < WALLS[row].length; col++) {
                if (this.boxes[row][col] > 0) {
                    s.append(this.boxes[row][col]);
                } else if (WALLS[row][col]) {
                    s.append("+");
                } else if (StateUtils.agentAt(this, row, col) != 0) {
                    s.append(StateUtils.agentAt(this, row, col));
                } else {
                    s.append(" ");
                }
            }
            s.append("\n");
        }
        return s.toString();
    }
}
