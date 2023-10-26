package searchclient;

import java.util.Arrays;
import java.util.List;

public class PlanUtils {

    public static Action[][] pathsToActions(int[][] plans) {
        Action[][] actions = new Action[plans.length][];

        for (int i=0; i<plans.length; i++) {
            actions[i] = removeNullActions(pathToActions(plans[i]));
        }

        return actions;
    }
    
    public static int getPrioritizedAgent(Action[][] plans) {
      Action[] minPlan = plans[0];
      int prioritizedAgent = 0;

      for (int i=1; i<plans.length; i++) {
          if (plans[i].length > 0 && (minPlan.length <= 0 || plans[i].length < minPlan.length)) {
              minPlan = plans[i];
              prioritizedAgent = i;
          }
      }

      return prioritizedAgent;
    }

    public static int getPrioritizedAgentBasedOnNumberOfObstacles(Action[][] plans, int[][] subPlansPath, State state) {
        List<Integer> agents = Obstacles.findAgentsByObstacles(plans, subPlansPath, state);

        Action[] minPlan = plans[agents.get(0)];
        int prioritizedAgent = agents.get(0);

        if (agents.size()==1){
            return prioritizedAgent;
        }

        for (int i = 1; i < agents.size(); i++) {
            int agent = agents.get(i);
            if (plans[agent].length < minPlan.length) {
                minPlan = plans[agent];
                prioritizedAgent = agent;
            }
        }

        return prioritizedAgent;
    }

    public static void deleteNonPrioritizedSubPlans(int prioritizedAgent, Action[][] plans) {
        for (int i=0; i<plans.length; i++) {
            if (i == prioritizedAgent) {
                continue;
            }
            plans[i] = new Action[0];
            Arrays.fill(plans[i], Action.NoOp);
        }
    }

    public static Action[][] alignPlans(Action[][] plans, int numOfAgents) {
        int numOfActions = fillEmptyElementsWithNoOp(plans, numOfAgents);
        Action[][] alignedSubPlans = new Action[numOfActions][numOfAgents];

        for (int actionIndex=0; actionIndex<numOfActions; actionIndex++) {
            for (int agentIndex=0; agentIndex<numOfAgents; agentIndex++) {
                alignedSubPlans[actionIndex][agentIndex] = plans[agentIndex][actionIndex];
            }
        }

        return alignedSubPlans;
    }
    

    public static Action[][] appendActionsToPlan(Action[][] plan, Action[][] partialPlan) {
        if (plan.length <= 0) {
            return partialPlan;
        }

        int currentLen = plan.length;
        int partialPlanLen = partialPlan.length;
        Action[][] newPlan = new Action[currentLen + partialPlanLen][0];

        for (int i=0; i<plan.length; i++) {
            newPlan[i] = plan[i];
        }

        for (int i=0; i<partialPlan.length; i++) {
            newPlan[currentLen + i] = partialPlan[i];
        }

        return newPlan;
    }

    private static int fillEmptyElementsWithNoOp(Action[][] plans, int numOfAgents) {
        int longestPlanLen = plans[0].length;

        for (int i=0; i<numOfAgents; i++) {
            if (plans[i].length > longestPlanLen) {
                longestPlanLen = plans[i].length;
            }
        }

        for (int i=0; i<numOfAgents; i++) {
            if (plans[i].length < longestPlanLen) {
                Action[] newPlan = new Action[longestPlanLen];
                Arrays.fill(newPlan, Action.NoOp);
                System.arraycopy(plans[i], 0, newPlan, 0, plans[i].length);
                plans[i] = newPlan;
            }
        }

        return longestPlanLen;
    }

    private static Action[] pathToActions(int[] path) {
        if (path.length < 2) {
            return new Action[0];
        }

        Action[] actions = new Action[2*LevelMetadata.MAP_AREA];

        int[] result = transformToMoveActions(path, actions);
        int pathIndex = result[0];

        if (isFinalPlan(path, pathIndex)) {
            return actions;
        }

        int actionIndex = result[1];
        int prevRow = path[pathIndex - 2];
        int prevCol = path[pathIndex - 1];
        int nextBoxRow = path[pathIndex + 4];
        int nextBoxCol = path[pathIndex + 5];
        int goalRow = path[path.length - 2];
        int goalCol = path[path.length - 1];

        boolean shouldPush = shouldPush(prevRow, prevCol, nextBoxRow, nextBoxCol);

        if (shouldPush) {
            transformToPushActionsWrapper(path, pathIndex, actions, actionIndex);
        } else if (canPullToGoal(goalRow, goalCol)) {
            transformToPullActions(path, pathIndex + 2, actions, actionIndex, true);
        } else {
            transformToPullWithPushActions(path, pathIndex, actions, actionIndex);
        }

        return actions;
    }

    private static boolean isFinalPlan(int[] path, int pathIndex) {
        return path.length == pathIndex;
    }

    private static int[] transformToMoveActions(int[] path, Action[] actions) {
        int prevRow = path[0];
        int prevCol = path[1];
        int i = 2;
        int actionIndex = 0;

        for (; i<path.length; i += 2) {
            int row = path[i];
            int col = path[i+1];

            if (row == -1 && col == -1) {
                break;
            }

            actions[actionIndex++] = getMoveAction(prevRow, prevCol, row, col);
            prevRow = row;
            prevCol = col;
        }

        return new int[]{i, actionIndex};
    }

    private static void transformToPushActionsWrapper(int[] path, int pathIndex, Action[] actions, int actionIndex) {
        int prevRow = path[pathIndex - 2];
        int prevCol = path[pathIndex - 1];
        int row = path[pathIndex + 2];
        int col = path[pathIndex + 3];
        int next_row = path[pathIndex + 4];
        int next_col = path[pathIndex + 5];

        actions[actionIndex++] = getPushAction(prevRow, prevCol, row, col, next_row, next_col);
        transformToPushActions(path, pathIndex + 2, actions, actionIndex);
    }

    private static int transformToPushActions(int[] path, int pathIndex, Action[] actions, int actionIndex) {
        int prevRow = path[pathIndex];
        int prevCol = path[pathIndex + 1];

        for (int i=pathIndex+2; i<path.length - 2; i += 2) {
            int row = path[i];
            int col = path[i+1];
            int next_row = path[i + 2];
            int next_col = path[i + 3];

            actions[actionIndex++] = getPushAction(prevRow, prevCol, row, col, next_row, next_col);

            prevRow = row;
            prevCol = col;
        }

        return actionIndex;
    }

    private static int transformToPullActions(int[] path, int pathIndex, Action[] actions, int actionIndex, boolean performFinalPush) {
        int prevRow = path[pathIndex];
        int prevCol = path[pathIndex + 1];

        for (int i=pathIndex + 2; i<path.length - 2; i += 2) {
            int row = path[i];
            int col = path[i+1];
            int next_row = path[i + 2];
            int next_col = path[i + 3];

            actions[actionIndex++] = getPullAction(prevRow, prevCol, row, col, next_row, next_col);

            prevRow = row;
            prevCol = col;
        }

        if (performFinalPush) {
            int goalRow = path[path.length - 2];
            int goalCol = path[path.length - 1];
            addFinalPull(prevRow, prevCol, goalRow, goalCol, actions, actionIndex++);
        }

        return actionIndex;
    }

    private static void transformToPullWithPushActions(int[] path, int pathIndex, Action[] actions, int actionIndex) {
        int agentRow = path[pathIndex - 2];
        int agentCol = path[pathIndex - 1];
        int boxRow = path[pathIndex + 2];
        int boxCol = path[pathIndex + 3];
        int goalRow = path[path.length - 2];
        int goalCol = path[path.length - 1];

        int[] closestBranchCell = findClosestBranchCell(agentRow, agentCol);
        int minRow = closestBranchCell[0];
        int minCol = closestBranchCell[1];

        int[] pathToBranch = LevelMetadata.getPath(agentRow, agentCol, minRow, minCol);
        boolean push = shouldPushToBranchCell(pathToBranch, agentRow, agentCol, boxRow, boxCol);

        if (push) {
            actionIndex = transformToPushActions(pathToBranch, 0, actions, actionIndex);

            int newAgentRow = pathToBranch[pathToBranch.length - 4];
            int newAgentCol = pathToBranch[pathToBranch.length - 3];
            int newBoxRow = pathToBranch[pathToBranch.length - 2];
            int newBoxCol = pathToBranch[pathToBranch.length - 1];
            int[] emptyCell = getFirstEmptyCellExcluding(newBoxRow, newBoxCol, new int[]{newAgentRow, newAgentCol});

            actions[actionIndex++] = getPushAction(newAgentRow, newAgentCol, newBoxRow, newBoxCol, emptyCell[0], emptyCell[1]);

            newAgentRow = newBoxRow;
            newAgentCol = newBoxCol;
            newBoxRow = emptyCell[0];
            newBoxCol = emptyCell[1];
            int[] pathToGoal = LevelMetadata.getPath(newAgentRow, newAgentCol, goalRow, goalCol);
            int nextGoalPathRow = pathToGoal[2];
            int nextGoalPathCol = pathToGoal[3];
            int[] newAgentCell = getFirstEmptyCellExcluding(newAgentRow, newAgentCol, new int[]{newBoxRow, newBoxCol, nextGoalPathRow, nextGoalPathCol});

            actions[actionIndex++] = getPullAction(newBoxRow, newBoxCol, newAgentRow, newAgentCol, newAgentCell[0], newAgentCell[1]);

            int[] newPathToGoal = LevelMetadata.getPath(newAgentCell[0], newAgentCell[1], goalRow, goalCol);

            transformToPushActions(newPathToGoal, 0, actions, actionIndex);
        }
        else {
            int[] extendedPathToBranch = prependCell(pathToBranch, boxRow, boxCol);
            int len = extendedPathToBranch.length;

            actionIndex = transformToPullActions(extendedPathToBranch, 0, actions, actionIndex, false);

            int newBoxRow = extendedPathToBranch[len - 4];
            int newBoxCol = extendedPathToBranch[len - 3];
            int branchCellRow = extendedPathToBranch[len - 2];
            int branchCellCol = extendedPathToBranch[len - 1];
            int[] pathToGoal = LevelMetadata.getPath(branchCellRow, branchCellCol, goalRow, goalCol);
            int nextGoalPathRow = pathToGoal[2];
            int nextGoalPathCol = pathToGoal[3];
            int[] newAgentCell = getFirstEmptyCellExcluding(branchCellRow, branchCellCol, new int[]{newBoxRow, newBoxCol, nextGoalPathRow, nextGoalPathCol});

            actions[actionIndex++] = getPullAction(newBoxRow, newBoxCol, branchCellRow, branchCellCol, newAgentCell[0], newAgentCell[1]);

            int[] newPathToGoal = LevelMetadata.getPath(newAgentCell[0], newAgentCell[1], goalRow, goalCol);

            transformToPushActions(newPathToGoal, 0, actions, actionIndex);
        }
    }

    private static int[] getFirstEmptyCellExcluding(int startRow, int startCol, int[] excludedCells) {
        for (int[] delta : new int[][]{{-1, 0}, {1, 0}, {0, -1}, {0, 1}}) {
            int row = startRow + delta[0];
            int col = startCol + delta[1];
            boolean isExcludedCell = false;
            for (int i=0; i<excludedCells.length; i+=2) {
                int excludedRow = excludedCells[i];
                int excludedCol = excludedCells[i + 1];
                if (StateUtils.isSameCell(row, col, excludedRow, excludedCol)) {
                    isExcludedCell = true;
                    break;
                }
            }
            if (!isExcludedCell && !StateUtils.cellIsWall(row, col)) {
                return new int[]{row, col};
            }
        }
        return new int[]{-1, -1};
    }

    private static int[] prependCell(int[] pathToBranch, int boxRow, int boxCol) {
        int[] extendedPathToBranch = new int[pathToBranch.length + 2];
        System.arraycopy(pathToBranch, 0, extendedPathToBranch, 2, pathToBranch.length);
        extendedPathToBranch[0] = boxRow;
        extendedPathToBranch[1] = boxCol;
        return extendedPathToBranch;
    }

    private static boolean shouldPushToBranchCell(int[] pathToBranch, int agentRow, int agentCol, int boxRow, int boxCol) {
        int lastRow = pathToBranch[pathToBranch.length - 2];
        int lastCol = pathToBranch[pathToBranch.length - 1];
        int agentLastDistance = LevelMetadata.getDistance(lastRow, lastCol, agentRow, agentCol);
        int boxLastDistance = LevelMetadata.getDistance(lastRow, lastCol, boxRow, boxCol);
        return agentLastDistance > boxLastDistance;
    }

    private static int[] findClosestBranchCell(int agentRow, int agentCol) {
        int minDistance = LevelMetadata.MAP_AREA;
        int minRow = -1;
        int minCol = -1;

        for(int i=0; i<LevelMetadata.BRANCH_CELLS.length; i += 2) {
            int row = LevelMetadata.BRANCH_CELLS[i];
            int col = LevelMetadata.BRANCH_CELLS[i+1];
            int distance = LevelMetadata.getDistance(agentRow, agentCol, row, col);
            if (distance < minDistance) {
                minDistance = distance;
                minRow = row;
                minCol = col;
            }
        }

        return new int[]{minRow, minCol};
    }

    private static void addFinalPull(int prevRow, int prevCol, int goalRow, int goalCol, Action[] actions, int actionIndex) {
        for (int[] delta : new int[][]{{-1, 0}, {1, 0}, {0, -1}, {0, 1}}) {
            int row = goalRow + delta[0];
            int col = goalCol + delta[1];
            if (!(row == prevRow && col == prevCol) && !StateUtils.cellIsWall(row, col)) {
                actions[actionIndex] = getPullAction(prevRow, prevCol, goalRow, goalCol, row, col);
                return;
            }
        }
    }

    private static boolean shouldPush(int agentRow, int agentCol, int nextBoxRow, int nextBoxCol) {
        return !StateUtils.isSameCell(agentRow, agentCol, nextBoxRow, nextBoxCol);
    }

    private static boolean canPullToGoal(int goalRow, int goalCol) {
        return StateUtils.getNumOfWallsAround(goalRow, goalCol) < 3;
    }

    private static Action[] removeNullActions(Action[] actions) {
        int i = actions.length - 1;
        if (i <= 0) {
            return actions;
        }

        for (; i >= 0 && actions[i] == null; i--) {}
        return Arrays.copyOf(actions, i + 1);
    }

    public static Action getMoveAction(int prevRow, int prevCol, int row, int col) {
        int deltaRow = row - prevRow;
        int deltaCol = col - prevCol;
        return Action.deltaToAction(deltaRow, deltaCol);
    }

    private static Action getPullAction(int prevRow, int prevCol, int row, int col, int nextRow, int nextCol) {
        int agentDeltaRow = nextRow - row;
        int agentDeltaCol = nextCol - col;
        int boxDeltaRow = row - prevRow;
        int boxDeltaCol = col - prevCol;

        return Action.deltaToAction(agentDeltaRow, agentDeltaCol, boxDeltaRow, boxDeltaCol, ActionType.Pull);
    }

    private static Action getPushAction(int prevRow, int prevCol, int row, int col, int nextRow, int nextCol) {
        int agentDeltaRow = row - prevRow;
        int agentDeltaCol = col - prevCol;
        int boxDeltaRow = nextRow - row;
        int boxDeltaCol = nextCol - col;

        return Action.deltaToAction(agentDeltaRow, agentDeltaCol, boxDeltaRow, boxDeltaCol, ActionType.Push);
    }
}
